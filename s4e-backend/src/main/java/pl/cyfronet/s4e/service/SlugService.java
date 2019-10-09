package pl.cyfronet.s4e.service;

import com.github.slugify.Slugify;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SlugService {
    private final Slugify slugify;

    public String slugify(String text) {
        return slugify.slugify(text);
    }
}
