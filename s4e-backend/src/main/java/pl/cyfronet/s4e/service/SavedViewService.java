package pl.cyfronet.s4e.service;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.val;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.properties.FileStorageProperties;
import pl.cyfronet.s4e.bean.SavedView;
import pl.cyfronet.s4e.controller.response.SavedViewResponse;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.SavedViewRepository;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SavedViewService {
    @Value
    @Builder
    public static class Create {
        String ownerEmail;
        LocalDateTime createdAt;
        String caption;
        byte[] thumbnail;
        Map<String, Object> configuration;
    }

    private final FileStorageProperties fileStorageProperties;
    private final SavedViewRepository savedViewRepository;
    private final AppUserRepository appUserRepository;
    private final FileStorage fileStorage;

    @Transactional(rollbackFor = NotFoundException.class)
    public UUID create(Create request) throws NotFoundException {
        val appUser = appUserRepository.findByEmail(request.getOwnerEmail())
                .orElseThrow(() -> new NotFoundException("AppUser with email '" + request.getOwnerEmail() + "' not found"));

        val savedView = savedViewRepository.save(SavedView.builder()
                .caption(request.getCaption())
                .configuration(request.getConfiguration())
                .createdAt(request.getCreatedAt())
                .owner(appUser)
                .build());

        fileStorage.upload(getThumbnailKey(savedView.getId()), request.getThumbnail());

        return savedView.getId();
    }

    @Transactional
    public void delete(UUID id) {
        savedViewRepository.deleteById(id);
        fileStorage.delete(getThumbnailKey(id));
    }

    public <T> Optional<T> findById(UUID id, Class<T> projection) {
        return savedViewRepository.findById(id, projection);
    }

    public Page<SavedViewResponse> listByAppUser(String email, Pageable pageable) {
        return savedViewRepository.findAllByOwnerEmail(email, pageable);
    }

    public String getThumbnailPath(UUID id) {
        return String.join("/", fileStorageProperties.getBucket(), getThumbnailKey(id));
    }

    public String getThumbnailKey(UUID id) {
        return fileStorageProperties.getKeyPrefix() + id;
    }

    private interface OwnerEmailProjection {
        String getEmail();
    }

    public boolean canDelete(UUID id, Authentication authentication) {
        String email = authentication.getName();
        if (email == null) {
            return false;
        }
        return savedViewRepository.findOwnerOf(id, OwnerEmailProjection.class)
                .map(oep -> email.equals(oep.getEmail()))
                .orElse(false);
    }
}
