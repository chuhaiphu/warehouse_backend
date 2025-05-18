package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.ImportRequest;
import capstonesu25.warehouse.entity.Item;
import capstonesu25.warehouse.entity.Provider;
import capstonesu25.warehouse.model.provider.ProviderRequest;
import capstonesu25.warehouse.model.provider.ProviderResponse;
import capstonesu25.warehouse.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProviderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderService.class);
    private final ProviderRepository providerRepository;

    public Page<ProviderResponse> getAllProviders(int page, int limit) {
        LOGGER.info("Getting all providers with page: {}, limit: {}", page, limit);
        Pageable pageable = PageRequest.of(page - 1, limit);
        Page<Provider> providers = providerRepository.findAll(pageable);
        return providers.map(this::mapToResponse);
    }

    public ProviderResponse getProviderById(Long providerId) {
        LOGGER.info("Getting provider by id: {}", providerId);
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + providerId));
        return mapToResponse(provider);
    }

    @Transactional
    public ProviderResponse create(ProviderRequest request) {
        LOGGER.info("Creating provider: {}", request);
        Provider provider = mapToEntity(request, null);
       return mapToResponse(providerRepository.save(provider));
    }

    @Transactional
    public ProviderResponse update(ProviderRequest request) {
        LOGGER.info("Updating provider: {}", request);
        if (request.getId() == null) {
            throw new RuntimeException("Provider ID must not be null for update operation");
        }

        Provider existingProvider = providerRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + request.getId()));

        Provider updatedProvider = mapToEntity(request, existingProvider);
        return mapToResponse(providerRepository.save(updatedProvider));
    }

    @Transactional
    public void delete(Long providerId) {
        LOGGER.info("Deleting provider with id: {}", providerId);
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + providerId));
        providerRepository.delete(provider);
    }

    private ProviderResponse mapToResponse(Provider provider) {
        ProviderResponse response = new ProviderResponse();
        response.setId(provider.getId());
        response.setName(provider.getName());
        response.setPhone(provider.getPhone());
        response.setAddress(provider.getAddress());

        // Convert OneToMany relationship with items to a list of IDs
        if (provider.getItems() != null) {
            response.setItemIds(provider.getItems().stream()
                    .map(Item::getId)
                    .collect(Collectors.toList()));
        }

        if (provider.getImportRequest() != null) {
            response.setImportRequestId(
                    provider.getImportRequest().stream()
                            .map(ImportRequest::getId)
                            .collect(Collectors.toList())
            );
        }


        return response;
    }

    private Provider mapToEntity(ProviderRequest request, Provider existingProvider) {
        if(existingProvider == null) {
            existingProvider = new Provider();
        }
        existingProvider.setName(request.getName());
        existingProvider.setPhone(request.getPhone());
        existingProvider.setAddress(request.getAddress());
        return existingProvider;
    }
}
