package com.greenfox.szilvi.githubchecker.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class MainController {

    @RequestMapping("/index")
    public String getMain(){
        return "index";
    }
}
