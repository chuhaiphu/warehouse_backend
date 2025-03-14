package capstonesu25.warehouse.service;

import capstonesu25.warehouse.entity.Provider;
import capstonesu25.warehouse.model.provider.ProviderRequest;
import capstonesu25.warehouse.model.provider.ProviderResponse;
import capstonesu25.warehouse.repository.ProviderRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProviderService {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProviderService.class);
    private final ProviderRepository providerRepository;

    public List<ProviderResponse> getAllProviders(int page, int limit) {
        LOGGER.info("Getting all providers with page: {}, limit: {}", page, limit);
        Pageable pageable = PageRequest.of(page - 1, limit);
        List<Provider> providers = providerRepository.findAll(pageable).getContent();
        return providers.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public ProviderResponse getProviderById(Long providerId) {
        LOGGER.info("Getting provider by id: {}", providerId);
        Provider provider = providerRepository.findById(providerId)
                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + providerId));
        return mapToResponse(provider);
    }

    @Transactional
    public void create(ProviderRequest request) {
        LOGGER.info("Creating provider: {}", request);
        Provider provider = mapToEntity(request);
        providerRepository.save(provider);
    }

    @Transactional
    public void update(ProviderRequest request) {
        LOGGER.info("Updating provider: {}", request);
        if (request.getId() == null) {
            throw new RuntimeException("Provider ID must not be null for update operation");
        }

        Provider existingProvider = providerRepository.findById(request.getId())
                .orElseThrow(() -> new RuntimeException("Provider not found with id: " + request.getId()));

        Provider updatedProvider = mapToEntity(request);
        providerRepository.save(updatedProvider);
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
                    .map(item -> item.getId())
                    .collect(Collectors.toList()));
        }

        // Convert OneToOne relationship with importRequest to an ID
        if (provider.getImportRequest() != null) {
            response.setImportRequestId(provider.getImportRequest().getId());
        }

        return response;
    }

    private Provider mapToEntity(ProviderRequest request) {
        Provider provider = new Provider();
        provider.setId(request.getId());
        provider.setName(request.getName());
        provider.setPhone(request.getPhone());
        provider.setAddress(request.getAddress());

        // Note: We don't set the items or importRequest here as they are managed
        // by their respective services. This avoids circular dependencies.
        // Those relationships will be managed when creating/updating items or import requests

        return provider;
    }
}
