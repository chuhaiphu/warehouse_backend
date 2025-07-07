package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.inventoryitem.ChangeInventoryItemOfExportDetailRequest;
import capstonesu25.warehouse.model.inventoryitem.InventoryItemRequest;
import capstonesu25.warehouse.model.inventoryitem.InventoryItemResponse;
import capstonesu25.warehouse.model.inventoryitem.UpdateInventoryLocationRequest;
import capstonesu25.warehouse.model.responsedto.MetaDataDTO;
import capstonesu25.warehouse.service.InventoryItemService;
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

import java.util.List;

@Controller
@RequestMapping("/inventory-item")
@RequiredArgsConstructor
@Validated
public class InventoryItemController {
	private final InventoryItemService inventoryItemService;
	private static final Logger LOGGER = LoggerFactory.getLogger(InventoryItemController.class);

	@Operation(summary = "Get all inventory items with pagination")
	@GetMapping
	public ResponseEntity<?> getAll(@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int limit) {
		LOGGER.info("Getting all inventory items");
		Page<InventoryItemResponse> result = inventoryItemService.getAllInventoryItems(page, limit);
		return ResponseUtil.getCollection(
				result.getContent(),
				HttpStatus.OK,
				"Successfully get all inventory items with pagination",
				new MetaDataDTO(
						result.hasNext(),
						result.hasPrevious(),
						limit,
						(int) result.getTotalElements(),
						page));
	}

	@Operation(summary = "Get all inventory items that need to return by item ID which not have export detail")
	@GetMapping("/need-to-return")
	public ResponseEntity<?> getAllNeedToReturnByItemId(@RequestParam String itemId,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int limit) {
		LOGGER.info("Getting all inventory items that need to return by item id: {}", itemId);
		Page<InventoryItemResponse> result = inventoryItemService.getAllNeedToReturnByItemId(itemId, page, limit);
		return ResponseUtil.getCollection(
				result.getContent(),
				HttpStatus.OK,
				"Successfully get all inventory items that need to return by item ID",
				new MetaDataDTO(
						result.hasNext(),
						result.hasPrevious(),
						limit,
						(int) result.getTotalElements(),
						page));
	}

	@Operation(summary = "Get inventory item by ID")
	@GetMapping("/{inventoryItemId}")
	
	public ResponseEntity<?> getById(@PathVariable String inventoryItemId) {
		LOGGER.info("Getting inventory item by id: {}", inventoryItemId);
		InventoryItemResponse result = inventoryItemService.getInventoryItemById(inventoryItemId);
		return ResponseUtil.getObject(
				result,
				HttpStatus.OK,
				"Successfully retrieved inventory item");
	}

	@Operation(summary = "Get inventory items by import order detail ID")
	@GetMapping("/import-order-detail/{importOrderDetailId}")
	public ResponseEntity<?> getByImportOrderDetailId(@PathVariable Long importOrderDetailId,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int limit) {
		LOGGER.info("Getting inventory items by import order detail id: {}", importOrderDetailId);
		Page<InventoryItemResponse> result = inventoryItemService.getByImportOrderDetailId(importOrderDetailId, page,
				limit);
		return ResponseUtil.getCollection(
				result.getContent(),
				HttpStatus.OK,
				"Successfully get inventory items by import order detail ID",
				new MetaDataDTO(
						result.hasNext(),
						result.hasPrevious(),
						limit,
						(int) result.getTotalElements(),
						page));
	}

	@Operation(summary = "Get inventory items by list import order detail IDs")
	@PostMapping("/import-order-detail")
	public ResponseEntity<?> getByListImportOrderDetailIds(@RequestBody List<Long> importOrderDetailIds) {
		LOGGER.info("Getting inventory items by list import order detail ids: {}", importOrderDetailIds);
		List<InventoryItemResponse> result = inventoryItemService.getByListImportOrderDetailIds(importOrderDetailIds);
		return ResponseUtil.getCollection(result, HttpStatus.OK, "Successfully get inventory items by list import order detail IDs", null);
	}

