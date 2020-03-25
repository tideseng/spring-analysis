package com.tideseng.spring.framework.beans.support;

import com.tideseng.spring.framework.beans.config.MyBeanDefinition;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * BeanDefinition生成器
 */
public class MyBeanDefinitionReader {

    private static final String SCAN = "scan-package";

    private Properties properties = new Properties();
    private List<String> registyBeanClassNames = new ArrayList<>();

    public MyBeanDefinitionReader(String... locations) throws Exception {
        // 加载配置文件
        doLoadConfig(locations[0].replace("classpath:", ""));

        // 扫描包下所有类
        doScanner(properties.getProperty(SCAN));
    }

    /**
     * 将配置文件中扫描到的所有的配置信息转换为GPBeanDefinition对象，以便于之后IOC操作方便
     * @return
     * @throws Exception
     */
    public List<MyBeanDefinition> loadBeanDefinitions() throws Exception{
        List<MyBeanDefinition> beanDefinitions = new ArrayList<>();
        for(String beanClassName : registyBeanClassNames) {
            Class<?> beanClass = Class.forName(beanClassName);
            if(beanClass.isInterface()) continue;

            beanDefinitions.add(createBeanDefinition(toLowerFirstCase(beanClass.getSimpleName()), beanClassName)); // 类以简单类名作为factoryBeanName
            beanDefinitions.add(createBeanDefinition(beanClass.getName(), beanClassName)); // 类以全类名作为factoryBeanName
            for(Class<?> i : beanClass.getInterfaces()){
                beanDefinitions.add(createBeanDefinition(i.getName(), beanClassName)); // 类的接口以接口全名作为factoryBeanName
            }
        }
        return beanDefinitions;
    }

    private MyBeanDefinition createBeanDefinition(String factoryBeanName, String beanClassName) throws Exception {
        return new MyBeanDefinition(factoryBeanName, beanClassName);
    }

    public Properties getProperties(){
        return this.properties;
    }

    private void doLoadConfig(String location) throws Exception {
        InputStream is = null;
        try{
            is = this.getClass().getClassLoader().getResourceAsStream(location);
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
                registyBeanClassNames.add(scanPackage+"."+file.getName().replace(".class", ""));
            }
        }
    }

    private String toLowerFirstCase(String str) {
        char[] chars = str.toCharArray();
        chars[0] += 1<<5; // 1向左移动5位，即+32（ASCII中大小写相差32）
        return String.valueOf(chars);
    }

}
