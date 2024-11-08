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
import java.util.stream.Collectors;

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


    @GetMapping("/categories")
    public String mostrarCategorias(@AuthenticationPrincipal org.springframework.security.core.userdetails.User user, Model model) {
        Set<CategoryEntity> categories = categoryService.getCategoriesByUser(user);

        // Filtra las categorías en "Ingresos" y "Gastos"
        Set<CategoryEntity> ingresos = categories.stream()
                .filter(CategoryEntity::isIncome)
                .collect(Collectors.toSet()); // Cambia a Collectors.toSet()

        Set<CategoryEntity> gastos = categories.stream()
                .filter(category -> !category.isIncome())
                .collect(Collectors.toSet()); // Cambia a Collectors.toSet()

        model.addAttribute("ingresos", ingresos);
        model.addAttribute("gastos", gastos);

        return "finance/categories";  // Asegúrate de que la vista esté en la ruta correcta
    }

    @PostMapping("/categories")
    @ResponseBody
    public ResponseEntity<CategoryEntity> createCategory(@AuthenticationPrincipal User user, @RequestBody CategoryEntity category) {
        try {
            // Logs para debug
            System.out.println("Datos recibidos:");
            System.out.println("Nombre categoría: " + category.getName());
            System.out.println("isIncome (antes): " + category.isIncome());

            CategoryEntity savedCategory = categoryService.createCategory(category, user);

            System.out.println("Categoría guardada:");
            System.out.println("ID: " + savedCategory.getId());
            System.out.println("Nombre: " + savedCategory.getName());
            System.out.println("isIncome (después): " + savedCategory.isIncome());

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
