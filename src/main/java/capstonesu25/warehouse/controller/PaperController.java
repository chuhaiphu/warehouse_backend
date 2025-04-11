package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.paper.PaperRequest;
import capstonesu25.warehouse.model.paper.PaperResponse;
import capstonesu25.warehouse.model.responsedto.MetaDataDTO;
import capstonesu25.warehouse.service.PaperService;
import capstonesu25.warehouse.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/paper")
@RequiredArgsConstructor
@Validated
public class PaperController {
    private final PaperService paperService;

    private static final Logger LOGGER = LoggerFactory.getLogger(PaperController.class);

    @Operation(summary = "Get all papers with pagination")
    @GetMapping("")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getAll(@RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting all papers");
        Page<PaperResponse> result = paperService.getListPaper(page, limit);
        return ResponseUtil.getCollection(
                result.getContent(),
                HttpStatus.OK,
                "Successfully get all papers with pagination",
                new MetaDataDTO(
                        result.hasNext(),
                        result.hasPrevious(),
                        limit,
                        (int) result.getTotalElements(),
                        page));
    }

    @Operation(summary = "Get paper by ID")
    @GetMapping("/{paperId}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getById(@PathVariable Long paperId) {
        LOGGER.info("Getting paper by id: {}", paperId);
        PaperResponse result = paperService.getPaperById(paperId);
        return ResponseUtil.getObject(
                result,
                HttpStatus.OK,
                "Successfully retrieved paper");
    }

    @Operation(summary = "Get papers by import order ID")
    @GetMapping("import-order/{importOrderId}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getByImportOrderId(@PathVariable Long importOrderId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting paper by import order id: {} ", importOrderId);
        List<PaperResponse> result = paperService.getListPaperByImportOrderId(importOrderId, page, limit);
        return ResponseUtil.getCollection(
                result,
                HttpStatus.OK,
                "Successfully retrieved paper by import order id",
                new MetaDataDTO(false, false, 0, result.size(), 1));
    }

    @Operation(summary = "Get papers by export request ID")
    @GetMapping("export-order/{exportRequestId}")
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> getByExportRequestId(@PathVariable Long exportRequestId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting paper by export request id: {} ", exportRequestId);
        List<PaperResponse> result = paperService.getListPaperByExportRequestId(exportRequestId, page, limit);
        return ResponseUtil.getCollection(
                result,
                HttpStatus.OK,
                "Successfully retrieved paper by export request id",
                new MetaDataDTO(false, false, 0, result.size(), 1));
    }

    @Operation(summary = "Create a new paper with file upload")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasRole('STAFF')")
    public ResponseEntity<?> createPaper(@ModelAttribute PaperRequest request) {
        LOGGER.info("Creating paper");
        try {
            paperService.createPaper(request);
        } catch (IOException e) {
            return ResponseUtil.error(
                    "Error",
                    "Failed to create paper",
                    HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return ResponseUtil.getObject(
                null,
                HttpStatus.CREATED,
                "Successfully created paper");
    }
}
