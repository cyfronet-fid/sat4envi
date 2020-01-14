package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.controller.response.PlaceResponse;
import pl.cyfronet.s4e.data.repository.PlaceRepository;

@Service
@RequiredArgsConstructor
@Slf4j
public class PlaceService {
    private final PlaceRepository placeRepository;

    public Page<PlaceResponse> findPlace(String name, Pageable pageable) {
        return placeRepository.findAllByNameIsStartingWithIgnoreCase(name, pageable);
    }
}
