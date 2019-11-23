package com.ccwici.mvc.servlet;

import com.ccwici.mvc.annotation.Autowired;
import com.ccwici.mvc.annotation.Controller;
import com.ccwici.mvc.annotation.Service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.*;

public class ApplicationContext {
    private static String propertiesFileName;

    private List<Class<?>> classList = Collections.synchronizedList(new ArrayList<>());

    private Map<String, Object> aliasInstanceMap = new HashMap<>();

    public ApplicationContext(String fileName) {
        propertiesFileName = fileName;
        try{
            String basePackage = getBasePackage(propertiesFileName);
            buildAliasInstanceMap(basePackage);
            doAutowired();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    private void buildAliasInstanceMap(String basePackage) throws IllegalAccessException, InstantiationException {
        scanClasses(basePackage);
        if(classList.size() == 0) {
            return;
        }

        for(Class<?> clazz : classList) {
            if(clazz.isAnnotationPresent(Controller.class) || clazz.isAnnotationPresent(Service.class)||clazz.isAnnotationPresent(Autowired.class)) {
                String alias = getAlias(clazz);
                Object obj = aliasInstanceMap.get(alias);

                if(obj != null) {
                    throw new RuntimeException("alias is duplicate");
                }

                aliasInstanceMap.put(alias, clazz.newInstance());
            }
        }
    }

    private void doAutowired() {
        if(aliasInstanceMap.size() == 0) {
            return;
        }

        aliasInstanceMap.forEach((aliasKey, instance)-> {
            Field[] fields = instance.getClass().getDeclaredFields();
            String subAlias = "";
            for (Field field : fields) {
                Autowired autowired = field.getAnnotation(Autowired.class);
                if(autowired == null) {
                    continue;
                }
                subAlias = autowired.value();
                if("".equals(subAlias)) {
                    subAlias = getAlias(field.getType());
                }
                Object subInstance = null;
                if(!"".equals(subAlias)) {
                    subInstance = aliasInstanceMap.get(subAlias);
                }
                boolean accessible = field.isAccessible();
                field.setAccessible(true);
                try{
                    field.set(instance, subInstance);
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
                field.setAccessible(accessible);
            }
        });
    }

    public String getAlias(Class<?> clazz) {
        String alias = "";
        Controller controller = clazz.getAnnotation(Controller.class);

        if(controller != null) {
            alias = controller.value();
        }

        Service service = clazz.getAnnotation(Service.class);
        if(service != null) {
            alias = service.value();
        }

        Autowired autowired = clazz.getAnnotation(Autowired.class);
        if(autowired != null) {
            alias = autowired.value();
        }

        if(alias.equals("")) {
            String simpleName = clazz.getSimpleName();
            alias = simpleName.substring(0,1).toLowerCase() + simpleName.substring(1);
        }

        return alias;
    }

    private void scanClasses(String basePackage) {
        if (basePackage == null || "".equals(basePackage)) {
            return;
        }
        if(basePackage == null || "".equals(basePackage)) {
            return;
        }

        doScan(basePackage);
    }

    private void doScan(String basePackage) {
        String path = basePackage.replaceAll("\\.", "/");
        URL url = getClass().getClassLoader().getResource(path);
        File file = new File(url.getFile());
        file.listFiles((childFile)->{
            String fileName = childFile.getName();
            if(childFile.isDirectory()) {
                doScan(basePackage + "." + fileName);
            } else {
                if(fileName.endsWith(".class")) {
                    String className = basePackage +"." + fileName.replace(".class", "");
                    try {
                        Class<?> clazz = getClass().getClassLoader().loadClass(className);
                        classList.add(clazz);
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            }
            return false;
        });
    }

    private String getBasePackage(String fileName) throws IOException {
        String basePackage;
        Properties prop = new Properties();
        InputStream in = this.getClass().getClassLoader().getResourceAsStream(fileName);
        prop.load(in);
        basePackage = prop.getProperty("basePackage");
        return basePackage;
    }

    public List<Class<?>> getClassList() {
        return classList;
    }

    public Object getBean(String alias) {
        return aliasInstanceMap.get(alias);
    }

    public Map<String, Object> getAliasInstanceMap() {
        return aliasInstanceMap;
    }
}
