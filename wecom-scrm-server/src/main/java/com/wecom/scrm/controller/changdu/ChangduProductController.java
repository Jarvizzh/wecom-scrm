package com.wecom.scrm.controller.changdu;

import com.wecom.scrm.entity.changdu.ChangduProduct;
import com.wecom.scrm.service.changdu.ChangduProductService;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/changdu/products")
public class ChangduProductController {

    private final ChangduProductService productService;

    public ChangduProductController(ChangduProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public Page<ChangduProduct> getProducts(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return productService.getProducts(page, size);
    }

    @PostMapping("/sync")
    public void syncProducts() {
        productService.syncProducts();
    }

    @PostMapping
    public ChangduProduct saveProduct(@RequestBody ChangduProduct product) {
        return productService.saveProduct(product);
    }

    @GetMapping("/{id}")
    public ChangduProduct getProduct(@PathVariable Long id) {
        return productService.getProduct(id);
    }

    @DeleteMapping("/{id}")
    public void deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
    }

    @PostMapping("/batch-status")
    public void batchUpdateStatus(@RequestBody BatchStatusRequest request) {
        productService.batchUpdateStatus(request.getIds(), request.getStatus());
    }

    @PostMapping("/batch-delete")
    public void batchDelete(@RequestBody List<Long> ids) {
        productService.batchDelete(ids);
    }

    @lombok.Data
    public static class BatchStatusRequest {
        private List<Long> ids;
        private Integer status;
    }
}
