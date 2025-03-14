package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.item.ItemRequest;
import capstonesu25.warehouse.model.item.ItemResponse;
import capstonesu25.warehouse.model.responsedto.MetaDataDTO;
import capstonesu25.warehouse.service.ItemService;
import capstonesu25.warehouse.utils.ResponseUtil;
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

    @PostMapping
    public ResponseEntity<?> createItem(@RequestBody ItemRequest request) {
        LOGGER.info("Creating item");
        itemService.create(request);
        return ResponseUtil.getObject(
                null,
                HttpStatus.CREATED,
                "Successfully created item"
        );
    }

    @PutMapping
    public ResponseEntity<?> updateItem(@RequestBody ItemRequest request) {
        LOGGER.info("Updating item");
        itemService.update(request);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "Successfully updated item"
        );
    }

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
