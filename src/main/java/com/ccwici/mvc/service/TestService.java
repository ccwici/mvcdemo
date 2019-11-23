package com.ccwici.mvc.service;

import com.ccwici.mvc.annotation.Autowired;
import com.ccwici.mvc.annotation.Service;

@Service
public class TestService {
    public String test() {
        System.out.println("test()");
        return "TestService.test()";
    }
}
