package app.web.controller;

import app.web.Service.CategoryService;

import app.web.persistence.entities.CategoryEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Set;

@Controller
@RequestMapping("/finance")
public class FinanceController {

    @Autowired
    private CategoryService categoryService;

    @GetMapping("/dashboard")
    public String dashboard() {
        return "/finance/dashboard";
    }

    @GetMapping("/transactions")
    public String transactions() {
        return "/finance/transactions";
    }



    @GetMapping("/categories")
    public String mostrarCategorias(Model model) {
        Set<CategoryEntity> categories = categoryService.getCategories();
        model.addAttribute("Categories", categories);
        return "/finance/categories";
    }

    @PostMapping("/categories")
    @ResponseBody
    public ResponseEntity<CategoryEntity> createCategory(@RequestBody CategoryEntity category) {
        try {
            CategoryEntity savedCategory = categoryService.createCategory(category);
            return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
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
