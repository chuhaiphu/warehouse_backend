package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.Configuration;
import capstonesu25.warehouse.entity.Item;
import capstonesu25.warehouse.model.configuration.ConfigurationDto;
import capstonesu25.warehouse.repository.ConfigurationRepository;
import capstonesu25.warehouse.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigurationService {
    private final ConfigurationRepository configurationRepository;
    private final ItemRepository itemRepository;
    private static final Logger logger = org.slf4j.LoggerFactory.getLogger(ConfigurationService.class);

    public ConfigurationDto getConfiguration() {
        logger.info("Fetching configuration");
        Configuration configuration = configurationRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Configuration not found"));
        return mapToDto(configuration);
    }

    public ConfigurationDto saveConfiguration(ConfigurationDto configurationDto) {
        logger.info("Updating configuration");
        Configuration configuration = configurationRepository.findById(configurationDto.getId())
                .orElseThrow(() -> new RuntimeException("Configuration not found"));
        logger.info("Configuration found, updating...");
        configuration.setItems(itemRepository.findAllById(configurationDto.getItemIds()));
        configuration.setWorkingTimeStart(configurationDto.getWorkingTimeStart());
        configuration.setWorkingTimeEnd(configurationDto.getWorkingTimeEnd());
        configuration.setCreateRequestTimeAtLeast(configurationDto.getCreateRequestTimeAtLeast());
        configuration.setTimeToAllowAssign(configurationDto.getTimeToAllowAssign());
        configuration.setTimeToAllowConfirm(configurationDto.getTimeToAllowConfirm());
        configuration.setTimeToAllowCancel(configurationDto.getTimeToAllowCancel());
        configuration.setDaysToAllowExtend(configurationDto.getDaysToAllowExtend());
        configuration.setMaxAllowedDaysForExtend(configurationDto.getMaxAllowedDaysForExtend());
        configuration.setMaxAllowedDaysForImportRequestProcess(configurationDto.getMaxAllowedDaysForImportRequestProcess());

        Configuration updatedConfiguration = configurationRepository.save(configuration);
        return mapToDto(updatedConfiguration);
    }

    private ConfigurationDto mapToDto(Configuration configuration) {
        return new ConfigurationDto(
                configuration.getId(),
                configuration.getItems()
                        .stream()
                        .map(Item::getId)
                        .toList(),
                configuration.getWorkingTimeStart(),
                configuration.getWorkingTimeEnd(),
                configuration.getCreateRequestTimeAtLeast(),
                configuration.getTimeToAllowAssign(),
                configuration.getTimeToAllowConfirm(),
                configuration.getTimeToAllowCancel(),
                configuration.getDaysToAllowExtend(),
                configuration.getMaxAllowedDaysForExtend(),
                configuration.getMaxAllowedDaysForImportRequestProcess(),
                configuration.getMaxDispatchErrorPercent(),
                configuration.getMaxPercentOfItemForExport()
        );
    }


}
