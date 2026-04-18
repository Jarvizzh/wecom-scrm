package com.wecom.scrm.service.yuewen;

import com.wecom.scrm.entity.yuewen.YuewenProduct;
import com.wecom.scrm.repository.yuewen.YuewenProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class YuewenProductService {

    private final YuewenProductRepository productRepository;
    private final YuewenSyncService syncService;

    public Page<YuewenProduct> getProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return productRepository.findAll(pageable);
    }

    public List<YuewenProduct> getActiveProducts() {
        return productRepository.findByStatus(1);
    }

    public YuewenProduct getProductById(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    public YuewenProduct saveProduct(YuewenProduct product) {
        boolean isNew = product.getId() == null;
        YuewenProduct saved;
        if (!isNew) {
            YuewenProduct existing = getProductById(product.getId());
            existing.setProductName(product.getProductName());
            existing.setWxAppId(product.getWxAppId());
            existing.setAppFlag(product.getAppFlag());
            existing.setStatus(product.getStatus());
            saved = productRepository.save(existing);
        } else {
            saved = productRepository.save(product);
        }

        try {Thread.sleep(200);} catch (InterruptedException ignored) {}

        // Trigger async one-year sync for new active products
        if (isNew && Integer.valueOf(1).equals(saved.getStatus())) {
            syncService.asyncSyncUsersForOneYear(saved.getAppFlag());
        }

        return saved;
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }
}
