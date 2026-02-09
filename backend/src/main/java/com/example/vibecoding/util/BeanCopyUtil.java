package com.example.vibecoding.util;

import org.springframework.beans.BeanUtils;

/**
 * Bean复制工具类
 * 用于统一处理Entity、DTO、VO之间的转换
 */
public class BeanCopyUtil {
    /**
     * 将源对象复制到目标对象
     * @param source 源对象
     * @param targetClass 目标对象类型
     * @return 目标对象
     */
    public static <T> T copy(Object source, Class<T> targetClass) {
        if (source == null) {
            return null;
        }
        try {
            T target = targetClass.newInstance();
            BeanUtils.copyProperties(source, target);
            return target;
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException("对象复制失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 将源对象复制到已存在的目标对象
     * @param source 源对象
     * @param target 目标对象
     */
    public static void copy(Object source, Object target) {
        if (source == null || target == null) {
            return;
        }
        BeanUtils.copyProperties(source, target);
    }
}