package pl.cyfronet.s4e.service;

import lombok.RequiredArgsConstructor;
import lombok.val;
import org.springframework.stereotype.Service;
import pl.cyfronet.s4e.bean.Product;
import pl.cyfronet.s4e.bean.ProductType;
import pl.cyfronet.s4e.bean.Webhook;
import pl.cyfronet.s4e.data.repository.ProductRepository;
import pl.cyfronet.s4e.data.repository.ProductTypeRepository;
import pl.cyfronet.s4e.ex.NotFoundException;
import pl.cyfronet.s4e.util.S3Util;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository productRepository;
    private final ProductTypeRepository productTypeRepository;
    private final S3Util s3Util;

    public List<Product> getProducts(Long productTypeId) {
        return productRepository.findByProductTypeId(productTypeId);
    }

    public List<Product> getProducts(Long productTypeId, LocalDateTime start, LocalDateTime end) {
        return productRepository.findAllByProductTypeIdAndTimestampGreaterThanEqualAndTimestampLessThanOrderByTimestampAsc(
                productTypeId, start, end
        );
    }

    public void saveProduct(Product product) {
        productRepository.save(product);
    }

    public Product buildFromWebhook(Webhook webhook) throws NotFoundException {
        return Product.builder().productType(getProductType(webhook.getKey()))
                .layerName(s3Util.getLayerName(webhook.getKey()))
                .timestamp(s3Util.getTimeStamp(webhook.getKey()))
                .s3Path(s3Util.getS3Path(webhook.getKey())).build();
    }

    public ProductType getProductType(String key) throws NotFoundException {
        return productTypeRepository.findByNameContainingIgnoreCase(s3Util.getProductType(key)).orElseThrow(() -> new NotFoundException());
    }

    public List<LocalDate> getAvailabilityDates(Long productTypeId, YearMonth yearMonth) {
        val start = LocalDateTime.of(yearMonth.getYear(), yearMonth.getMonth(), 1, 0, 0);
        return productRepository.findDatesWithData(productTypeId, start, start.plusMonths(1)).stream()
                .map(Date::toLocalDate)
                .collect(Collectors.toList());
    }
}
