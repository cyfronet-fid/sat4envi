package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.bean.ProductType;
import pl.cyfronet.s4e.data.repository.ProductTypeRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductTypeService {

    private final ProductTypeRepository repository;

    public List<ProductType> getProductTypes() {
        val out = new ArrayList<ProductType>();
        repository.findAll().forEach(out::add);
        return out;
    }

    public Optional<ProductType> getProductType(Long id){
        return repository.findById(id);
    }
}
