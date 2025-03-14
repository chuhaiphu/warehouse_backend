package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.Category;
import capstonesu25.warehouse.entity.Item;
import capstonesu25.warehouse.entity.Provider;
import capstonesu25.warehouse.model.item.ItemRequest;
import capstonesu25.warehouse.model.item.ItemResponse;
import capstonesu25.warehouse.repository.CategoryRepository;
import capstonesu25.warehouse.repository.ItemRepository;
import capstonesu25.warehouse.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ItemService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ItemService.class);
    private final ItemRepository itemRepository;
    private final CategoryRepository categoryRepository;
    private final ProviderRepository providerRepository;

    public List<ItemResponse> getAllItems(int page, int limit) {
        LOGGER.info("Getting all items with page: {}, limit: {}", page, limit);
        Pageable pageable = PageRequest.of(page - 1, limit);
        List<Item> items = itemRepository.findAll(pageable).getContent();
        return items.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ItemResponse getItemById(Long itemId) {
        LOGGER.info("Getting item by id: {}", itemId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));
        return mapToResponse(item);
    }

    @Transactional
    public void create(ItemRequest request) {
        LOGGER.info("Creating item: {}", request);
        Item item = mapToEntity(request);
        itemRepository.save(item);
    }

    @Transactional
    public void update(ItemRequest request) {
        LOGGER.info("Updating item: {}", request);
        if (request.getId() == null) {
            throw new RuntimeException("Item ID must not be null for update operation");
        }

        Item existingItem = itemRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + request.getId()));

        Item updatedItem = mapToEntity(request);
        itemRepository.save(updatedItem);
    }

    @Transactional
    public void delete(Long itemId) {
        LOGGER.info("Deleting item with id: {}", itemId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));
        itemRepository.delete(item);
    }

    public List<ItemResponse> getItemsByCategoryId(Long categoryId, int page, int limit) {
        LOGGER.info("Getting items by category id: {}, page: {}, limit: {}", categoryId, page, limit);
        Pageable pageable = PageRequest.of(page - 1, limit);
        List<Item> items = itemRepository.findByCategoryId(categoryId, pageable);
        return items.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public List<ItemResponse> getItemsByProviderId(Long providerId, int page, int limit) {
        LOGGER.info("Getting items by provider id: {}, page: {}, limit: {}", providerId, page, limit);
        Pageable pageable = PageRequest.of(page - 1, limit);
        List<Item> items = itemRepository.findByProviderId(providerId, pageable);
        return items.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
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

        if (item.getProvider() != null) {
            response.setProviderId(item.getProvider().getId());
        }

        // Convert OneToMany relationships to lists of IDs
        if (item.getImportOrderDetails() != null) {
            response.setImportOrderDetailIds(item.getImportOrderDetails().stream()
                    .map(detail -> detail.getId())
                    .collect(Collectors.toList()));
        }

        if (item.getImportRequestDetails() != null) {
            response.setImportRequestDetailIds(item.getImportRequestDetails().stream()
                    .map(detail -> detail.getId())
                    .collect(Collectors.toList()));
        }

        if (item.getExportRequestDetails() != null) {
            response.setExportRequestDetailIds(item.getExportRequestDetails().stream()
                    .map(detail -> detail.getId())
                    .collect(Collectors.toList()));
        }

        if (item.getInventoryItems() != null) {
            response.setInventoryItemIds(item.getInventoryItems().stream()
                    .map(inventoryItem -> inventoryItem.getId())
                    .collect(Collectors.toList()));
        }

        return response;
    }

    private Item mapToEntity(ItemRequest request) {
        Item item = new Item();
        item.setId(request.getId());
        item.setName(request.getName());
        item.setDescription(request.getDescription());
        item.setMeasurementUnit(request.getMeasurementUnit());
        item.setTotalMeasurementValue(request.getTotalMeasurementValue());
        item.setUnitType(request.getUnitType());
        item.setDaysUntilDue(request.getDaysUntilDue());
        item.setMinimumStockQuantity(request.getMinimumStockQuantity());
        item.setMaximumStockQuantity(request.getMaximumStockQuantity());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));
            item.setCategory(category);
        }

        if (request.getProviderId() != null) {
            Provider provider = providerRepository.findById(request.getProviderId())
                    .orElseThrow(() -> new RuntimeException("Provider not found with id: " + request.getProviderId()));
            item.setProvider(provider);
        }

        return item;
    }
}
