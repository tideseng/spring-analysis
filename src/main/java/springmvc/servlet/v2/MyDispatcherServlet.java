package springmvc.servlet.v2;

import springmvc.annotation.*;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.*;

/**
 * 代码实现Spring核心原理v2.0
 * v2.0功能：
 *      应用设计模式提高了代码阅读性和可维护性
 *      实现RequestParam功能、动态匹配参数
 *      可以进行常用类型数据转换
 * v2.0缺陷
 *      处理请求调用方法时略显复杂，即handlerMapping封装的不够完美
 * 实现思路：
 *  1.配置阶段
 *      配置web.xml：              DispatcherServlet
 *      设定init-param：           contextConfigLocation=classpath:application.xml
 *      设定url-pattern：          /
 *      配置Annotation：           @Controller/@Service/@Autowired/@RequestMapping/@RequestParam
 *  2.初始化阶段
 *      调用init方法：             加载配置文件
 *      扫描相关类：               scan-package="com.tideseng"
 *      创建实例并保存到IOC容器：   通过反射实例化对象
 *      DI依赖注入：                   扫描IOC容器中的实例，给属性自动赋值
 *      初始化HandlerMapping：     将URI与对应的Method进行映射关联
 *  3.运行阶段
 *      调用doPost()/doGet()：     处理请求，获取request、response
 *      匹配HandlerMapping：       通过请求url获取对应的Method
 *      反射调用method.invoke()：    通过反射调用方法并返回结果
 *      response.getWrite().write()：将返会结果输出到客户端
 */
public class MyDispatcherServlet extends HttpServlet {

    private Properties properties = new Properties();
    private List<String> classNameList = new ArrayList<>();
    private Map<String, Object> ioc = new HashMap<>();
    private Map<String, Method> handlerMapping = new HashMap<>();

