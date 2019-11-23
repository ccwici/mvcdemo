package com.ccwici.mvc.servlet;

import com.ccwici.mvc.annotation.Controller;
import com.ccwici.mvc.annotation.RequestMapping;
import com.ccwici.mvc.annotation.RequestParam;
import sun.misc.Request;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.*;

public class DispatchServlet extends HttpServlet {

    private ArrayList<HandlerMapping> handlerMappingList = new ArrayList<>();
    private Map<String, HandlerMapping> handlerMappingMap = new HashMap<>();
    private Map<HandlerMapping, HandlerAdapter> adapterMap = new HashMap<>();
    ApplicationContext applicationContext = null;

    public DispatchServlet() {
        System.out.println("======================================================");
    }

    @Override
    public void init(ServletConfig config) throws ServletException {
        String contextConfigLoaction = config.getInitParameter("contextConfigLoaction");
        ApplicationContext applicationContext = new ApplicationContext(contextConfigLoaction);
        Map<String, Object> aliasInstanceMap = applicationContext.getAliasInstanceMap();
        //初始化HandlerMapping
        initHandlerMapping(aliasInstanceMap);
        //初始化HandlerAdapter
        initHandlerAdapter();
    }

    private void initHandlerMapping(Map<String, Object> aliasInstanceMap) {
        aliasInstanceMap.forEach((alias,object)->{
            Class<?> clazz = object.getClass();
            Controller controller = clazz.getAnnotation(Controller.class);
            if(controller == null) {
                return;
            }
            RequestMapping requestMapping = clazz.getAnnotation(RequestMapping.class);
            String classUrl = requestMapping.value();
            for(Method method : clazz.getMethods()) {
                requestMapping = method.getAnnotation(RequestMapping.class);
                if (requestMapping == null) {
                    continue;
                }
                String url = requestMapping.value();
                HandlerMapping handlerMapping = new HandlerMapping();
                url = (classUrl + "/" + url).replaceAll("/{2,}", "/");
                handlerMapping.setUrl(url);
                handlerMapping.setMethod(method);
                handlerMapping.setControllerInstance(object);
                handlerMappingList.add(handlerMapping);
            }
        });
    }

    private void initHandlerAdapter() {
        handlerMappingList.stream().forEach((handlerMapping -> {
            Method method = handlerMapping.getMethod();
            HandlerAdapter handlerAdapter = new HandlerAdapter();
            int paramIndex = 0;
            for(Parameter paramter : method.getParameters()) {
                RequestParam requestParam = paramter.getAnnotation(RequestParam.class);
                String paramName;
                if(requestParam != null) {
                    paramName = requestParam.value();
                } else {
                    paramName = paramter.getParameterizedType().getTypeName();
                }
                handlerAdapter.put(paramIndex++, paramName);
            }
            handlerMappingMap.put(handlerMapping.getUrl(), handlerMapping);
            adapterMap.put(handlerMapping, handlerAdapter);
        }));
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String servletPath = req.getServletPath();
        HandlerMapping handlerMapping = handlerMappingMap.get(servletPath);
        if(handlerMapping == null) {
            super.doGet(req, resp);
            return;
        }

        Object result = null;
        try {
            result = adapterMap.get(handlerMapping).handler(req, resp, handlerMapping);
            resp.getOutputStream().write(result.toString().getBytes());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        super.doPost(req, resp);
    }
}
