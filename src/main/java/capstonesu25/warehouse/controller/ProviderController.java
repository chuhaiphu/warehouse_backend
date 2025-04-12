package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.provider.ProviderRequest;
import capstonesu25.warehouse.model.provider.ProviderResponse;
import capstonesu25.warehouse.model.responsedto.MetaDataDTO;
import capstonesu25.warehouse.service.ProviderService;
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
import org.springframework.security.access.prepost.PreAuthorize;


@Controller
@RequestMapping("/provider")
@RequiredArgsConstructor
@Validated
public class ProviderController {
    private final ProviderService providerService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderController.class);

    @Operation(summary = "Get all providers", description = "Returns a list of all providers with pagination")
    @GetMapping
    
    public ResponseEntity<?> getAll(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting all providers");
        Page<ProviderResponse> result = providerService.getAllProviders(page, limit);
        return ResponseUtil.getCollection(
                result.getContent(),
                HttpStatus.OK,
                "Successfully get all providers",
                new MetaDataDTO(
                        result.hasNext(),
                        result.hasPrevious(),
                        limit,
                        (int) result.getTotalElements(),
                        page
                )
        );
    }

    @Operation(summary = "Get provider by ID", description = "Returns a provider by its ID")
    @GetMapping("/{providerId}")
    
    public ResponseEntity<?> getById(@PathVariable Long providerId) {
        LOGGER.info("Getting provider by id: {}", providerId);
        ProviderResponse result = providerService.getProviderById(providerId);
        return ResponseUtil.getObject(
                result,
                HttpStatus.OK,
                "Successfully retrieved provider"
        );
    }

    @Operation(summary = "Create a new provider", description = "Creates a new provider in the system")
    @PostMapping
    
    public ResponseEntity<?> createProvider(@RequestBody ProviderRequest request) {
        LOGGER.info("Creating provider");
        providerService.create(request);
        return ResponseUtil.getObject(
                providerService.create(request),
                HttpStatus.CREATED,
                "Successfully created provider"
        );
    }

    @Operation(summary = "Update an existing provider", description = "Updates an existing provider's information")
    @PutMapping
    
    public ResponseEntity<?> updateProvider(@RequestBody ProviderRequest request) {
        LOGGER.info("Updating provider");
        return ResponseUtil.getObject(
                providerService.update(request),
                HttpStatus.OK,
                "Successfully updated provider"
        );
    }

    @Operation(summary = "Delete a provider by ID", description = "Removes a provider from the system")
    @DeleteMapping("/{providerId}")
    public ResponseEntity<?> deleteProvider(@PathVariable Long providerId) {
        LOGGER.info("Deleting provider");
        providerService.delete(providerId);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "Successfully deleted provider"
        );
    }
}
