package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.service.CategoryService;
import capstonesu25.warehouse.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/category")
@RequiredArgsConstructor
@Validated
public class CategoryController {
    private final CategoryService categoryService;

    @Operation(summary = "Get all categories")
    @GetMapping()
    
    public ResponseEntity<?> getAll(){
        return ResponseUtil.getCollection(
                categoryService.getAllCategories(),
                HttpStatus.OK,
                "Fetch all categories successfully",
                null
        );
    }

    @Operation(summary = "Get category by id")
    @GetMapping("{categoryId}")
    
    public ResponseEntity<?> getById(@PathVariable Long categoryId) {
        return ResponseUtil.getObject(
                categoryService.getCategoryById(categoryId),
                HttpStatus.OK,
                "Fetch all categories successfully"
        );
    }
}
