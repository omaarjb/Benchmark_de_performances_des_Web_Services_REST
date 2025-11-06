package com.omar.datarest.controllers;

import org.springframework.web.bind.annotation.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import com.omar.datarest.repositories.ItemRepository;
import com.omar.entities.Item;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/categories/{categoryId}/items")
public class CategoryItemController {

    @Autowired
    private ItemRepository itemRepository;

    @GetMapping
    public Map<String, Object> itemsByCategory(
            @PathVariable("categoryId") Long categoryId,
            @RequestParam(name = "page", defaultValue = "0") int page,
            @RequestParam(name = "size", defaultValue = "20") int size) {

        Page<Item> result = itemRepository.findByCategoryId(categoryId, PageRequest.of(page, size));

        Map<String, Object> response = new HashMap<>();
        response.put("content", result.getContent());
        return response;
    }
}