	@Operation(summary = "Get inventory items by export request detail ID")
	@GetMapping("/export-request-detail/{exportRequestDetailId}")
	public ResponseEntity<?> getByExportRequestDetailId(@PathVariable Long exportRequestDetailId,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int limit) {
		LOGGER.info("Getting inventory items by export request detail id: {}", exportRequestDetailId);
		Page<InventoryItemResponse> result = inventoryItemService.getByExportRequestDetailId(exportRequestDetailId,
				page, limit);
		return ResponseUtil.getCollection(
				result.getContent(),
				HttpStatus.OK,
				"Successfully get inventory items by export request detail ID",
				new MetaDataDTO(
						result.hasNext(),
						result.hasPrevious(),
						limit,
						(int) result.getTotalElements(),
						page));
	}

	@Operation(summary = "Get inventory items by stored location ID")
	@GetMapping("/stored-location/{storedLocationId}")
	public ResponseEntity<?> getByStoredLocationId(@PathVariable Long storedLocationId,
			@RequestParam(defaultValue = "1") int page,
			@RequestParam(defaultValue = "10") int limit) {
		LOGGER.info("Getting inventory items by stored location id: {}", storedLocationId);
		Page<InventoryItemResponse> result = inventoryItemService.getByStoredLocationId(storedLocationId, page, limit);
		return ResponseUtil.getCollection(
				result.getContent(),
				HttpStatus.OK,
				"Successfully get inventory items by stored location ID",
				new MetaDataDTO(
						result.hasNext(),
						result.hasPrevious(),
						limit,
						(int) result.getTotalElements(),
						page));
	}

	@Operation(summary = "Get QR codes by inventory item IDs")
	@PostMapping("/qr-codes")
	public ResponseEntity<?> getListQrCodes(@RequestBody List<String> inventoryItemIds) {
		LOGGER.info("Getting QR codes by inventory item IDs");
		List<InventoryItemResponse> result = inventoryItemService.getListQrCodes(inventoryItemIds);
		return ResponseUtil.getCollection(
				result,
				HttpStatus.OK,
				"Successfully retrieved QR codes",
				null);
	}

	@Operation(summary = "Update stored location of inventory items")
	@PutMapping("/update-location")
	public ResponseEntity<?> updateStoreLocationOfInventoryItem(@RequestBody List<UpdateInventoryLocationRequest> requests) {
		LOGGER.info("Updating stored location of inventory items");
		List<InventoryItemResponse> result = inventoryItemService.updateStoredLocation(requests);
		return ResponseUtil.getCollection(
				result,
				HttpStatus.OK,
				"Successfully updated stored location of inventory items",
				null);
	}

	@Operation(summary = "Update an existing inventory item")
	@PutMapping
	public ResponseEntity<?> updateInventoryItem(@RequestBody InventoryItemRequest request) {
		LOGGER.info("Updating inventory item");
		return ResponseUtil.getObject(
				inventoryItemService.update(request),
				HttpStatus.OK,
				"Successfully updated inventory item");
	}
	@Operation(summary = "Change inventory item of export request detail")
	@PostMapping("/change-inventory-item-export-detail")
	public ResponseEntity<?> changeInventoryItemOfExportDetail(@RequestBody ChangeInventoryItemOfExportDetailRequest request) {
		LOGGER.info("Changing export request detail of inventory item");
		InventoryItemResponse updatedItem = inventoryItemService.changeInventoryItemOfExportDetail(request);
		return ResponseUtil.getObject(
				updatedItem,
				HttpStatus.OK,
				"Successfully changed export request detail of inventory item");

	}

	@Operation(summary = "Auto change new suitable inventory item for export request detail")
	@PutMapping("/auto-change/{inventoryItemId}")
	public ResponseEntity<?> autoChangeNewSuitableInventoryItem(@PathVariable String inventoryItemId) {
		LOGGER.info("Auto choosing new suitable inventory item for export request detail with ID: {}", inventoryItemId);
		InventoryItemResponse result = inventoryItemService.autoChangeInventoryItem(inventoryItemId);
		return ResponseUtil.getObject(
				result,
				HttpStatus.OK,
				"Successfully auto-chosen new suitable inventory item");
	}

}
