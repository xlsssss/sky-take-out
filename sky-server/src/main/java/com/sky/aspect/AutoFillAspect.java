package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.time.LocalDateTime;

/**
 * 自定义切面：实现公共字段自动填充处理逻辑
 */
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    /**
     * 切入点
     */
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut(){
    }

    /**
     * qian'z
     * @param joinPoint
     */
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint){
        log.info("开始进行公共字段自动填充...");

        //获取到当前被拦截的方法的数据库操作类型
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();//方法签名变量
        AutoFill annotation = signature.getMethod().getAnnotation(AutoFill.class);//获得方法上的注解对象
        OperationType operationType = annotation.value();//获得数据库操作类型

        //获取当前被拦截的方法的参数--实体对象
        Object[] args = joinPoint.getArgs();
        if(args == null || args.length == 0){
            return;
        }

        Object entity = args[0];

        //准备赋值的数据
        LocalDateTime now = LocalDateTime.now();
        Long currentID = BaseContext.getCurrentId();

        //根据当前不同的操作类型。为对应的属性通过反射来赋值
        if(operationType == OperationType.INSERT){
            //为四个公共字段赋值
            try {
                Method setCreateTimes = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUsers = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTimes = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUsers = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                //通过反射为对象属性赋值
                setCreateTimes.invoke(entity,now);
                setCreateUsers.invoke(entity,currentID);
                setUpdateTimes.invoke(entity,now);
                setUpdateUsers.invoke(entity,currentID);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        } else if (operationType == OperationType.UPDATE) {
            //为两个公共字段赋值
            try {
                Method setUpdateTimes = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUsers = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTimes.invoke(entity,now);
                setUpdateUsers.invoke(entity,currentID);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
