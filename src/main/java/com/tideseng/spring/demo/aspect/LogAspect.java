package com.tideseng.spring.demo.aspect;

import com.tideseng.spring.framework.aop.aspect.MyJoinPoint;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;

@Slf4j
public class LogAspect {

    public void before(MyJoinPoint joinPoint){
        joinPoint.setUserAttribute("startTime_" + joinPoint.getMethod().getName(), System.currentTimeMillis());
        log.info("方法准备执行" +
                "\nTargetObject:" +  joinPoint.getThis() +
                "\nArgs:" + Arrays.toString(joinPoint.getArguments()));
    }

    public void afterReturning(MyJoinPoint joinPoint, Object resultValue){
        log.info("方法执行成功" +
                "\nTargetObject:" +  joinPoint.getThis() +
                "\nArgs:" + Arrays.toString(joinPoint.getArguments()) +
                "\nResultValue:" +  resultValue);
    }

    public void afterThrowing(MyJoinPoint joinPoint, Throwable e){
        log.info("方法出现异常" +
                "\nTargetObject:" +  joinPoint.getThis() +
                "\nArgs:" + Arrays.toString(joinPoint.getArguments()) +
                "\nThrows:" + e.getMessage());
    }

    public void after(MyJoinPoint joinPoint, Object resultValue){
        log.info("方法最终执行" +
                "\nTargetObject:" +  joinPoint.getThis() +
                "\nArgs:" + Arrays.toString(joinPoint.getArguments()));
        long startTime = (Long) joinPoint.getUserAttribute("startTime_" + joinPoint.getMethod().getName());
        long endTime = System.currentTimeMillis();
        System.out.println("use time :" + (endTime - startTime));
    }

}
