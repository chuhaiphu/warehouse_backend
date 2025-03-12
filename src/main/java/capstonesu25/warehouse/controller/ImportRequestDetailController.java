package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.service.ImportRequestDetailService;
import capstonesu25.warehouse.utils.ResponseUtil;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@Controller
@RequestMapping("/import-request-detail")
@RequiredArgsConstructor
@Validated
public class ImportRequestDetailController {
    private final ImportRequestDetailService service;
    private static final Logger LOGGER = LoggerFactory.getLogger(ImportRequestDetailController.class);

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
}
