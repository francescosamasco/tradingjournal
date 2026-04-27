package it.samfrafx.tradingjournal.webapp.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("title", "Home");
        Map<String, Object> tradesByDate = new HashMap<>();

        tradesByDate.put("2025-04-04", Map.of("amount", 2900, "trades", 2));
        tradesByDate.put("2025-04-09", Map.of("amount", 4700, "trades", 1));
        tradesByDate.put("2025-04-17", Map.of("amount", -1500, "trades", 4));
        tradesByDate.put("2025-04-25", Map.of("amount", 999, "trades", 2));

        tradesByDate.put("2025-05-06", Map.of("amount", 850, "trades", 1));
        tradesByDate.put("2025-05-13", Map.of("amount", -320, "trades", 2));
        tradesByDate.put("2025-05-21", Map.of("amount", 1450, "trades", 3));

        model.addAttribute("tradesByDate", tradesByDate);
        return "home";
    }
    
    
    @GetMapping("/menu")
    public String menu(Model model) {
        model.addAttribute("title", "Menu");
        return "menu";
    }
    
    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        Map<String, Object> tradesByDate = new HashMap<>();

        tradesByDate.put("2025-04-04", Map.of("amount", 2900, "trades", 2));
        tradesByDate.put("2025-04-09", Map.of("amount", 4700, "trades", 1));
        tradesByDate.put("2025-04-17", Map.of("amount", -1500, "trades", 4));
        tradesByDate.put("2025-04-25", Map.of("amount", 123, "trades", 2));

        tradesByDate.put("2025-05-06", Map.of("amount", 850, "trades", 1));
        tradesByDate.put("2025-05-13", Map.of("amount", -320, "trades", 2));
        tradesByDate.put("2025-05-21", Map.of("amount", 1450, "trades", 3));

        model.addAttribute("tradesByDate", tradesByDate);

        return "dashboard";
    }
}