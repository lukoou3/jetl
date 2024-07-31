package com.lk.jetl.sql.expressions;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class NonAbstractSubclassFinder {

    // 示例用法
    public static void main(String[] args) {
        try {
            List<Class<?>> nonAbstractSubclasses = getNonAbstractSubclasses(Expression.class, "com.lk.jetl.sql.expressions");
            for (Class<?> clazz : nonAbstractSubclasses) {
                System.out.println(clazz.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static List<Class<?>> getNonAbstractSubclasses(Class<?> superclass, String packageName) throws Exception {
        List<Class<?>> nonAbstractSubclasses = new ArrayList<>();
        // 获取包下所有的Class文件
        List<Class<?>> allClasses = getClasses(packageName);
        // 筛选出非抽象类的子类
        for (Class<?> clazz : allClasses) {
            if (!superclass.isAssignableFrom(clazz) || clazz.isInterface() || Modifier.isAbstract(clazz.getModifiers())) {
                continue;
            }
            nonAbstractSubclasses.add(clazz);
        }
        return nonAbstractSubclasses;
    }

    private static List<Class<?>> getClasses(String packageName) throws ClassNotFoundException, IOException {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String path = packageName.replace('.', '/');
        Enumeration<URL> resources = classLoader.getResources(path);
        List<File> dirs = new ArrayList<>();
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            dirs.add(new File(URLDecoder.decode(resource.getFile(), "UTF-8")));
        }
        ArrayList<Class<?>> classes = new ArrayList<>();
        for (File directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }
        return classes;
    }

    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        List<Class<?>> classes = new ArrayList<>();
        if (!directory.exists()) {
            return classes;
        }
        File[] files = directory.listFiles();
        for (File file : files) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, packageName + "." + file.getName()));
            } else if (file.getName().endsWith(".class")) {
                classes.add(Class.forName(packageName + '.' + file.getName().substring(0, file.getName().length() - 6)));
            }
        }
        return classes;
    }

}
