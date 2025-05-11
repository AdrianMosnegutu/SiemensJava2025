package com.siemens.internship.service;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class ItemServiceTest {

    @Mock
    private ItemRepository itemRepository;

    @InjectMocks
    private ItemService itemService;

    private Item testItem;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        testItem = new Item();
        testItem.setId(1L);
        testItem.setName("Test Item");
        testItem.setDescription("Test Description");
        testItem.setStatus("PENDING");
        testItem.setEmail("test@example.com");
    }

    @Test
    void testFindAll() {
        when(itemRepository.findAll()).thenReturn(Arrays.asList(testItem));
        List<Item> items = itemService.findAll();
        assertNotNull(items);
        assertEquals(1, items.size());
        assertEquals(testItem, items.get(0));
        verify(itemRepository, times(1)).findAll();
    }

    @Test
    void testFindById() {
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        Optional<Item> found = itemService.findById(1L);
        assertTrue(found.isPresent());
        assertEquals(testItem, found.get());
        verify(itemRepository, times(1)).findById(1L);
    }

    @Test
    void testSave() {
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);
        Item saved = itemService.save(testItem);
        assertNotNull(saved);
        assertEquals(testItem, saved);
        verify(itemRepository, times(1)).save(testItem);
    }

    @Test
    void testDeleteById() {
        doNothing().when(itemRepository).deleteById(1L);
        itemService.deleteById(1L);
        verify(itemRepository, times(1)).deleteById(1L);
    }

    @Test
    void testProcessItemsAsync() {
        when(itemRepository.findAllIds()).thenReturn(Arrays.asList(1L, 2L));
        when(itemRepository.findById(1L)).thenReturn(Optional.of(testItem));
        when(itemRepository.findById(2L)).thenReturn(Optional.of(testItem));
        when(itemRepository.save(any(Item.class))).thenReturn(testItem);

        List<Item> processedItems = itemService.processItemsAsync();
        assertNotNull(processedItems);
        assertEquals(2, processedItems.size());
        processedItems.forEach(item -> assertEquals("PROCESSED", item.getStatus()));
        verify(itemRepository, times(1)).findAllIds();
        verify(itemRepository, times(1)).findById(1L);
        verify(itemRepository, times(1)).findById(2L);
        verify(itemRepository, times(2)).save(any(Item.class));
    }
} 