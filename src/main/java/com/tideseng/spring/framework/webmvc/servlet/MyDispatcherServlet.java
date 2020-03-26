package com.tideseng.spring.framework.webmvc.servlet;

import com.tideseng.spring.framework.annotation.MyController;
import com.tideseng.spring.framework.annotation.MyRequestMapping;
import com.tideseng.spring.framework.context.MyApplicationContext;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

@Slf4j
public class MyDispatcherServlet extends HttpServlet {

    private static final String LOCATION = "contextConfigLocation";

    private List<MyHandlerMapping> handlerMappings = new ArrayList<>();
    private Map<MyHandlerMapping, MyHandlerAdapter> handlerAdapters = new ConcurrentHashMap<>();
    private List<MyViewResolver> viewResolvers = new ArrayList<>();

    @Override
    public void init(ServletConfig config) throws ServletException {
        try{
            //1、初始化ApplicationContext
            MyApplicationContext context = new MyApplicationContext(config.getInitParameter(LOCATION));
            //2、初始化Spring MVC九大组件
            initStrategies(context);
        } catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("utf-8");
        resp.setContentType("text/html;charset=utf-8");
        try {
            doDispatch(req, resp);
        } catch (Exception e) {
            e.printStackTrace();
            Map<String, Object> error = new HashMap<>();
            error.put("detail", e.getMessage());
            error.put("stackTrace", Arrays.toString(e.getStackTrace()));
            processDispatchResult(req, resp, new MyModelAndView("500", error));
        }
    }

    private void doDispatch(HttpServletRequest req, HttpServletResponse resp) throws Exception {
        //1、通过request请求中的URL，匹配HandlerMapping
        MyHandlerMapping handlerMapping = getHandler(req);
        if(handlerMapping == null) {
            processDispatchResult(req, resp, new MyModelAndView("404"));
            return;
        }

        //2、准备调用前的参数
        MyHandlerAdapter handlerAdapter = getHandlerAdapter(handlerMapping);

        //3、调用方法，返回ModelAndView（存储了页面模板名称、页面数据）
        MyModelAndView modelAndView = handlerAdapter.handle(req, resp, handlerMapping);

        //4、输出内容
        processDispatchResult(req, resp, modelAndView);
    }

    private MyHandlerMapping getHandler(HttpServletRequest req) {
        if(handlerMappings.isEmpty()) return null;
        String url = req.getRequestURI().replace(req.getContextPath(), "");
        for(MyHandlerMapping handler : handlerMappings){
            if(handler.getPattern().matcher(url).matches())
                return handler;
        }
        return null;
    }

    private MyHandlerAdapter getHandlerAdapter(MyHandlerMapping handler) {
        MyHandlerAdapter handlerAdapter = this.handlerAdapters.get(handler);
        if(handlerAdapter == null) return null;
        if(!handlerAdapter.supports(handler)) return null;
        return handlerAdapter;
    }

    private void processDispatchResult(HttpServletRequest req, HttpServletResponse resp, MyModelAndView modelAndView) throws IOException {
        if(modelAndView == null) return;
        if(this.viewResolvers.isEmpty()) return;

        for (MyViewResolver viewResolver : this.viewResolvers) {
            MyView view = viewResolver.resolveViewName(modelAndView.getViewName(), null);
            view.render(modelAndView.getModel(), req, resp);
            break;
        }
    }

    protected void initStrategies(MyApplicationContext context) throws Exception {
        // 初始化文件上传组件
        initMultipartResolver(context);
        // 初始化本地语言环境
        initLocaleResolver(context);
        // 初始化模板处理器
        initThemeResolver(context);
        // 初始化handlerMapping（必须实现），保存了Controller中RequestMapping与Method的一一对应关系
        initHandlerMappings(context);
        // 初始化handlerAdpater（必须实现），参数动态匹配并调用Method
        initHandlerAdapters(context);
        // 初始化异常拦截器
        initHandlerExceptionResolvers(context);
        // 初始化视图默认处理组件
        initRequestToViewNameTranslator(context);
        // 初始化视图转换器（必须实现）
        initViewResolvers(context);
        // 初始化flashMapManager
        initFlashMapManager(context);
    }

    private void initMultipartResolver(MyApplicationContext context) {}
    private void initLocaleResolver(MyApplicationContext context) {}
    private void initThemeResolver(MyApplicationContext context) {}
    private void initHandlerExceptionResolvers(MyApplicationContext context) {}
    private void initRequestToViewNameTranslator(MyApplicationContext context) {}
    private void initFlashMapManager(MyApplicationContext context) {}

    /**
     * 封装controller、method、url信息
     * @param context
     * @throws Exception
     */
    private void initHandlerMappings(MyApplicationContext context) throws Exception {
        String[] beanDefinitionNames = context.getBeanDefinitionNames();
        for (String beanDefinitionName : beanDefinitionNames) {
            Object controller = context.getBean(beanDefinitionName);
            Class<?> clazz = controller.getClass();
            if(!clazz.isAnnotationPresent(MyController.class)) continue;
            String controllerUri = clazz.getAnnotation(MyRequestMapping.class).value();
            for(Method method : clazz.getMethods()){ // 获取所有public方法
                if(!method.isAnnotationPresent(MyRequestMapping.class)) continue;
                MyRequestMapping requestMapping = method.getAnnotation(MyRequestMapping.class);
                String url = ("/"+controllerUri+"/"+requestMapping.value()).replaceAll("/+", "/");
                Pattern pattern = Pattern.compile(url);
                this.handlerMappings.add(new MyHandlerMapping(pattern, method, controller));
                log.info("Mapping: " + url + " , " + method);
            }
        }
    }

    /**
     * 将handlerMapping与handlerAdapter对应，处理参数自动匹配、Method调用
     * @param context
     */
    private void initHandlerAdapters(MyApplicationContext context) {
        for (MyHandlerMapping handlerMapping : this.handlerMappings) {
            this.handlerAdapters.put(handlerMapping, new MyHandlerAdapter());
        }
    }

    private void initViewResolvers(MyApplicationContext context) throws Exception {
        String templateRoot = context.getProperties().getProperty("templateRoot");
        File templateRootDir = new File(this.getClass().getClassLoader().getResource(templateRoot).toURI().getPath());
        for (int i=0; i<templateRootDir.listFiles().length; i++) { // 为了兼容多模板，模仿Spring用list保存
            this.viewResolvers.add(new MyViewResolver(templateRootDir));
        }
    }

}
