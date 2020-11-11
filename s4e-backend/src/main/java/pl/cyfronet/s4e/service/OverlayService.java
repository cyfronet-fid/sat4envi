package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.*;
import pl.cyfronet.s4e.controller.response.OverlayResponse;
import pl.cyfronet.s4e.data.repository.WMSOverlayRepository;
import pl.cyfronet.s4e.properties.GeoServerProperties;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OverlayService {
    private final WMSOverlayRepository wmsOverlayRepository;
    private final GeoServerProperties geoServerProperties;

    public List<OverlayResponse> findAllGlobalByUser(AppUser user) {
        val overlays = wmsOverlayRepository
                .findAllByOwnerType(OverlayOwner.GLOBAL);
        val nonActiveIds = user.getPreferences().getNonVisibleOverlays();
        return mapToOverlaysResponses(overlays, nonActiveIds);
    }

    public List<OverlayResponse> findAllGlobal() {
        val overlays = wmsOverlayRepository
                .findAllByOwnerType(OverlayOwner.GLOBAL);
        List<Long> nonActiveIds = List.of();
        return mapToOverlaysResponses(overlays, nonActiveIds);
    }

    public List<OverlayResponse> findAllInstitutionalByUser(AppUser user) {
        val overlays = wmsOverlayRepository
                .findAllInstitutional(
                        user.getId(),
                        AppRole.INST_MEMBER,
                        OverlayOwner.INSTITUTIONAL
                );
        val nonActiveIds = user.getPreferences().getNonVisibleOverlays();
        return mapToOverlaysResponses(overlays, nonActiveIds);
    }

    public List<OverlayResponse> findAllPersonalByUser(AppUser appUser) {
        val overlays = wmsOverlayRepository
                .findAllPersonal(appUser.getId(), OverlayOwner.PERSONAL);
        val nonActiveIds = appUser.getPreferences().getNonVisibleOverlays();
        return mapToOverlaysResponses(overlays, nonActiveIds);
    }

    public <T> Optional<T> findByIdAndOwnerTypeAndInstitutionId(
            Long id,
            OverlayOwner owner,
            Institution institution,
            Class<T> projection
    ) {
        return wmsOverlayRepository
                .findByIdAndOwnerTypeAndInstitutionId(id, owner, institution.getId(), projection);
    }

    public <T> Optional<T> findByIdAndAppUserIdAndOwnerType(
            Long id,
            AppUser appUser,
            OverlayOwner owner,
            Class<T> projection
    ) {
        return wmsOverlayRepository
                .findByIdAndAppUserIdAndOwnerType(id, appUser.getId(), owner, projection);
    }

    @Transactional
    public void deleteByIdAndOwnerType(Long id, OverlayOwner owner) {
        wmsOverlayRepository.deleteByIdAndOwnerType(id, owner);
    }

    @Transactional
    public void delete(WMSOverlay overlay) {
        wmsOverlayRepository.delete(overlay);
    }

    @Transactional
    public OverlayResponse save(WMSOverlay overlay) {
        return toOverlayResponse(wmsOverlayRepository.save(overlay), List.of());
    }

    private List<OverlayResponse> mapToOverlaysResponses(List<WMSOverlay> overlays, List<Long> nonActiveIds) {
        return overlays.stream()
            .map(overlay -> toOverlayResponse(overlay, nonActiveIds))
            .collect(Collectors.toList());
    }

    private OverlayResponse toOverlayResponse(WMSOverlay overlay, List<Long> nonActiveIds) {
        return OverlayResponse.builder()
                .id(overlay.getId())
                .url(getOverlayUrlBy(overlay))
                .createdAt(overlay.getCreatedAt())
                .ownerType(overlay.getOwnerType().toString())
                .label(overlay.getLabel())
                .visible(!nonActiveIds.contains(overlay.getId()))
                .build();
    }

    private String getOverlayUrlBy(WMSOverlay overlay) {
        val layersParam = overlay.getLayerName() == null || overlay.getLayerName().isEmpty()
                ? ""
                : "?LAYERS=" + overlay.getLayerName();
        return overlay.getUrl().isEmpty()
                ? geoServerProperties.getOutsideBaseUrl() + layersParam
                : overlay.getUrl();
    }
}
