package com.wecom.scrm.controller;

import lombok.extern.slf4j.Slf4j;
import me.chanjar.weixin.common.bean.result.WxMediaUploadResult;
import com.wecom.scrm.config.WxCpServiceManager;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/media")
public class MediaController {

    private final WxCpServiceManager wxCpServiceManager;

    public MediaController(WxCpServiceManager wxCpServiceManager) {
        this.wxCpServiceManager = wxCpServiceManager;
    }

    @PostMapping("/upload")
    public ResponseEntity<Map<String, String>> uploadMedia(@RequestParam("file") MultipartFile file) throws Exception {
        // Map file extension to Wx media type, here we assume "image" for MVP
        // wecom supports: image, voice, video, file
        String mediaType = "image";

        // WxJava requires a File object or an InputStream with filename
        // We'll use getInputStream

        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";
        // The provided snippet for `uploadAttachment` is syntactically incorrect.
        // Assuming the intent was to keep the original uploadAttachment call
        // and potentially add a pic_url to the response if applicable,
        // but the instruction and snippet are contradictory for this method.
        // The original uploadAttachment call is restored to maintain functionality.
        WxMediaUploadResult result = wxCpServiceManager.getWxCpService().getExternalContactService().uploadAttachment(
                mediaType, filename, 1, file.getInputStream());

        Map<String, String> response = new HashMap<>();
        response.put("media_id", result.getMediaId());
        response.put("url", result.getUrl()); // Could be null for temporary media

        return ResponseEntity.ok(response);
    }

    @PostMapping("/uploadImg")
    public ResponseEntity<Map<String, String>> uploadImg(@RequestParam("file") MultipartFile file) throws Exception {
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";

        // Create a temporary file because WxCpMediaService.uploadImg takes a File
        File tempFile = File.createTempFile("upload-", filename);
        try {
            file.transferTo(tempFile);
            String picUrl = wxCpServiceManager.getWxCpService().getMediaService().uploadImg(tempFile);

            Map<String, String> response = new HashMap<>();
            response.put("pic_url", picUrl);

            return ResponseEntity.ok(response);
        } finally {
            tempFile.delete();
        }
    }

    @PostMapping("/uploadTemp")
    public ResponseEntity<Map<String, String>> uploadTempMedia(@RequestParam("file") MultipartFile file) throws Exception {
        String mediaType = "image";
        String filename = file.getOriginalFilename() != null ? file.getOriginalFilename() : "image.jpg";

        File tempFile = File.createTempFile("upload-temp-", filename);
        try {
            file.transferTo(tempFile);
            WxMediaUploadResult result = wxCpServiceManager.getWxCpService().getMediaService().upload(mediaType, tempFile);

            Map<String, String> response = new HashMap<>();
            response.put("media_id", result.getMediaId());
            response.put("url", result.getUrl());

            return ResponseEntity.ok(response);
        } finally {
            tempFile.delete();
        }
    }

    @GetMapping("/image/{corpId}/{mediaId}")
    public ResponseEntity<byte[]> getImage(@PathVariable String corpId, @PathVariable String mediaId) {
        try {
            log.info("Downloading media image: corpId={}, mediaId={}", corpId, mediaId);
            WxCpServiceManager.setCurrentCorpId(corpId);
            File file = wxCpServiceManager.getWxCpService(corpId).getMediaService().download(mediaId);
            byte[] bytes = Files.readAllBytes(file.toPath());
            
            // Basic content type detection, WeCom images are usually jpg/png
            MediaType contentType = MediaType.IMAGE_JPEG;
            String fileName = file.getName().toLowerCase();
            if (fileName.endsWith(".png")) {
                contentType = MediaType.IMAGE_PNG;
            } else if (fileName.endsWith(".gif")) {
                contentType = MediaType.IMAGE_GIF;
            }

            return ResponseEntity.ok()
                    .contentType(contentType)
                    .body(bytes);
        } catch (Exception e) {
            log.error("Failed to download media image: " + mediaId, e);
            return ResponseEntity.notFound().build();
        } finally {
            WxCpServiceManager.clearCurrentCorpId();
        }
    }
}
