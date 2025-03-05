package com.school_project.smart_mirror.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ConnectionTest {

    @GetMapping("/hello")
    public String getMain() {
        return "hello";
    }
}
