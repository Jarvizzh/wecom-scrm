package com.wecom.scrm.controller.yuewen;

import com.wecom.scrm.thirdparty.api.yuewen.dto.YuewenSyncRequest;
import com.wecom.scrm.entity.yuewen.YuewenProduct;
import com.wecom.scrm.service.yuewen.YuewenProductService;
import com.wecom.scrm.service.yuewen.YuewenSyncService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/yuewen/product")
@RequiredArgsConstructor
public class YuewenProductController {

    private final YuewenProductService productService;
    private final YuewenSyncService syncService;

    @PostMapping("/sync")
    public ResponseEntity<String> manualSync(@RequestBody YuewenSyncRequest request) {
        syncService.manualSync(request.getAppFlag(), request.getStartTime(), request.getEndTime());
        return ResponseEntity.ok("Sync task started successfully");
    }

    @GetMapping
    public Page<YuewenProduct> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return productService.getProducts(page, size);
    }

    @GetMapping("/{id}")
    public YuewenProduct get(@PathVariable Long id) {
        return productService.getProductById(id);
    }

    @PostMapping
    public YuewenProduct save(@RequestBody YuewenProduct product) {
        return productService.saveProduct(product);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.ok().build();
    }
}
