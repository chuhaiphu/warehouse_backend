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
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/item")
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
        Page<ItemResponse> result = itemService.getAllItems(page, limit);
        return ResponseUtil.getCollection(
                result.getContent(),
                HttpStatus.OK,
                "Successfully get all items with pagination",
                new MetaDataDTO(
                        result.hasNext(),
                        result.hasPrevious(),
                        limit,
                        (int) result.getTotalElements(),
                        page));
    }

    @Operation(summary = "Get item by ID")
    @GetMapping("/{itemId}")
    public ResponseEntity<?> getById(@PathVariable String itemId) {
        LOGGER.info("Getting item by id: {}", itemId);
        ItemResponse result = itemService.getItemById(itemId);
        return ResponseUtil.getObject(
                result,
                HttpStatus.OK,
                "Successfully retrieved item");
    }

    @Operation(summary = "Get items by category ID")
    @GetMapping("/category/{categoryId}")
    
    public ResponseEntity<?> getItemsByCategory(@PathVariable Long categoryId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting items by category id: {}", categoryId);
        Page<ItemResponse> result = itemService.getItemsByCategoryId(categoryId, page, limit);
        return ResponseUtil.getCollection(
                result.getContent(),
                HttpStatus.OK,
                "Successfully get items by category ID",
                new MetaDataDTO(
                        result.hasNext(),
                        result.hasPrevious(),
                        limit,
                        (int) result.getTotalElements(),
                        page));
    }

    @Operation(summary = "Get items by provider ID")
    @GetMapping("/provider/{providerId}")
    public ResponseEntity<?> getItemsByProvider(@PathVariable Long providerId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting items by provider id: {}", providerId);
        Page<ItemResponse> result = itemService.getItemsByProviderId(providerId, page, limit);
        return ResponseUtil.getCollection(
                result.getContent(),
                HttpStatus.OK,
                "Successfully  items by provider ID",
                new MetaDataDTO(
                        result.hasNext(),
                        result.hasPrevious(),
                        limit,
                        (int) result.getTotalElements(),
                        page));
    }

    @Operation(summary = "get item figures")
    @GetMapping("/figures")
    public ResponseEntity<?> getItemFigures() {
        LOGGER.info("Getting item figures");
        return ResponseUtil.getObject(
                itemService.getItemFigures(),
                HttpStatus.OK,
                "Successfully retrieved item figures");
    }

    @Operation(summary = "get import-export number of item")
    @GetMapping("/im-ex/{itemId}")
    public ResponseEntity<?> getImExNumberOfItem(@PathVariable String itemId) {
        LOGGER.info("get im-ex number of item");
        return ResponseUtil.getObject(
                itemService.getImEXNumberItem(itemId),
                HttpStatus.OK,
                "successfully retrieved item im-ex number"
        );
    }

    @Operation(summary = "Create a new item")
    @PostMapping
    public ResponseEntity<?> createItem(@RequestBody ItemRequest request) {
        LOGGER.info("Creating item");
        return ResponseUtil.getObject(
                itemService.create(request),
                HttpStatus.CREATED,
                "Successfully created item");
    }

    @Operation(summary = "Update an existing item")
    @PutMapping
    
    public ResponseEntity<?> updateItem(@RequestBody ItemRequest request) {
        LOGGER.info("Updating item");
        return ResponseUtil.getObject(
                itemService.update(request),
                HttpStatus.OK,
                "Successfully updated item");
    }



}
