package org.commons.beanutils;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Set;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.poi.ss.formula.functions.T;

public class BeanUtilsFaced {
    /**
     * 设置javaBean的属性
     * @param map
     * @param c
     * @return
     */
    @SuppressWarnings("hiding")
    public static <T> T mapToBean(Map<String, Object> map, Class<T> c) {
        T obj = null;

        try {
            obj = c.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }

        Set<Map.Entry<String, Object>> entrySet = map.entrySet();
        try {
            for (Map.Entry<String, Object> entry : entrySet) {
                BeanUtils.setProperty(obj, entry.getKey(), entry.getValue());

            }
        } catch (IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return obj;
    }
    /**
     * 把s1的属性值拷贝到S2中
     * @param target
     * @param src
     */
    @SuppressWarnings("hiding")
    public static <T> void BeanToBean(T target, T src) {
        try {
            BeanUtils.copyProperties(target, src);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    /**
     * 把一个map集合中的数据拷贝到javaBean中
     * @param map
     * @param t
     */
    public static <T> void mapToBean(Map<String, Object> map, T t) {
        try {
            BeanUtils.copyProperties(t, map);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }
}
