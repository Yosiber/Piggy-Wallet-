package app.web.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/finance")
public class FinanceController {

    @GetMapping("/dashboard")
    public String dashboard() {
        return "/finance/dashboard";
    }

    @GetMapping("/transactions")
    public String transactions() {
        return "/finance/transactions";
    }

    @GetMapping("/categories")
    public String categories() {
        return "/finance/categories";
    }

    @GetMapping("/analysis")
    public String analysis() {
        return "/finance/analysis";
    }

    @GetMapping("/config")
    public String config() {
        return "/finance/config";
    }
}
