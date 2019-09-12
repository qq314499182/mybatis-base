package com.loit.base.utils;

/**
 * @Auther: zhaoke
 * @Date: 2019/8/27 0027  15:19
 * @Description:
 */
public class ThreadLocalUtils {

    private static final ThreadLocal<Object> THREAD_LOCAL = new ThreadLocal<>();

    public static Object get(){
        return THREAD_LOCAL.get();
    }

    public static void set(Class clazz){
        THREAD_LOCAL.set(clazz);
    }

    public static void set(String str){
        THREAD_LOCAL.set(str);
    }

    public static void remove(){
        THREAD_LOCAL.remove();
    }
}
