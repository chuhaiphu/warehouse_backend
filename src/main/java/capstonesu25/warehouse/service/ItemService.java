package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.*;
import capstonesu25.warehouse.entity.pk.ItemProviderPK;
import capstonesu25.warehouse.enums.ItemStatus;
import capstonesu25.warehouse.model.item.ImExNumberItem;
import capstonesu25.warehouse.model.item.ItemFigure;
import capstonesu25.warehouse.model.item.ItemRequest;
import capstonesu25.warehouse.model.item.ItemResponse;
import capstonesu25.warehouse.repository.CategoryRepository;
import capstonesu25.warehouse.repository.ExportRequestRepository;
import capstonesu25.warehouse.repository.ImportOrderRepository;
import capstonesu25.warehouse.repository.ItemRepository;
import capstonesu25.warehouse.repository.ProviderRepository;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
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
    private final ImportOrderRepository importOrderRepository;
    private final ExportRequestRepository exportRequestRepository;

    public Page<ItemResponse> getAllItems(int page, int limit) {
        LOGGER.info("Getting all items with page: {}, limit: {}", page, limit);
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Item> items = itemRepository.findAll(pageable);
        return items.map(this::mapToResponse);
    }

    public ItemResponse getItemById(String itemId) {
        LOGGER.info("Getting item by id: {}", itemId);
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found with id: " + itemId));
        return mapToResponse(item);
    }

    @Transactional
    public ItemResponse create(ItemRequest request) {
        LOGGER.info("Creating item: {}", request);
        if (request.getId() == null || request.getId().trim().isEmpty()) {
            throw new RuntimeException("Item ID must not be null or empty for create operation");
        }
        Item item = new Item();
        item.setId(request.getId());
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

    public Page<ItemResponse> getItemsByCategoryId(Long categoryId, int page, int limit) {
        LOGGER.info("Getting items by category id: {}, page: {}, limit: {}", categoryId, page, limit);
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Item> items = itemRepository.findByCategoryId(categoryId, pageable);
        return items.map(this::mapToResponse);
    }

    public Page<ItemResponse> getItemsByProviderId(Long providerId, int page, int limit) {
        LOGGER.info("Getting items by provider id: {}, page: {}, limit: {}", providerId, page, limit);
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Item> items = itemRepository.findByItemProviders_Provider_Id(providerId, pageable);
        return items.map(this::mapToResponse);
    }

    public ItemFigure getItemFigures() {
        LOGGER.info("Getting item figures");
        List<Item> items = itemRepository.findAll();
        int totalItems = items.size();
        ItemFigure itemFigure = new ItemFigure(0, 0 );
         for(Item item : items) {
             if(item.getTotalMeasurementValue() == null || item.getTotalMeasurementValue() == 0
             || item.getTotalMeasurementValue() < item.getMeasurementValue()* item.getMinimumStockQuantity()) {
                 itemFigure.setTotalOutOfStock(itemFigure.getTotalOutOfStock() + 1);
             }
             if(item.getTotalMeasurementValue() > item.getMeasurementValue()* item.getMinimumStockQuantity()
                     && item.getTotalMeasurementValue() < item.getMeasurementValue()* item.getMaximumStockQuantity()) {
                 itemFigure.setTotalInStock(itemFigure.getTotalInStock() + 1);
             }
        }

        return itemFigure;
    }

    public ImExNumberItem getImEXNumberItem(String itemId, LocalDate fromDate, LocalDate toDate) {
        LOGGER.info("getting im ex number");

        List<ImportOrder> importOrders = Optional.ofNullable(importOrderRepository.findAll())
                .orElse(Collections.emptyList());

        importOrders =   importOrders.stream()
                .filter(importOrder ->
                        importOrder.getCreatedDate() != null &&
                                importOrder.getCreatedDate().isAfter(fromDate.atStartOfDay()) &&
                                importOrder.getCreatedDate().isBefore(toDate.atStartOfDay())
                )
                .toList();

        List<ExportRequest> exportRequests = Optional.ofNullable(exportRequestRepository.findAll())
                .orElse(Collections.emptyList());

        exportRequests = exportRequests.stream()
                .filter(importOrder ->
                        importOrder.getCreatedDate() != null &&
                                importOrder.getCreatedDate().isAfter(fromDate.atStartOfDay()) &&
                                importOrder.getCreatedDate().isBefore(toDate.atStartOfDay())
                )
                .toList();

        // helper: trả về list details an toàn
        Function<ImportOrder, List<ImportOrderDetail>> safeImportDetails =
                io -> Optional.ofNullable(io)
                        .map(ImportOrder::getImportOrderDetails)
                        .orElse(Collections.emptyList());

        Function<ExportRequest, List<ExportRequestDetail>> safeExportDetails =
                er -> Optional.ofNullable(er)
                        .map(ExportRequest::getExportRequestDetails)
                        .orElse(Collections.emptyList());

        Predicate<ImportOrderDetail> matchItemImport =
                d -> d != null && d.getItem() != null && itemId.equals(d.getItem().getId());

        Predicate<ExportRequestDetail> matchItemExport =
                d -> d != null && d.getItem() != null && itemId.equals(d.getItem().getId());

        // Lọc orders/requests có ít nhất 1 detail khớp itemId
        importOrders = importOrders.stream()
                .filter(Objects::nonNull)
                .filter(io -> safeImportDetails.apply(io).stream().anyMatch(matchItemImport))
                .toList();

        exportRequests = exportRequests.stream()
                .filter(Objects::nonNull)
                .filter(er -> safeExportDetails.apply(er).stream().anyMatch(matchItemExport))
                .toList();

        // Tổng measurement input (null -> 0)
        double measurementInput = importOrders.stream()
                .flatMap(io -> safeImportDetails.apply(io).stream())
                .filter(matchItemImport)
                .mapToDouble(d -> {
                    Double v = d.getActualMeasurementValue(); // nếu bạn muốn dùng measurement khác thì đổi getter tại đây
                    return v == null ? 0d : v.doubleValue();
                })
                .sum();

        // Tổng measurement output (null -> 0)
        double measurementOutput = exportRequests.stream()
                .flatMap(er -> safeExportDetails.apply(er).stream())
                .filter(matchItemExport)
                .mapToDouble(d -> {
                    Double v = d.getActualMeasurementValue();
                    return v == null ? 0d : v.doubleValue();
                })
                .sum();

        ImExNumberItem result = new ImExNumberItem();
        result.setImportMeasurementValue(measurementInput);
        result.setExportMeasurementValue(measurementOutput);
        result.setImportOrderIds(importOrders.stream().map(ImportOrder::getId).filter(Objects::nonNull).toList());
        result.setExportRequestIds(exportRequests.stream().map(ExportRequest::getId).filter(Objects::nonNull).toList());
        return result;
    }

    private ItemResponse mapToResponse(Item item) {
        ItemResponse response = new ItemResponse();
        response.setId(item.getId());
        response.setName(item.getName());
        response.setDescription(item.getDescription());
        response.setMeasurementUnit(item.getMeasurementUnit());
        response.setMeasurementValue(item.getMeasurementValue());
        response.setTotalMeasurementValue(item.getTotalMeasurementValue());
        response.setQuantity(item.getQuantity());
        response.setUnitType(item.getUnitType());
        response.setDaysUntilDue(item.getDaysUntilDue());
        response.setMinimumStockQuantity(item.getMinimumStockQuantity());
        response.setMaximumStockQuantity(item.getMaximumStockQuantity());
        response.setCountingMinutes(item.getCountingMinutes());

        if (item.getCategory() != null) {
            response.setCategoryId(item.getCategory().getId());
        }

        if(item.getItemProviders() != null) {
            response.setProviderCode(
                    item.getItemProviders().stream()
                            .map(ItemProvider::getProviderCode)
                            .collect(Collectors.toList())
            );
        }

        if (!item.getItemProviders().isEmpty()) {
            response.setProviderIds(
                    item.getItemProviders().stream()
                            .map(ip -> ip.getProvider().getId())
                            .collect(Collectors.toList())
            );
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
            List<String> list = new ArrayList<>();
            for (InventoryItem inventoryItem : item.getInventoryItems()) {
                String id = inventoryItem.getId();
                list.add(id);
            }
            response.setInventoryItemIds(list);
            int availableCount = (int) item.getInventoryItems().stream()
                    .filter(i -> i.getStatus() == ItemStatus.AVAILABLE)
                    .count();

            double availableMeasurementValues = item.getInventoryItems().stream()
                    .filter(i -> i.getStatus() == ItemStatus.AVAILABLE)
                    .mapToDouble(InventoryItem::getMeasurementValue)
                    .sum();

            response.setNumberOfAvailableMeasurementValues(availableMeasurementValues);
            response.setNumberOfAvailableItems(availableCount);

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
        existingItem.setMeasurementValue(request.getMeasurementValue());
        existingItem.setTotalMeasurementValue(request.getTotalMeasurementValue());
        existingItem.setUnitType(request.getUnitType());
        existingItem.setDaysUntilDue(request.getDaysUntilDue());
        existingItem.setMinimumStockQuantity(request.getMinimumStockQuantity());
        existingItem.setMaximumStockQuantity(request.getMaximumStockQuantity());
        existingItem.setCountingMinutes(request.getCountingMinutes());

        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                    .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));
            existingItem.setCategory(category);
        }

        if (request.getProviderId() != null) {
            Provider provider = providerRepository.findById(request.getProviderId())
                    .orElseThrow(() -> new RuntimeException("Provider not found with id: " + request.getProviderId()));
            ItemProvider itemProvider = new ItemProvider();

            ItemProviderPK pk = new ItemProviderPK();
            pk.setItemId(existingItem.getId());
            pk.setProviderId(provider.getId());

            itemProvider.setId(pk);
            itemProvider.setItem(existingItem);
            itemProvider.setProvider(provider);
            itemProvider.setProviderCode(request.getProviderCode()); // nếu cần set providerCode

// thêm vào list
            existingItem.getItemProviders().add(itemProvider);
        }

        return existingItem;
    }

}
