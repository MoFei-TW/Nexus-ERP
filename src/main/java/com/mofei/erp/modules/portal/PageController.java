package com.mofei.erp.modules.portal;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PageController {

    @GetMapping("/")
    public String index() {
        return "redirect:/dashboard";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/employee")
    public String employee() {
        return "employee";
    }

    @GetMapping("/product")
    public String product() {
        return "product";
    }

    @GetMapping("/customer")
    public String customer() {
        return "customer";
    }

    @GetMapping("/order")
    public String order() {
        return "order";
    }
}
