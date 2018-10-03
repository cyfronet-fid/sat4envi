package pl.cyfronet.s4e.granules;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.util.List;

import static pl.cyfronet.s4e.Constants.API_PREFIX;

@RestController
@RequestMapping(API_PREFIX)
@RequiredArgsConstructor
public class GranuleController {
    private final GranuleService granuleService;

    @GetMapping("/granules/productId/{productId}")
    public List<Granule> getGranules(@PathVariable Long productId) throws NotFoundException {
        return granuleService.getGranules(productId);
    }
}
