package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.model.item.ItemRequest;
import capstonesu25.warehouse.model.item.ItemResponse;
import capstonesu25.warehouse.repository.CategoryRepository;
import capstonesu25.warehouse.repository.ItemRepository;
import capstonesu25.warehouse.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemService.class);
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final ProviderRepository providerRepository;

    public Page<ItemResponse> getAllItems(int page, int limit) {
        LOGGER.info("Getting all items with page: {}, limit: {}", page, limit);
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Item> items = itemRepository.findAll(pageable);
        return items.map(this::mapToResponse);
    }

    public ItemResponse getItemById(Long itemId) {
        LOGGER.info("Getting item by id: {}", itemId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));
        return mapToResponse(item);
    }

    @Transactional
    public ItemResponse create(ItemRequest request) {
        LOGGER.info("Creating item: {}", request);
        Item item = new Item();
        item = mapToEntity(request, item);
        return mapToResponse(itemRepository.save(item));
    }

    @Transactional
    public ItemResponse update(ItemRequest request) {
        LOGGER.info("Updating item: {}", request);
        if (request.getId() == null) {
            throw new RuntimeException("Item ID must not be null for update operation");
        }

        Item existingItem = itemRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + request.getId()));

        Item updatedItem = mapToEntity(request,existingItem);

        return mapToResponse(itemRepository.save(updatedItem));
    }

    @Transactional
    public void delete(Long itemId) {
        LOGGER.info("Deleting item with id: {}", itemId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));
        item.getStoredLocations().forEach(storedLocation -> storedLocation.setItem(null));
        itemRepository.delete(item);
    }

    public Page<ItemResponse> getItemsByCategoryId(Long categoryId, int page, int limit) {
        LOGGER.info("Getting items by category id: {}, page: {}, limit: {}", categoryId, page, limit);
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Item> items = itemRepository.findByCategoryId(categoryId, pageable);
        return items.map(this::mapToResponse);
    }

    public Page<ItemResponse> getItemsByProviderId(Long providerId, int page, int limit) {
        LOGGER.info("Getting items by provider id: {}, page: {}, limit: {}", providerId, page, limit);
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Item> items = itemRepository.findByProviders_Id(providerId, pageable);
        return items.map(this::mapToResponse);
    }

    private ItemResponse mapToResponse(Item item) {
        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setName(item.getName());
        response.setDescription(item.getDescription());
        response.setMeasurementUnit(item.getMeasurementUnit());
        response.setTotalMeasurementValue(item.getTotalMeasurementValue());
        response.setUnitType(item.getUnitType());
        response.setDaysUntilDue(item.getDaysUntilDue());
        response.setMinimumStockQuantity(item.getMinimumStockQuantity());
        response.setMaximumStockQuantity(item.getMaximumStockQuantity());

        if (item.getCategory() != null) {
            response.setCategoryId(item.getCategory().getId());
        }

        if (!item.getProviders().isEmpty()) {
            response.setProviderIds(item.getProviders().stream()
                    .map(Provider::getId)
                    .collect(Collectors.toList()));
        }

        // Convert OneToMany relationships to lists of IDs
        if (item.getImportOrderDetails() != null) {
            response.setImportOrderDetailIds(item.getImportOrderDetails().stream()
                    .map(ImportOrderDetail::getId)
                    .collect(Collectors.toList()));
        }

        if (item.getImportRequestDetails() != null) {
            response.setImportRequestDetailIds(item.getImportRequestDetails().stream()
                    .map(ImportRequestDetail::getId)
                    .collect(Collectors.toList()));
        }

        if (item.getExportRequestDetails() != null) {
            response.setExportRequestDetailIds(item.getExportRequestDetails().stream()
                    .map(ExportRequestDetail::getId)
                    .collect(Collectors.toList()));
        }

        if (item.getInventoryItems() != null) {
            List<Long> list = new ArrayList<>();
            for (InventoryItem inventoryItem : item.getInventoryItems()) {
                Long id = inventoryItem.getId();
                list.add(id);
            }
            response.setInventoryItemIds(list);
        }

        return response;
    }

    private Item mapToEntity(ItemRequest request, Item existingItem) {
        if (request == null) {
            throw new IllegalArgumentException("Item request must not be null");
        }
        existingItem.setName(request.getName());
        existingItem.setDescription(request.getDescription());
        existingItem.setMeasurementUnit(request.getMeasurementUnit());
        existingItem.setTotalMeasurementValue(request.getTotalMeasurementValue());
        existingItem.setUnitType(request.getUnitType());
        existingItem.setDaysUntilDue(request.getDaysUntilDue());
        existingItem.setMinimumStockQuantity(request.getMinimumStockQuantity());
        existingItem.setMaximumStockQuantity(request.getMaximumStockQuantity());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));
            existingItem.setCategory(category);
        }

        if (request.getProviderId() != null) {
            Provider provider = providerRepository.findById(request.getProviderId())
                    .orElseThrow(() -> new RuntimeException("Provider not found with id: " + request.getProviderId()));
            existingItem.setProviders(List.of(provider));
        }

        return existingItem;
    }

}
