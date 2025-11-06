package com.omar.springmvc.controller;

import com.omar.entities.Item;
import com.omar.springmvc.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    @GetMapping
    public List<Item> getItems(
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size,
            @RequestParam(value = "categoryId", required = false) Long categoryId) {

        Pageable pageable = PageRequest.of(page, size);

        if (categoryId != null) {
            Page<Item> itemsPage = itemRepository.findByCategoryId(categoryId, pageable);
            return itemsPage.getContent(); // Retourne seulement la liste
        }

        Page<Item> itemsPage = itemRepository.findAll(pageable);
        return itemsPage.getContent(); // Retourne seulement la liste
    }

    @GetMapping("/{id}")
    public ResponseEntity<Item> getItem(@PathVariable("id") Long id) {
        Optional<Item> item = itemRepository.findById(id);
        return item.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public Item createItem(@Valid @RequestBody Item item) {
        return itemRepository.save(item);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Item> updateItem(
            @PathVariable("id") Long id,
            @Valid @RequestBody Item itemDetails) {
        return itemRepository.findById(id)
                .map(item -> {
                    item.setName(itemDetails.getName());
                    item.setDescription(itemDetails.getDescription());
                    item.setPrice(itemDetails.getPrice());
                    item.setCategory(itemDetails.getCategory());
                    return ResponseEntity.ok(itemRepository.save(item));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteItem(@PathVariable("id") Long id) {
        return itemRepository.findById(id)
                .map(item -> {
                    itemRepository.delete(item);
                    return ResponseEntity.ok().build();
                })
                .orElse(ResponseEntity.notFound().build());
    }
}