package com.siemens.internship.service;

import com.siemens.internship.model.Item;
import com.siemens.internship.repository.ItemRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ItemService {
    private static final Logger logger = LoggerFactory.getLogger(ItemService.class);
    private static final ExecutorService executor = Executors.newFixedThreadPool(10);
    private final ConcurrentHashMap<Long, Item> processedItems = new ConcurrentHashMap<>();

    private final ItemRepository itemRepository;

    @Autowired
    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public List<Item> findAll() {
        return itemRepository.findAll();
    }

    public Optional<Item> findById(Long id) {
        return itemRepository.findById(id);
    }

    /**
     * Saves an item to the database.
     * @param item The item to save
     * @return The saved item
     * @throws DataIntegrityViolationException if the email is already in use
     */
    public Item save(Item item) {
        try {
            return itemRepository.save(item);
        } catch (DataIntegrityViolationException e) {
            logger.error("Failed to save item: Email {} is already in use", item.getEmail());
            throw new DataIntegrityViolationException("Email is already in use");
        }
    }

    public void deleteById(Long id) {
        itemRepository.deleteById(id);
    }

    /**
     * Asynchronously processes all items in the database.
     * This method:
     * 1. Retrieves all item IDs from the database
     * 2. Processes each item asynchronously
     * 3. Updates the status of each item to "PROCESSED"
     * 4. Tracks successfully processed items
     * 5. Returns a list of all processed items when complete
     *
     * @return List of successfully processed items
     */
    @Async
    public List<Item> processItemsAsync() {
        List<Long> itemIds = itemRepository.findAllIds();
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        for (Long id : itemIds) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    // Simulate processing time
                    Thread.sleep(100);

                    Optional<Item> itemOpt = itemRepository.findById(id);
                    if (!itemOpt.isPresent()) {
                        logger.warn("Item with ID {} not found", id);
                        return;
                    }

                    Item item = itemOpt.get();
                    item.setStatus("PROCESSED");

                    Item savedItem = itemRepository.save(item);
                    processedItems.put(id, savedItem);

                    logger.info("Successfully processed item with ID: {}", id);
                } catch (InterruptedException e) {
                    logger.error("Processing interrupted for item ID {}: {}", id, e.getMessage());
                    Thread.currentThread().interrupt();
                } catch (Exception e) {
                    logger.error("Error processing item ID {}: {}", id, e.getMessage());
                }
            }, executor);
            futures.add(future);
        }

        // Wait for all futures to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        // Return list of processed items
        return new ArrayList<>(processedItems.values());
    }
}
