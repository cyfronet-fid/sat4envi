package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.controller.response.AppUserResponse;
import pl.cyfronet.s4e.controller.response.OverlayResponse;
import pl.cyfronet.s4e.data.repository.InstitutionRepository;
import pl.cyfronet.s4e.data.repository.WMSOverlayRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OverlayService {
    private final WMSOverlayRepository wmsOverlayRepository;
    private final InstitutionRepository institutionRepository;

    @Transactional(readOnly = true)
    public List<OverlayResponse> findAllGlobalBy(String geoserverUrl) {
        return allWmsToResponses(
                wmsOverlayRepository
                        .findAllByOwnerType(OverlayOwner.GLOBAL, WMSOverlay.class),
                geoserverUrl
        );
    }

    public List<OverlayResponse> findAllInstitutional(AppUserResponse user, String geoserverUrl) {
        return user.getRoles().stream()
                .filter(userRoleResponse -> userRoleResponse.getRole().equals(AppRole.GROUP_MEMBER))
                .map(userRoleResponse -> {
                    val institution = institutionRepository.findBySlug(userRoleResponse.getInstitutionSlug(), Institution.class).get();
                    return allWmsToResponses(
                            wmsOverlayRepository
                                    .findAllByOwnerTypeAndInstitutionId(OverlayOwner.INSTITUTIONAL, institution.getId(), WMSOverlay.class),
                            geoserverUrl
                    );
                })
                .flatMap(Collection::stream)
                .collect(Collectors.toList());
    }

    public List<OverlayResponse> findAllPersonalBy(AppUser appUser, String geoserverUrl) {
        return allWmsToResponses(
                wmsOverlayRepository
                        .findAllByOwnerTypeAndAppUserId(OverlayOwner.PERSONAL, appUser.getId(), WMSOverlay.class),
                geoserverUrl
        );
    }

    public <T> Optional<T> findByOwnerTypeAndIdAndInstitutionId(OverlayOwner owner, Long id, Long institutionId, Class<T> projection) {
        return wmsOverlayRepository.findByOwnerTypeAndIdAndInstitutionId(owner, id, institutionId, projection);
    }

    public <T> Optional<T> findByOwnerTypeAndIdAndUserId(OverlayOwner owner, Long id, AppUser appUser, Class<T> projection) {
        return wmsOverlayRepository.findByOwnerTypeAndIdAndAppUserId(owner, id, appUser.getId(), projection);
    }

    public <T> Optional<T> findByOwnerTypeAndId(OverlayOwner owner, Long id, Class<T> projection) {
        return wmsOverlayRepository.findByOwnerTypeAndId(owner, id, projection);
    }

    @Transactional
    public OverlayResponse save(WMSOverlay overlay) {
        return wmsToResponse(wmsOverlayRepository.save(overlay))
                .url(overlay.getUrl())
                .build();
    }
    @Transactional
    public void delete(WMSOverlay overlay) {
        wmsOverlayRepository.delete(overlay);
    }

    private List<OverlayResponse> allWmsToResponses(List<WMSOverlay> overlays, String geoserverUrl) {
        return overlays
                .stream()
                .map(wmsOverlay -> wmsToResponse(wmsOverlay)
                        .url(
                                wmsOverlay.getUrl().isEmpty()
                                        ? geoserverUrl + wmsOverlay.getLabel()
                                        : wmsOverlay.getUrl()
                        ).build()
                )
                .collect(Collectors.toList());
    }

    private OverlayResponse.OverlayResponseBuilder wmsToResponse(WMSOverlay overlay) {
        return OverlayResponse.builder()
                .id(overlay.getId())
                .label(overlay.getLabel())
                .visible(true)
                .createdAt(overlay.getCreatedAt())
                .ownerType(overlay.getOwnerType().toString());
    }
}
