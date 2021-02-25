package com.javam4.modules.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : niceyoo
 * @version : 1.0
 * @title : HtmlController
 * @description :
 * @copyright : 公共服务与应急管理战略本部 Copyright(c)2020
 * @date : 2021/2/8 13:53
 */
@Controller
@RequestMapping("/html")
public class HtmlController {

    @GetMapping("/success")
    public String success() {
        return "success";
    }

    @GetMapping("/error")
    public String error() {
        return "error";
    }

}


