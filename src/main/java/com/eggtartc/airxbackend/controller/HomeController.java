package com.eggtartc.airxbackend.controller;

import com.eggtartc.airxbackend.controller.generic.BaseController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.text.SimpleDateFormat;
import java.util.Date;

@Controller
class HomeController extends BaseController {
    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("serverTime", getCurrentTimeFormatted());
        return "home";
    }

    private String getCurrentTimeFormatted() {
        return new SimpleDateFormat("EEE LLL dd yyyy HH:mm:ss 'GMT'Z (zzzz)")
            .format(new Date())
            .replaceAll("\\.", "");
    }
}
