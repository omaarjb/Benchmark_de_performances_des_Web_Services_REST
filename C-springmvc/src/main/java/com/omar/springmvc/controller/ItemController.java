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
import java.util.Optional;

@RestController
@RequestMapping("/items")
public class ItemController {

    @Autowired
    private ItemRepository itemRepository;

    @GetMapping
    public Page<Item> getItems(
            @RequestParam(value = "categoryId", required = false) Long categoryId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "50") int size,
            @RequestParam(value = "joinFetch", defaultValue = "false") boolean joinFetch) {

        Pageable pageable = PageRequest.of(page, size);

        if (categoryId != null) {
            if (joinFetch) {
                return itemRepository.findByCategoryIdWithJoinFetch(categoryId, pageable);
            } else {
                return itemRepository.findByCategoryId(categoryId, pageable);
            }
        }

        return itemRepository.findAll(pageable);
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
                    item.setSku(itemDetails.getSku());
                    item.setName(itemDetails.getName());
                    item.setPrice(itemDetails.getPrice());
                    item.setStock(itemDetails.getStock());
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