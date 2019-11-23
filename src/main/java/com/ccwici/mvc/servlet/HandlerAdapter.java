package com.ccwici.mvc.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

public class HandlerAdapter {
    private Map<Integer, String> paramMap = new HashMap<>();

    public Object handler(HttpServletRequest request, HttpServletResponse response, HandlerMapping handlerMapping) throws InvocationTargetException, IllegalAccessException {
        Method method = handlerMapping.getMethod();
        Object classInstance = handlerMapping.getControllerInstance();

        int paramNum = method.getParameterCount();
        Object[] paramObj = new Object[paramNum];
        for(int i = 0; i< paramNum; i++) {
            String paramName = paramMap.get(i);
            if(paramName.equals(HttpServletRequest.class.getName())) {
                paramObj[i] = request;
            } else if(paramName.equals(HttpServletResponse.class.getName())) {
                paramObj[i] = response;
            } else {
                paramObj[i] = request.getParameter(paramName);
            }
        }

        return method.invoke(classInstance, paramObj);
    }

    public void put(Integer index, String paramName) {
        paramMap.put(index, paramName);
    }
}
