package com.ccwici.mvc.controller;

import com.ccwici.mvc.annotation.Autowired;
import com.ccwici.mvc.annotation.Controller;
import com.ccwici.mvc.annotation.RequestMapping;
import com.ccwici.mvc.annotation.RequestParam;
import com.ccwici.mvc.service.TestService;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Controller
@RequestMapping(value="/test")
public class TestController {
    @Autowired(value="testService")
    TestService testService;

    @RequestMapping(value="/index.do")
    public String index(@RequestParam(value="param") String param, HttpServletResponse response) {
        try {
            response.sendRedirect("323222");
        } catch (IOException e) {
            e.printStackTrace();
        }

        return testService.test() + " with param " + param;
    }
}
