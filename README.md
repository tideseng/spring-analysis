# 手写实现Spring核心原理

## IOC

入口：refresh()

关键逻辑：
- 定位：getResource
- 加载：loadBeanDefinitions
- 注册：registerBeanDefinition（伪IOC容器）
  
## DI

入口：getBean()

关键逻辑：
- 实例化：instantiateBean（单例IOC容器和缓存IOC容器）
- 依赖注入：populateBean

## AOP

入口：getBean()

关键逻辑：
- 加载：AdvisedSupport
- 解析：MethodInterceptor（生成拦截器链）
- 执行：proceed

## MVC

入口：initStrategies()

关键逻辑：
- 映射：initHandlerMappings
- 适配：initHandlerAdapters
- 解析：initViewResolvers
- 输出：render