    /**
     * 二、初始化阶段（模板模式实现）
     *      加载配置文件
     *      扫描包
     *      创建实例并保存到IOC容器
     *      DI依赖注入
     *      初始化HandlerMapping
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        try{
            // 1.加载配置文件
            doLoadConfig(config.getInitParameter("contextConfigLocation"));

            // 2.扫描包
            doScanner(properties.getProperty("scan-package"));

            // 3.创建实例并保存到IOC容器
            doInstance(classNameList);

            // 4.DI依赖注入
            doInject(ioc);

            // 5.初始化HandlerMapping
            initHandlerMapping(ioc);
        } catch(Exception e){
            e.printStackTrace();
            throw new RuntimeException(e.getMessage());
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    /**
     * 委派模式
     * 三、运行阶段
     *      接收请求
     *      匹配HandlerMapping
     *      调用method
     *      响应客户端
     * @param req
     * @param resp
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("text/html;charset=utf-8");
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception " + Arrays.toString(e.getStackTrace()));
        }
    }

    private void doLoadConfig(String contextConfigLocation) throws IOException {
        InputStream is = null;
        try{
            is = this.getClass().getClassLoader().getResourceAsStream(contextConfigLocation);
            properties.load(is);
        } catch(Exception e){
            e.printStackTrace();
        } finally {
            if(is != null) is.close();
        }
    }

    private void doScanner(String scanPackage) throws URISyntaxException {
        String path = this.getClass().getClassLoader().getResource(scanPackage.replaceAll("\\.", "/")).toURI().getPath();
        for(File file : new File(path).listFiles()){
            if(file.isDirectory()){
                doScanner(scanPackage+"."+file.getName()); // 递归调用
            } else {
                if(!file.getName().endsWith(".class")) continue;
                classNameList.add(scanPackage+"."+file.getName().replace(".class", ""));
            }
        }
    }

    /**
     * 工厂模式&注册式单例模式
     * @param classNameList
     * @throws ClassNotFoundException
     * @throws IllegalAccessException
     * @throws InstantiationException
     */
    private void doInstance(List<String> classNameList) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        for(String className : classNameList){
            Class<?> clazz = Class.forName(className);
            if(clazz.isAnnotationPresent(MyController.class)){
                MyController controller = clazz.getAnnotation(MyController.class);
                String beanName = getAnnotationValueWithLowerFirstCase(clazz, controller.value());
                ioc.put(beanName, clazz.newInstance()); // Controller以全类名保存至IOC容器
            } else if (clazz.isAnnotationPresent(MyService.class)){
                MyService service = clazz.getAnnotation(MyService.class);
                String beanName = getAnnotationValueWithLowerFirstCase(clazz, service.value());
                Object instance = clazz.newInstance();
                ioc.put(beanName, instance); // Service以指定名或类名保存至IOC容器
                for(Class i : clazz.getInterfaces()){ // 【如果存在多接口怎么处理】
                    ioc.put(i.getName(), instance); // Service的接口以全类名保存至IOC容器（当存在相同接口时会被覆盖，依赖注入时建议指定名称）
                }
            }
        }
    }

    private String getAnnotationValueWithLowerFirstCase(Class<?> clazz, String value){
        if("".equals(value)) value = toLowerFirstCase(clazz.getSimpleName());
        if(ioc.containsKey(value)) throw new RuntimeException("The " + value + " is exists");
        return value;
    }

    private void doInject(Map<String, Object> ioc) throws IllegalAccessException {
        for(Map.Entry<String, Object> entry : ioc.entrySet()){
            for(Field field : entry.getValue().getClass().getDeclaredFields()){ // 获取class的所有属性
                if(!field.isAnnotationPresent(MyAutowired.class)) continue;
                MyAutowired autowired = field.getAnnotation(MyAutowired.class);
                String beanName = autowired.value();
                if("".equals(beanName)) beanName = field.getType().getName(); // 注入时不指定名称，以全类名/类型获取
                if(ioc.get(beanName) == null) throw new RuntimeException(beanName+"对象未创建，无法注入");
                field.setAccessible(true);
                field.set(entry.getValue(), ioc.get(beanName)); // 注入值
            }
        }
    }

    /**
     * 策略模式
     * @param ioc
     */
    private void initHandlerMapping(Map<String, Object> ioc) {
        for(Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if(!clazz.isAnnotationPresent(MyController.class)) continue;
            String controllerUri = clazz.getAnnotation(MyRequestMapping.class).value();
            for(Method method : clazz.getMethods()){ // 获取所有public方法
                if(!method.isAnnotationPresent(MyRequestMapping.class)) continue;
                MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);
                handlerMapping.put(("/"+controllerUri+"/"+requestMapping.value()).replaceAll("/+", "/"), method);
            }
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {
        String uri = req.getRequestURI().replace(req.getContextPath(), "");
        Method method = handlerMapping.get(uri);
        if(method == null) {
            resp.getWriter().write("404 error");
            return;
        }
        method.invoke(ioc.get(toLowerFirstCase(method.getDeclaringClass().getSimpleName())), getArgs(req, resp, method));
    }

    private Object[] getArgs(HttpServletRequest req, HttpServletResponse resp, Method method){
        Map<String, String[]> parameterMap = req.getParameterMap(); // 请求参数
        Class<?>[] parameterTypes = method.getParameterTypes(); // 形参列表
        Object[] args = new Object[parameterTypes.length]; // 实参列表
        for (int i = 0; i < parameterTypes.length; i++) {
            Class<?> parameterType = parameterTypes[i];
            if(parameterType == HttpServletRequest.class){
                args[i] = req;
            } else if(parameterType == HttpServletResponse.class){
                args[i] = resp;
            } else {
                Annotation[][] parameterAnnotations = method.getParameterAnnotations(); // 获取方法中加带注解的参数列表（一维数组是参数列表，二维数组是注解列表）
                for(Annotation annotation : parameterAnnotations[i]){
                    if(annotation instanceof MyRequestParam){
                        String paramName = ((MyRequestParam) annotation).value();
                        if(parameterMap.containsKey(paramName)) {
                            String paramValue = Arrays.toString(parameterMap.get(paramName)).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");
                            args[i] = convert(parameterType, paramValue);
                        }
                    }
                }
            }
        }
        return args;
    }

    /**
     * 数据类型转换
     * @param parameterType
     * @param paramValue
     * @return
     */
    private Object convert(Class<?> parameterType, String paramValue) {
        if(parameterType == Integer.class || parameterType == int.class)
            return Integer.parseInt(paramValue);
        return paramValue;
    }

    private String toLowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 1<<5; // 1向左移动5位，即+32（ASCII中大小写相差32）
        return String.valueOf(chars);
    }
    
}

