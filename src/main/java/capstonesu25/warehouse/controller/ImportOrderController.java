package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.importorder.ImportOrderRequest;
import capstonesu25.warehouse.model.importorder.ImportOrderResponse;
import capstonesu25.warehouse.model.responsedto.MetaDataDTO;
import capstonesu25.warehouse.service.ImportOrderService;
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
@RequestMapping("/import-order")
@RequiredArgsConstructor
@Validated
public class ImportOrderController {
    private final ImportOrderService importOrderService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportOrderController.class);

    @GetMapping("/page/{importRequestId}")
    public ResponseEntity<?> getAll(@PathVariable Long importRequestId, @RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int limit){
        LOGGER.info("Getting all import orders");
        List<ImportOrderResponse> result =  importOrderService.getImportOrdersByImportRequestId(importRequestId, page, limit);
        return ResponseUtil.getCollection(
                result,
                HttpStatus.OK,
                "Successfully retrieved all import orders",
                new MetaDataDTO(page < result.size(),page > 1, limit, result.size(), page)

        );
    }

    @GetMapping("/{importOrderId}")
    public ResponseEntity<?> getById(@PathVariable Long importOrderId){
        LOGGER.info("Getting import order by id");
        ImportOrderResponse result = importOrderService.getImportOrderById(importOrderId);
        return ResponseUtil.getObject(
                result,
                HttpStatus.OK,
                "Successfully retrieved import order"
        );
    }

    @PostMapping()
    public ResponseEntity<?> createImportOrder(@RequestBody ImportOrderRequest request){
        LOGGER.info("Creating import order");
        importOrderService.create(request);
        return ResponseUtil.getObject(
                null,
                HttpStatus.CREATED,
                "Successfully created import order"
        );
    }

    @PutMapping()
    public ResponseEntity<?> updateImportOrder(@RequestBody ImportOrderRequest request){
        LOGGER.info("Updating import order");
        importOrderService.create(request);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "Successfully updated import order"
        );
    }

    @DeleteMapping("/{importOrderId}")
    public ResponseEntity<?> deleteImportOrder(@PathVariable Long importOrderId){
        LOGGER.info("Deleting import order");
        importOrderService.delete(importOrderId);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "Successfully deleted import order"
        );
    }


}
