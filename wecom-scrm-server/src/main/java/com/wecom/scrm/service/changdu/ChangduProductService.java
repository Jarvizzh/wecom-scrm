package com.wecom.scrm.service.changdu;

import com.wecom.scrm.entity.changdu.ChangduProduct;
import com.wecom.scrm.repository.changdu.ChangduProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ChangduProductService {

    private final ChangduProductRepository productRepository;
    private final ChangduSyncService changduSyncService;

    public ChangduProductService(ChangduProductRepository productRepository, ChangduSyncService changduSyncService) {
        this.productRepository = productRepository;
        this.changduSyncService = changduSyncService;
    }

    public void syncProducts() {
        changduSyncService.syncProducts();
    }

    public Page<ChangduProduct> getProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createTime"));
        return productRepository.findAll(pageable);
    }

    @Transactional
    public ChangduProduct saveProduct(ChangduProduct product) {
        return productRepository.save(product);
    }

    @Transactional
    public void deleteProduct(Long id) {
        productRepository.deleteById(id);
    }

    public ChangduProduct getProduct(Long id) {
        return productRepository.findById(id).orElseThrow(() -> new RuntimeException("Product not found"));
    }

    @Transactional
    public void batchUpdateStatus(List<Long> ids, Integer status) {
        productRepository.batchUpdateStatus(ids, status);
    }

    @Transactional
    public void batchDelete(List<Long> ids) {
        productRepository.deleteAllByIdInBatch(ids);
    }
}
