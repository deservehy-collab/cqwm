package com.sky.aspect;

import com.sky.annotation.AutoFill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;

//自动填充字段切面，实现公共自动填充字段的功能
@Aspect
@Component
@Slf4j
public class AutoFillAspect {
    //切入点
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.AutoFill)")
    public void autoFillPointCut() {
    }

    //通知
    @Before("autoFillPointCut()")
    public void autoFill(JoinPoint joinPoint) {
        log.info("自动填充字段");
        //获取当前拦截到的注解数据库的操作类型
        //方法获得签名
        MethodSignature methodSignature = (MethodSignature) joinPoint.getSignature();
        //获得方法上的注解对象
        AutoFill autoFill = methodSignature.getMethod().getAnnotation(AutoFill.class);
        //获得注解上的数据库操作类型
        OperationType operationType = autoFill.value();
        //获取当前被拦截的方法的参数--实体对象
        Object[] args = joinPoint.getArgs();
        //获取实体对象
        Object entity = args[0];
        //准备赋值的数据
        Long currentUserId = BaseContext.getCurrentId();
        LocalDateTime now = LocalDateTime.now();
        //根据不同的操作类型，通过反射来赋值对应的的属性
        if (operationType == OperationType.INSERT) {
            //为4个公共字段赋值
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //通过反射为对象属性赋值
                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentUserId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentUserId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (operationType == OperationType.UPDATE) {
            //为2个公共字段赋值
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);
                //通过反射为对象属性赋值
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentUserId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}