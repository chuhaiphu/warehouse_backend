package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.importrequest.importrequestdetail.ImportRequestDetailRequest;
import capstonesu25.warehouse.model.importrequest.importrequestdetail.ImportRequestDetailResponse;
import capstonesu25.warehouse.model.responsedto.MetaDataDTO;
import capstonesu25.warehouse.service.ImportRequestDetailService;
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
import org.springframework.web.multipart.MultipartFile;


@Controller
@RequestMapping("/import-request-detail")
@RequiredArgsConstructor
@Validated
public class ImportRequestDetailController {
    private final ImportRequestDetailService service;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportRequestDetailController.class);

    @Operation(summary = "Get paginated import request details by import request ID")
    @GetMapping("/page/{importRequestId}")
    public ResponseEntity<?> getImportRequestDetails(@PathVariable Long importRequestId
            ,@RequestParam(defaultValue = "1") int page
            ,@RequestParam(defaultValue = "10") int limit){
        LOGGER.info("Getting import request detail");
        Page<ImportRequestDetailResponse> result = service.getImportRequestDetailsByImportRequestId(importRequestId, page, limit);

        return ResponseUtil.getCollection(
                result.getContent(),
                HttpStatus.OK,
                "Successfully get import request detail",
                new MetaDataDTO(
                        result.hasNext(),
                        result.hasPrevious(),
                        limit,
                        (int) result.getTotalElements(),
                        page
                )
        );
    }

    @Operation(summary = "Get import request detail by ID")
    @GetMapping("/{importRequestDetailId}")
    public ResponseEntity<?> getImportRequestDetail(@PathVariable Long importRequestDetailId){
        LOGGER.info("Getting import request detail");
        return ResponseUtil.getObject(
                service.getImportRequestDetailById(importRequestDetailId),
                HttpStatus.OK,
                "Successfully get import request detail"
        );
    }

    @Operation(summary = "Create import request details from file upload")
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

    @Operation(summary = "Update import request details")
    @PutMapping("/{importRequestDetailId}")
    public ResponseEntity<?> updateImportRequest(@RequestBody ImportRequestDetailRequest importRequestDetailRequest
            , @PathVariable Long importRequestDetailId){
        LOGGER.info("Updating import request detail");
        service.updateImportRequestDetail(importRequestDetailRequest, importRequestDetailId);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "Successfully updated import request"
        );
    }

    @Operation(summary = "Delete import request details by import request ID")
    @DeleteMapping("/{importRequestDetailId}")
    public ResponseEntity<?> deleteImportRequest(@PathVariable Long importRequestDetailId){
        LOGGER.info("Deleting import request detail");
        service.deleteImportRequestDetail(importRequestDetailId);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "Successfully deleted import request"
        );
    }
}
