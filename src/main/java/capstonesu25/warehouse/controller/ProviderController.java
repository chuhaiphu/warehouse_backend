package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.provider.ProviderRequest;
import capstonesu25.warehouse.model.provider.ProviderResponse;
import capstonesu25.warehouse.model.responsedto.MetaDataDTO;
import capstonesu25.warehouse.service.ProviderService;
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
@RequestMapping("/providers")
@RequiredArgsConstructor
@Validated
public class ProviderController {
    private final ProviderService providerService;
    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderController.class);

    @GetMapping
    public ResponseEntity<?> getAll(@RequestParam(defaultValue = "1") int page,
                                    @RequestParam(defaultValue = "10") int limit) {
        LOGGER.info("Getting all providers");
        List<ProviderResponse> result = providerService.getAllProviders(page, limit);
        return ResponseUtil.getCollection(
                result,
                HttpStatus.OK,
                "Successfully retrieved all providers",
                new MetaDataDTO(page < result.size(), page > 1, limit, result.size(), page)
        );
    }

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

    @PostMapping
    public ResponseEntity<?> createProvider(@RequestBody ProviderRequest request) {
        LOGGER.info("Creating provider");
        providerService.create(request);
        return ResponseUtil.getObject(
                null,
                HttpStatus.CREATED,
                "Successfully created provider"
        );
    }

    @PutMapping
    public ResponseEntity<?> updateProvider(@RequestBody ProviderRequest request) {
        LOGGER.info("Updating provider");
        providerService.update(request);
        return ResponseUtil.getObject(
                null,
                HttpStatus.OK,
                "Successfully updated provider"
        );
    }

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
