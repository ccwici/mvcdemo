package com.ccwici.mvc.servlet;

import java.util.HashMap;
import java.util.Map;

public class ModelAndView {
    private String viewName;

    private Map<String, Object> data = new HashMap<>();

    public ModelAndView(String viewName) {
        this.viewName = viewName;
    }

    public void addAttribute(String name, Object value) {
        data.put(name, value);
    }

    public Map<String, Object> getData() {
        return data;
    }
}
