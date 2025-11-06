package com.omar.springmvc.controller;

import com.omar.entities.Category;
import com.omar.entities.Item;
import com.omar.springmvc.repository.CategoryRepository;
import com.omar.springmvc.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/categories")
public class CategoryController {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ItemRepository itemRepository;

    @GetMapping
    public List<Category> getCategories(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Category> categoryPage = categoryRepository.findAll(pageable);
        return categoryPage.getContent(); // Retourne seulement la liste
    }

    @GetMapping("/{id}")
    public ResponseEntity<Category> getCategory(@PathVariable("id") Long id) {
        Optional<Category> category = categoryRepository.findById(id);
        return category.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Category createCategory(@Valid @RequestBody Category category) {
        return categoryRepository.save(category);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Category> updateCategory(
            @PathVariable("id") Long id,
            @Valid @RequestBody Category categoryDetails) {
        return categoryRepository.findById(id)
                .map(category -> {
                    category.setCode(categoryDetails.getCode());
                    category.setName(categoryDetails.getName());
                    return ResponseEntity.ok(categoryRepository.save(category));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteCategory(@PathVariable("id") Long id) {
        return categoryRepository.findById(id)
                .map(category -> {
                    Long itemCount = itemRepository.countByCategoryId(id);
                    if (itemCount > 0) {
                        return ResponseEntity.status(HttpStatus.CONFLICT)
                                .body("Cannot delete category with " + itemCount + " associated items");
                    }
                    categoryRepository.delete(category);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }

    // Endpoint relationnel - retourne seulement la liste des items
    @GetMapping("/{id}/items")
    public ResponseEntity<List<Item>> getCategoryItems(
            @PathVariable("id") Long id,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size) {

        if (!categoryRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }

        Pageable pageable = PageRequest.of(page, size);
        Page<Item> itemsPage = itemRepository.findByCategoryId(id, pageable);
        return ResponseEntity.ok(itemsPage.getContent()); // Retourne seulement la liste
    }
}