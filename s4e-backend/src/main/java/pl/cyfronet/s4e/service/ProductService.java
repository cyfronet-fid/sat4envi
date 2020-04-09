package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.controller.response.BasicProductResponse;
import pl.cyfronet.s4e.controller.response.ProductResponse;
import pl.cyfronet.s4e.data.repository.AppUserRepository;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.security.AppUserDetails;
import pl.cyfronet.s4e.util.SecurityHelper;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final AppUserRepository appUserRepository;
    private final SecurityHelper securityHelper;

    public List<BasicProductResponse> getProducts() {
        return productRepository.findAllBy(BasicProductResponse.class);
    }

    public boolean isFavourite(Long productId) {
        AppUserDetails userDetails = securityHelper.getUserDetailsIfAvailable();
        if (userDetails != null) {
            return productRepository.isFavouriteByEmailAndProductId(userDetails.getUsername(), productId);
        }
        return false;
    }

    public Optional<ProductResponse> getProduct(Long id) {
        return productRepository.findById(id, ProductResponse.class);
    }

    @Transactional
    public void addFavourite(Long id, String username) throws NotFoundException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product with id '" + id + "' not found"));

        product.addFavourite(appUserRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("User with id '" + id + "' not found")));
    }

    @Transactional
    public void deleteFavourite(Long id, String username) throws NotFoundException {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Product with id '" + id + "' not found"));

        product.removeFavourite(appUserRepository.findByEmail(username)
                .orElseThrow(() -> new NotFoundException("User with id '" + id + "' not found")));
    }
}
