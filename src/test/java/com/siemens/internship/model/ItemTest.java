package com.siemens.internship.model;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ItemTest {
    private static Validator validator;

    @BeforeAll
    static void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testValidItem() {
        Item item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setStatus("PENDING");
        item.setEmail("test@example.com");

        assertTrue(validator.validate(item).isEmpty());
    }

    @Test
    void testInvalidName() {
        Item item = new Item();
        item.setName(""); // Empty name
        item.setDescription("Test Description");
        item.setStatus("PENDING");
        item.setEmail("test@example.com");

        assertFalse(validator.validate(item).isEmpty());
    }

    @Test
    void testInvalidEmail() {
        Item item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setStatus("PENDING");
        item.setEmail("invalid-email"); // Invalid email format

        assertFalse(validator.validate(item).isEmpty());
    }

    @Test
    void testInvalidStatus() {
        Item item = new Item();
        item.setName("Test Item");
        item.setDescription("Test Description");
        item.setStatus("INVALID_STATUS"); // Invalid status
        item.setEmail("test@example.com");

        assertFalse(validator.validate(item).isEmpty());
    }

    @Test
    void testDescriptionLength() {
        Item item = new Item();
        item.setName("Test Item");
        item.setDescription("a".repeat(501)); // Description too long
        item.setStatus("PENDING");
        item.setEmail("test@example.com");

        assertFalse(validator.validate(item).isEmpty());
    }
} 