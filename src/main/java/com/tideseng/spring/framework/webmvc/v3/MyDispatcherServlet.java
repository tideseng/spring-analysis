package com.tideseng.spring.framework.webmvc.v3;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.*;
import com.tideseng.spring.framework.annotation.*;

/**
 * 代码实现Spring核心原理v3.0
 * v3.0功能：
 *      优化HandlerMapping
 * 相关结论：
 *      Spring中的Bean不存在线程是否安全一说
 *          Spring是通过扫描包利用反射创建对象并放入IOC容器
 *          Bean是否线程安全只何Bean本身有关，和Spring无关
*       Spring中的不同的Bean有不同的回收机制，某些Bean不会被回收
 *          Spring中的Bean有singleton、prototype、request、session、global-session
 *          Spring中的Bean默认是singleton，会一直存在IOC容器中，而IOC容器本身就是单例，基于Spring上下文，singleton随着Spring的存亡而存亡
 *          prototype是用到的手创建，用完之后Bean的引用不指向任何地方，等着GC被回收
 */
public class MyDispatcherServlet extends HttpServlet {

    private Properties properties = new Properties();
    private List<String> classNameList = new ArrayList<>();
    private Map<String, Object> ioc = new HashMap<>();
    // 因为Handler中已经有了url和method的对应关系，根据设计原则（单一职责原则、最少知道原则）和性能差别不大上考虑，不用map
    private List<HandlerMapping> handlerMappings = new ArrayList<>();

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
    private void initHandlerMapping(Map<String, Object> ioc) throws IllegalAccessException, InstantiationException {
        for(Map.Entry<String, Object> entry : ioc.entrySet()) {
            Class<?> clazz = entry.getValue().getClass();
            if(!clazz.isAnnotationPresent(MyController.class)) continue;
            String controllerUri = clazz.getAnnotation(MyRequestMapping.class).value();
            for(Method method : clazz.getMethods()){ // 获取所有public方法
                if(!method.isAnnotationPresent(MyRequestMapping.class)) continue;
                MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);
                handlerMappings.add(new HandlerMapping(("/"+controllerUri+"/"+requestMapping.value()).replaceAll("/+", "/"), method, entry.getValue()));
            }
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws IOException, InvocationTargetException, IllegalAccessException {
        String uri = req.getRequestURI().replace(req.getContextPath(), "");
        HandlerMapping handlerMapping = getHandlerMapping(uri);
        if(handlerMapping == null) {
            resp.getWriter().write("404 error");
            return;
        }
        Object result = handlerMapping.getMethod().invoke(handlerMapping.getController(), getArgs(req, resp, handlerMapping));
        if(result == null || result instanceof  Void) return;
        resp.getWriter().write(result.toString());
    }

    private HandlerMapping getHandlerMapping(String uri) {
        if(handlerMappings.isEmpty()) return null;
        for(HandlerMapping handler : handlerMappings){
            if(handler.getUrl().equals(uri))
                return handler;
        }
        return null;
    }

    /**
     * 封装实参更为方便
     * @param req
     * @param resp
     * @param handlerMapping
     * @return
     */
    private Object[] getArgs(HttpServletRequest req, HttpServletResponse resp, HandlerMapping handlerMapping){
        Map<String, String[]> parameterMap = req.getParameterMap(); // 请求参数
        Class<?>[] parameterTypes = handlerMapping.getParameterTypes(); // 形参列表
        Object[] args = new Object[parameterTypes.length]; // 实参列表

        if(handlerMapping.getParamIndexMapping().containsKey(HttpServletRequest.class.getName())) {
            Integer reqIndex = handlerMapping.getParamIndexMapping().get(HttpServletRequest.class.getName());
            args[reqIndex] = req;
        }
        if(handlerMapping.getParamIndexMapping().containsKey(HttpServletResponse.class.getName())) {
            Integer respIndex = handlerMapping.getParamIndexMapping().get(HttpServletResponse.class.getName());
            args[respIndex] = resp;
        }
        for (Map.Entry<String, String[]> entry : parameterMap.entrySet()) {
            String paramValue = Arrays.toString(entry.getValue()).replaceAll("\\[|\\]", "").replaceAll(",\\s", ",");
            if(!handlerMapping.getParamIndexMapping().containsKey(entry.getKey())) continue;
            Integer index = handlerMapping.getParamIndexMapping().get(entry.getKey());
            args[index] = convert(parameterTypes[index], paramValue);
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

