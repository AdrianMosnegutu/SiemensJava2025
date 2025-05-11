package com.siemens.internship.controller;

import com.siemens.internship.model.Item;
import com.siemens.internship.service.ItemService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * REST Controller for managing Item resources.
 * Provides endpoints for CRUD operations and item processing.
 */
@RestController
@RequestMapping("/api/items")
public class ItemController {
    private static final Logger logger = LoggerFactory.getLogger(ItemController.class);
    private final ItemService itemService;

    @Autowired
    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    /**
     * Retrieves all items.
     * @return List of all items with HTTP 200 OK status
     */
    @GetMapping
    public ResponseEntity<List<Item>> getAllItems() {
        logger.info("Retrieving all items");
        return ResponseEntity.ok(itemService.findAll());
    }

    /**
     * Creates a new item.
     * @param item The item to create
     * @param result Binding result for validation
     * @return Created item with HTTP 201 CREATED status, or error details with HTTP 400 BAD REQUEST
     */
    @PostMapping
    public ResponseEntity<?> createItem(@Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();

            result.getAllErrors().forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();
                errors.put(fieldName, errorMessage);
            });

            logger.warn("Validation failed for item creation: {}", errors);
            return ResponseEntity.badRequest().body(errors);
        }

        try {
            logger.info("Creating new item: {}", item.getName());

            Item savedItem = itemService.save(item);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedItem);
        } catch (DataIntegrityViolationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("email", "Email is already in use");

            logger.warn("Email already in use: {}", item.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }

    /**
     * Retrieves an item by ID.
     * @param id The ID of the item to retrieve
     * @return Item with HTTP 200 OK status, or HTTP 404 NOT FOUND if not found
     */
    @GetMapping("/{id}")
    public ResponseEntity<Item> getItemById(@PathVariable Long id) {
        logger.info("Retrieving item with ID: {}", id);
        return itemService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /**
     * Updates an existing item.
     * @param id The ID of the item to update
     * @param item The updated item data
     * @return Updated item with HTTP 200 OK status, or HTTP 404 NOT FOUND if not found
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateItem(@PathVariable Long id, @Valid @RequestBody Item item, BindingResult result) {
        if (result.hasErrors()) {
            Map<String, String> errors = new HashMap<>();

            result.getAllErrors().forEach(error -> {
                String fieldName = ((FieldError) error).getField();
                String errorMessage = error.getDefaultMessage();

                errors.put(fieldName, errorMessage);
            });

            logger.warn("Validation failed for item update: {}", errors);
            return ResponseEntity.badRequest().body(errors);
        }

        Optional<Item> existingItem = itemService.findById(id);
        if (existingItem.isEmpty()) {
            logger.warn("Item not found for update with ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        try {
            logger.info("Updating item with ID: {}", id);
            item.setId(id);

            Item updatedItem = itemService.save(item);
            return ResponseEntity.ok(updatedItem);
        } catch (DataIntegrityViolationException e) {
            Map<String, String> error = new HashMap<>();
            error.put("email", "Email is already in use");

            logger.warn("Email already in use: {}", item.getEmail());
            return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
        }
    }

    /**
     * Deletes an item.
     * @param id The ID of the item to delete
     * @return HTTP 204 NO CONTENT if successful, or HTTP 404 NOT FOUND if not found
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteItem(@PathVariable Long id) {
        Optional<Item> existingItem = itemService.findById(id);
        
        if (existingItem.isEmpty()) {
            logger.warn("Item not found for deletion with ID: {}", id);
            return ResponseEntity.notFound().build();
        }

        logger.info("Deleting item with ID: {}", id);
        itemService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Processes all items asynchronously.
     * @return List of processed items with HTTP 200 OK status
     */
    @GetMapping("/process")
    public ResponseEntity<List<Item>> processItems() {
        logger.info("Starting asynchronous processing of all items");
        List<Item> processedItems = itemService.processItemsAsync();
        return ResponseEntity.ok(processedItems);
    }
}
