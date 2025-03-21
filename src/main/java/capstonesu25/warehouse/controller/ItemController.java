package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.item.ItemRequest;
import capstonesu25.warehouse.model.item.ItemResponse;
import capstonesu25.warehouse.model.responsedto.MetaDataDTO;
import capstonesu25.warehouse.service.ItemService;
import capstonesu25.warehouse.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/items")
@RequiredArgsConstructor
@Validated
public class ItemController {
    private final ItemService itemService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemController.class);

    @Operation(summary = "Get all items with pagination")
    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting all items");
        List<ItemResponse> result = itemService.getAllItems(page, limit);
        return ResponseUtil.getCollection(
                result,
                HttpStatus.OK,
                "Successfully retrieved all items",
                new MetaDataDTO(page < result.size(), page > 1, limit, result.size(), page)
        );
    }

    @Operation(summary = "Get item by ID")
    @GetMapping("/{itemId}")
    public ResponseEntity<?> getById(@PathVariable Long itemId) {
        LOGGER.info("Getting item by id: {}", itemId);
        ItemResponse result = itemService.getItemById(itemId);
        return ResponseUtil.getObject(
                result,
                HttpStatus.OK,
                "Successfully retrieved item"
        );
    }

    @Operation(summary = "Create a new item")
    @PostMapping
    public ResponseEntity<?> createItem(@RequestBody ItemRequest request) {
        LOGGER.info("Creating item");
        return ResponseUtil.getObject(
                itemService.create(request),
                HttpStatus.CREATED,
                "Successfully created item"
        );
    }

    @Operation(summary = "Update an existing item")
    @PutMapping
    public ResponseEntity<?> updateItem(@RequestBody ItemRequest request) {
        LOGGER.info("Updating item");
        return ResponseUtil.getObject(
                itemService.update(request),
                HttpStatus.OK,
                "Successfully updated item"
        );
    }

    @Operation(summary = "Delete an item by ID")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<?> deleteItem(@PathVariable Long itemId) {
        LOGGER.info("Deleting item");
        itemService.delete(itemId);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "Successfully deleted item"
        );
    }

    @Operation(summary = "Get items by category ID")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<?> getItemsByCategory(@PathVariable Long categoryId,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting items by category id: {}", categoryId);
        List<ItemResponse> result = itemService.getItemsByCategoryId(categoryId, page, limit);
        return ResponseUtil.getCollection(
                result,
                HttpStatus.OK,
                "Successfully retrieved items by category",
                new MetaDataDTO(page < result.size(), page > 1, limit, result.size(), page)
        );
    }

    @Operation(summary = "Get items by provider ID")
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<?> getItemsByProvider(@PathVariable Long providerId,
                                                @RequestParam(defaultValue = "1") int page,
                                                @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting items by provider id: {}", providerId);
        List<ItemResponse> result = itemService.getItemsByProviderId(providerId, page, limit);
        return ResponseUtil.getCollection(
                result,
                HttpStatus.OK,
                "Successfully retrieved items by provider",
                new MetaDataDTO(page < result.size(), page > 1, limit, result.size(), page)
        );
    }
}
