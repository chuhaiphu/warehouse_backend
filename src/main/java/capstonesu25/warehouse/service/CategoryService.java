package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.Category;
import capstonesu25.warehouse.entity.Item;
import capstonesu25.warehouse.model.category.CategoryResponse;
import capstonesu25.warehouse.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;

    private static final Logger LOGGER = LoggerFactory.getLogger(CategoryService.class);

    private List<CategoryResponse> getAllCategories() {
        LOGGER.info("Get all categories");
        return categoryRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private CategoryResponse getCategoryById(Long categoryId) {
        LOGGER.info("Get category by id: {}", categoryId);
        Category category = categoryRepository.findById(categoryId).orElseThrow(() -> new RuntimeException("Category not found"));
        return mapToResponse(category);
    }

    private CategoryResponse mapToResponse(Category category) {
        CategoryResponse categoryResponse = new CategoryResponse();
        categoryResponse.setId(category.getId());
        categoryResponse.setName(category.getName());
        categoryResponse.setDescription(category.getDescription());
        categoryResponse.setItemIds(category.getItems().stream().map(Item::getId).collect(Collectors.toList()));
        return categoryResponse;
    }
}
