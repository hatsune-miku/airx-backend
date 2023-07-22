package com.eggtartc.airxbackend.controller;

import com.eggtartc.airxbackend.controller.generic.BaseController;
import com.eggtartc.airxbackend.entity.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Optional;

@Controller
public class AccountActivationController extends BaseController {
    @Value("#{environment.getProperty('debug') != null && environment.getProperty('debug') != 'false'}")
    boolean isDebug;

    @GetMapping("/auth/activate/{token}")
    public String activate(Model model, @PathVariable String token) {
        // token is actually user salt
        Optional<User> userOpt = userRepository.findBySalt(token);
        if (userOpt.isEmpty()) {
            return "redirect:/not-found";
        }

        User user = userOpt.get();
        String activationStatusText;

        if (user.isActivated()) {
            activationStatusText = "User already activated.";
        }
        else {
            user.setActivated(true);
            try {
                userRepository.save(user);
                activationStatusText = "Activation successful!";
            }
            catch (Exception e) {
                activationStatusText = "Failed to activate user: " + e.getMessage();
            }
        }

        model.addAttribute("redirectUrl", getFrontendUrl() + "/activation-result");
        model.addAttribute("activationStatusText", activationStatusText);
        model.addAttribute("uid", user.getUid());
        return "activate";
    }

    private String getFrontendUrl() {
        return isDebug
            ? "http://localhost:5173"
            : "https://airx-cloud.eggtartc.com";
    }
}
