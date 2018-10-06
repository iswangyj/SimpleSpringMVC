package com.test.core.controller;

import com.test.annotation.SimpleController;
import com.test.annotation.SimpleRequestMapping;
import com.test.annotation.SimpleRequestParam;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author wyj
 * @date 2018/10/5
 */
@SimpleController
@SimpleRequestMapping("/test")
public class TestController {

    @SimpleRequestMapping("/doTest1")
    public void test1(HttpServletRequest request, HttpServletResponse response,
                      @SimpleRequestParam("param") String param) {
        System.out.println(param);
        try {
            response.getWriter().write("doTest1 method successfully! param:" + param);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @SimpleRequestMapping("/doTest2")
    public void test2(HttpServletRequest request, HttpServletResponse response) {
        try {
            response.getWriter().write("doTest2 method successfully!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
