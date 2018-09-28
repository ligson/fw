package org.ligson.fw.core;

import org.ligson.fw.core.annotation.Component;
import org.ligson.fw.core.annotation.FWApp;
import org.ligson.fw.core.annotation.Service;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

public class Application {
    private Context context;

    public <T> T getBeanByClass(Class<T> clazz) {
        return (T) context.get(clazz).getInstance();
    }

    public List<File> listDir(File file) {
        List<File> files = new ArrayList<>();
        if (file.isFile()) {
            files.add(file);
        } else {
            for (File file1 : file.listFiles()) {
                files.addAll(listDir(file1));
            }
        }
        return files;
    }

    public List<Class> findClassByPackage(String packageName) {
        List<Class> classList = new ArrayList<>();
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        String pack = packageName.replaceAll("\\.", "/");
        try {
            Enumeration<URL> url = loader.getResources(pack);
            while (url.hasMoreElements()) {
                URL url1 = url.nextElement();
                File file = new File(url1.getFile());
                List<File> files = listDir(file);
                for (File file1 : files) {
                    String clazzName = packageName + file1.getAbsolutePath().replaceAll(file.getAbsolutePath(), "").replaceAll("/", "\\.").replaceAll(".class", "");
                    classList.add(Class.forName(clazzName));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return classList;
    }

    public void run(String[] args) throws Exception {
        StackTraceElement[] st = Thread.currentThread().getStackTrace();
        StackTraceElement mainInvoke = st[st.length - 1];
        String clazzName = mainInvoke.getClassName();
        String methodName = mainInvoke.getMethodName();
        Class firstClazz = Class.forName(clazzName);
        FWApp fwApp = (FWApp) firstClazz.getDeclaredAnnotation(FWApp.class);
        context = new Context(fwApp);
        String[] packages = fwApp.basePackages();
        if (packages.length == 0) {
            packages = new String[]{firstClazz.getPackage().getName()};
        }
        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        List<Class> classList = new ArrayList<>();
        for (String aPackage : packages) {
            classList.addAll(findClassByPackage(aPackage));
        }
        Class[] scanAnonClass = new Class[]{Component.class, Service.class};
        for (Class aClass : classList) {
            boolean contain = false;
            for (Class anonClass : scanAnonClass) {
                if (aClass.getDeclaredAnnotation(anonClass) != null) {
                    contain = true;
                    break;
                }
            }
            if (contain) {
                context.put(aClass);
            }
        }
        context.initAopFilters();
    }
}
