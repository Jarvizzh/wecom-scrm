package com.wecom.scrm.controller;

import com.wecom.scrm.entity.WecomMaterial;
import com.wecom.scrm.service.WecomMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/materials")
public class WecomMaterialController {

    private final WecomMaterialService materialService;

    public WecomMaterialController(WecomMaterialService materialService) {
        this.materialService = materialService;
    }

    @GetMapping
    public Page<WecomMaterial> getMaterials(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String sourceType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return materialService.getMaterials(type, title, sourceType, page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<WecomMaterial> getMaterial(@PathVariable Long id) {
        return materialService.getById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public WecomMaterial saveMaterial(@RequestBody WecomMaterial material) {
        return materialService.save(material);
    }

    @PostMapping("/batch")
    public List<WecomMaterial> saveBatch(@RequestBody List<WecomMaterial> materials) {
        return materialService.saveBatch(materials);
    }

    @DeleteMapping("/{id}")
    public void deleteMaterial(@PathVariable Long id) {
        materialService.delete(id);
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadLocal(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        try {
            // Get root execution folder 'uploads/materials'
            String userDir = System.getProperty("user.dir");
            File uploadDir = new File(userDir + File.separator + "uploads" + File.separator + "materials");
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            // Generate unique name
            String originalFilename = file.getOriginalFilename();
            String extension = ".jpg";
            if (originalFilename != null && originalFilename.lastIndexOf(".") > -1) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String newFilename = UUID.randomUUID().toString() + extension;

            // Save file
            File destinationFile = new File(uploadDir, newFilename);
            file.transferTo(destinationFile);

            log.info("Uploaded file locally: {}", destinationFile.getAbsolutePath());

            // Build relative access URL
            String relativeUrl = "/api/materials/local/" + newFilename;

            Map<String, String> response = new HashMap<>();
            response.put("url", relativeUrl);
            response.put("filename", newFilename);

            return ResponseEntity.ok(response);
        } catch (IOException e) {
            log.error("Failed to upload local file", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/local/{filename:.+}")
    public ResponseEntity<byte[]> serveLocalFile(@PathVariable String filename) {
        // Prevent path traversal
        if (filename.contains("..") || filename.contains("/") || filename.contains("\\")) {
            return ResponseEntity.badRequest().build();
        }

        try {
            String userDir = System.getProperty("user.dir");
            File file = new File(userDir + File.separator + "uploads" + File.separator + "materials", filename);

            if (!file.exists()) {
                log.warn("Local material file not found: {}", file.getAbsolutePath());
                return ResponseEntity.notFound().build();
            }

            byte[] bytes = Files.readAllBytes(file.toPath());

            MediaType contentType = MediaType.IMAGE_JPEG;
            String lowerName = filename.toLowerCase();
            if (lowerName.endsWith(".png")) {
                contentType = MediaType.IMAGE_PNG;
            } else if (lowerName.endsWith(".gif")) {
                contentType = MediaType.IMAGE_GIF;
            } else if (lowerName.endsWith(".webp")) {
                contentType = MediaType.parseMediaType("image/webp");
            }

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .body(bytes);
        } catch (IOException e) {
            log.error("Failed to serve local file " + filename, e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
