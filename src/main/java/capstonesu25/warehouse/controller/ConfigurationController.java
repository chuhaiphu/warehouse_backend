package capstonesu25.warehouse.controller;

import capstonesu25.warehouse.model.configuration.ConfigurationDto;
import capstonesu25.warehouse.service.ConfigurationService;
import capstonesu25.warehouse.utils.ResponseUtil;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/configuration")
@RequiredArgsConstructor
@Validated
public class ConfigurationController {
    private final ConfigurationService configurationService;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigurationController.class);
    @Operation(summary = "Get all configurations")
    @GetMapping()
    public ResponseEntity<?> getAll() {
        logger.info("Fetching all configurations");
        return ResponseUtil.getObject(
                configurationService.getConfiguration(),
                HttpStatus.OK,
                "Fetch all configurations successfully"
        );
    }

    @Operation(summary = "Save configurations")
    @PostMapping("/save")
    public ResponseEntity<?> save(@RequestBody ConfigurationDto configurationDto) {
        logger.info("Saving configurations");
        return ResponseUtil.getObject(
                configurationService.saveConfiguration(configurationDto),
                HttpStatus.OK,
                "Save configurations successfully"
        );
    }
}
