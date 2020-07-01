package br.org.sistemafieg.aquecimentoapp.util;

import java.lang.reflect.Field;

public class UtilReflect {



    public static <E> E getValue(Object obj, String fieldName) {
        E result = null;
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            result = (E) field.get(obj);
            return result;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static Field getField(Object obj, String fieldName) {
        Field result = null;
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            field.setAccessible(true);
            result = field;
            return result;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return result;
    }

    public static Class typeColumn(Class clazz, String fieldName) {
        Class result = null;
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            result = field.getType();
            return result;
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }

        return result;
    }



}
