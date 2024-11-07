package app.web.controller;

import app.web.Service.CategoryService;

import app.web.persistence.entities.CategoryEntity;
import app.web.persistence.entities.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.Set;

@Controller
@RequestMapping("/finance")
public class FinanceController {

    public FinanceController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

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


    // Muestra las categorías del usuario actual
    @GetMapping("/categories")
    public String mostrarCategorias(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user, Model model) {
        Set<CategoryEntity> category = categoryService.getCategoriesByUser(user);
        model.addAttribute("categories", category);
        return "finance/categories";  // Asegúrate de que la vista esté en la ruta correcta
    }

    // Agrega una nueva categoría para el usuario actual
    @PostMapping("/categories")
    @ResponseBody
    public ResponseEntity<CategoryEntity> createCategory(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user, @RequestBody CategoryEntity category) {
        try {
            CategoryEntity savedCategory = categoryService.createCategory(category, user);
            return new ResponseEntity<>(savedCategory, HttpStatus.CREATED);
        } catch (Exception e) {
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
