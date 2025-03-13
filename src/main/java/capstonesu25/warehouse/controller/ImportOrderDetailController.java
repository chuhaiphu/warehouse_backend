package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.importorder.importorderdetail.ImportOrderDetailRequest;
import capstonesu25.warehouse.model.responsedto.MetaDataDTO;
import capstonesu25.warehouse.service.ImportOrderDetailService;
import capstonesu25.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/import-order-detail")
@RequiredArgsConstructor
@Validated
public class ImportOrderDetailController {
    private final ImportOrderDetailService service;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportOrderDetailController.class);

    @GetMapping("/page/{importOrderId}")
    public ResponseEntity<?> getImportOrderDetail(@PathVariable Long importOrderId
            , @RequestParam(defaultValue = "1") int page
            , @RequestParam(defaultValue = "10") int limit){
        LOGGER.info("Getting import order detail");
        var result = service.getAllByImportOrderId(importOrderId, page, limit);
        return ResponseUtil.getCollection(
                result,
                HttpStatus.OK,
                "Successfully get import order detail",
                new MetaDataDTO(page < result.size(),page > 1, limit, result.size(), page)

        );
    }

    @GetMapping("/{importOrderId}")
    public ResponseEntity<?> getImportOrderDetail(@PathVariable Long importOrderId){
        LOGGER.info("Getting import order detail");
        var result = service.getById(importOrderId);
        return ResponseUtil.getCollection(
                result,
                HttpStatus.OK,
                "Successfully get import order detail",
                null

        );
    }

    @PostMapping("/{importOrderId}")
    public ResponseEntity<?> createImportOrderDetail(@RequestPart MultipartFile file
            , @PathVariable Long importOrderId){
        LOGGER.info("Creating import order detail");
        service.create(file, importOrderId);
        return ResponseUtil.getObject(
                null,
                HttpStatus.CREATED,
                "Successfully created import order"
        );
    }

    @PutMapping("/{importOrderId}")
    public ResponseEntity<?> updateImportOrderDetail(@RequestBody List<ImportOrderDetailRequest> requestList
            , @PathVariable Long importOrderId){
        LOGGER.info("Updating import order detail");
        service.updateImportOrderDetail(requestList, importOrderId);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "Successfully updated import order"
        );
    }

    @DeleteMapping("/{importOrderId}")
    public ResponseEntity<?> deleteImportOrderDetail(@PathVariable Long importOrderId){
        LOGGER.info("Deleting import order detail");
        service.delete(importOrderId);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "Successfully deleted import order"
        );
    }

}
