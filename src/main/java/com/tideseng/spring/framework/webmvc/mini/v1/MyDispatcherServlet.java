package com.tideseng.spring.framework.webmvc.mini.v1;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.*;
import com.tideseng.spring.framework.annotation.*;

/**
 * 代码实现Spring核心原理v1.0
 * v1.0问题：
 *      面向过程，阅读性差、可维护性差
 *      映射容器功能过于集中
 *      接收参数写死、未实现RequestParam功能、不能动态匹配
 *      不能进行类型数据转换
 */
public class MyDispatcherServlet extends HttpServlet {

    /**
     * 映射容器，保存了类全名对应的Controller实例对象、指定名或类全名对应的Service实例对象、uri与method的映射关系
     */
    private Map<String, Object> mapping = new HashMap<String, Object>();

    /**
     * 初始化相关类：
     *      加载配置文件
     *      扫描包
     *      创建实例并放入映射容器
     *      保存uri与method的映射关系
     *      DI依赖注入
     * @param config
     * @throws ServletException
     */
    @Override
    public void init(ServletConfig config) throws ServletException {
        InputStream is = null;
        try{
            // 从web.xml中加载配置文件
            Properties configContext = new Properties();
            is = this.getClass().getClassLoader().getResourceAsStream(config.getInitParameter("contextConfigLocation"));
            configContext.load(is);

            // 扫描指定包下的所有class文件，将类的全路径作为映射容器的key
            String scanPackage = configContext.getProperty("scan-package");
            doScanner(scanPackage);

            // 实例化Controller和Service对象，保存访问路径对应的method
            for (String className : mapping.keySet()) {
                if(!className.contains(".")){continue;}
                Class<?> clazz = Class.forName(className);
                if(clazz.isAnnotationPresent(MyController.class)){
                    // 实例化Controller，通过类的全路径实例化对象，并保存在映射容器中
                    mapping.put(className, clazz.newInstance());
                    String baseUrl = "";
                    if (clazz.isAnnotationPresent(MyRequestMapping.class)) {
                        MyRequestMapping requestMapping = clazz.getAnnotation(MyRequestMapping.class);
                        // 获取到Controller的路径
                        baseUrl = requestMapping.value();
                    }
                    Method[] methods = clazz.getMethods();
                    for (Method method : methods) {
                        if (!method.isAnnotationPresent(MyRequestMapping.class)) {continue;}
                        MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);
                        // 获取到Controller中方法的路径，并生成对应的访问路径
                        String url = (baseUrl + "/" + requestMapping.value()).replaceAll("/+", "/");
                        // 在映射容器中保存请求路径和对应的方法
                        mapping.put(url, method);
                        System.out.println("Mapped " + url + "," + method);
                    }
                }else if(clazz.isAnnotationPresent(MyService.class)){
                    // 实例化Service
                    MyService service = clazz.getAnnotation(MyService.class);
                    String beanName = service.value();
                    if("".equals(beanName)){beanName = clazz.getName();}
                    Object instance = clazz.newInstance();
                    mapping.put(beanName, instance);
                    // 将Service类的接口也作为key存入IOC容器
                   for (Class<?> i : clazz.getInterfaces()) {
                        mapping.put(i.getName(), instance);
                    }
                }else {continue;}
            }

            // 注入Controller中的Service
            for (Object object : mapping.values()) {
                if(object == null){continue;}
                Class clazz = object.getClass();
                if(clazz.isAnnotationPresent(MyController.class)){
                    Field[] fields = clazz.getDeclaredFields();
                    for (Field field : fields) {
                        if(!field.isAnnotationPresent(MyAutowired.class)){continue;}
                        MyAutowired autowired = field.getAnnotation(MyAutowired.class);
                        if(autowired.required()){
                            String beanName = autowired.value();
                            // 不指定名称时根据类型注入
                            if("".equals(beanName)){beanName = field.getType().getName();}
                            field.setAccessible(true);
                            try {
                                // 属性注入
                                field.set(mapping.get(clazz.getName()), mapping.get(beanName));
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(is != null){
                try {is.close();} catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        System.out.print("My MVC Framework is init");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    /**
     * 处理请求：
     *      接收请求
     *      匹配对应的method
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
            String uri = req.getRequestURI().replace(req.getContextPath(), "").replaceAll("/+", "/");
            if(!this.mapping.containsKey(uri)){
                resp.getWriter().write("404 Not Found!!");
                return;
            }
            Method method = (Method) this.mapping.get(uri);
            Map<String,String[]> params = req.getParameterMap();
            String param = params.get("name")[0];
            method.invoke(this.mapping.get(method.getDeclaringClass().getName()), new Object[]{req, resp, param});
        } catch (Exception e) {
            e.printStackTrace();
            resp.getWriter().write("500 Exception " + Arrays.toString(e.getStackTrace()));
        }
    }

    /**
     * 扫描指定包下的所有class文件，将类的全路径作为映射容器的key（递归调用）
     * @param scanPackage
     * @throws URISyntaxException
     */
    private void doScanner(String scanPackage) throws URISyntaxException {
//        String classpath = this.getClass().getResource("/"+scanPackage.replaceAll("\\.","/")).toURI().getPath();
        String classpath = this.getClass().getClassLoader().getResource(scanPackage.replaceAll("\\.","/")).toURI().getPath();
        File classDir = new File(classpath);
        for (File file : classDir.listFiles()) {
            if(file.isDirectory()){
                doScanner(scanPackage + "." +  file.getName());
            } else {
                if(!file.getName().endsWith(".class"))
                    continue;
                String clazzName = (scanPackage + "." + file.getName().replace(".class",""));
                mapping.put(clazzName, null);
            }
        }
    }

}
