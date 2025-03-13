package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.importrequest.importrequestdetail.ImportRequestDetailRequest;
import capstonesu25.warehouse.model.responsedto.MetaDataDTO;
import capstonesu25.warehouse.service.ImportRequestDetailService;
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
@RequestMapping("/import-request-detail")
@RequiredArgsConstructor
@Validated
public class ImportRequestDetailController {
    private final ImportRequestDetailService service;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportRequestDetailController.class);

    @GetMapping("/page/{importRequestId}")
    public ResponseEntity<?> getImportRequestDetail(@PathVariable Long importRequestId
            ,@RequestParam(defaultValue = "1") int page
            ,@RequestParam(defaultValue = "10") int limit){
        LOGGER.info("Getting import request detail");
        var result = service.getImportRequestDetailsByImportRequestId(importRequestId, page, limit);
        return ResponseUtil.getCollection(
                result,
                HttpStatus.OK,
                "Successfully get import request detail",
                new MetaDataDTO(page < result.size(),page > 1, limit, result.size(), page)

        );
    }

    @GetMapping("/{importRequestDetailId}")
    public ResponseEntity<?> getImportRequestDetail(@PathVariable Long importRequestDetailId){
        LOGGER.info("Getting import request detail");
        return ResponseUtil.getObject(
                service.getImportRequestDetailById(importRequestDetailId),
                HttpStatus.OK,
                "Successfully get import request detail"
        );
    }

    @PostMapping("/{importRequestId}")
    public ResponseEntity<?> createImportRequest(@RequestPart MultipartFile file, @PathVariable Long importRequestId){
        LOGGER.info("Creating import request detail");
        service.createImportRequestDetail(file, importRequestId);
        return ResponseUtil.getObject(
                null,
                HttpStatus.CREATED,
                "Successfully created import request"
        );
    }

    @PutMapping("/{importRequestId}")
    public ResponseEntity<?> updateImportRequest(@RequestBody List<ImportRequestDetailRequest> requestList
            , @PathVariable Long importRequestId){
        LOGGER.info("Updating import request detail");
        service.updateImportRequestDetail(requestList, importRequestId);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "Successfully updated import request"
        );
    }

    @DeleteMapping("/{importRequestId}")
    public ResponseEntity<?> deleteImportRequest(@PathVariable Long importRequestId){
        LOGGER.info("Deleting import request detail");
        service.deleteImportRequestDetail(importRequestId);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "Successfully deleted import request"
        );
    }
}
