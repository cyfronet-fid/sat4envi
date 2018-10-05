package pl.cyfronet.s4e.granules;

import lombok.val;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.ex.NotFoundException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class GranuleService {
    private static final LocalDateTime BASE_TIME = LocalDateTime.of(2018, 10, 4, 8, 0);

    public List<Granule> getGranules(Long productId) throws NotFoundException {
        if (productId != 1L && productId != 2L) {
            throw new NotFoundException();
        }
        val out = new ArrayList<Granule>();
        val count = 4;
        for (int i = 3; i >= 0; i--) {
            LocalDateTime timestamp = BASE_TIME.plusMinutes(15*i);
            out.add(Granule.builder()
                    .id(productId*count+i)
                    .productId(productId)
                    .timestamp(timestamp)
                    .layerName("test:"+ DateTimeFormatter.ofPattern("yyyyMMddHHmm").format(timestamp) +"_Merkator_Europa_ir_108m")
                    .build());
        }
        return out;
    }
}
