package com.bookhair.backend.controllers;

import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import com.bookhair.backend.services.UserPhotoService;
import com.bookhair.dto.UploadPhotoResponse;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/photosUser")
public class UserPhotoController {

    private final UserPhotoService userPhotoService;

    // ------- UPLOAD/UPDATE: devolvem SEMPRE JSON (url + etag + hasPhoto) -------

    @PostMapping(value = "/user/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadPhotoResponse> uploadUserPhoto(
            @PathVariable("id") String userId,
            @RequestParam("file") MultipartFile file) throws Exception {
        var resp = userPhotoService.upload(userId, file, true, "user");
        return ResponseEntity.ok(resp);
    }

    @PutMapping(value = "/user/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadPhotoResponse> updateUserPhoto(
            @PathVariable("id") String userId,
            @RequestParam("file") MultipartFile file) throws Exception {
        var resp = userPhotoService.upload(userId, file, false, "user");
        return ResponseEntity.ok(resp);
    }

    @PostMapping(value = "/service/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadPhotoResponse> uploadServicePhoto(
            @PathVariable("id") String serviceId,
            @RequestParam("file") MultipartFile file) throws Exception {
        var resp = userPhotoService.upload(serviceId, file, false, "service");
        return ResponseEntity.ok(resp);
    }

    @PutMapping(value = "/service/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<UploadPhotoResponse> updateServicePhoto(
            @PathVariable("id") String serviceId,
            @RequestParam("file") MultipartFile file) throws Exception {
        var resp = userPhotoService.upload(serviceId, file, false, "service");
        return ResponseEntity.ok(resp);
    }

    // ------- GET: com ETag + 304 + Cache-Control -------

    @GetMapping(value = "/{id}")
    public ResponseEntity<byte[]> getPhoto(
            @PathVariable("id") String userId,
            @RequestHeader(name = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {

        var p = userPhotoService.get(userId);
        if (p.etag() != null && p.etag().equals(ifNoneMatch)) {
            return ResponseEntity.status(HttpStatus.NOT_MODIFIED)
                    .eTag(p.etag())
                    .build();
        }
        return ResponseEntity.ok()
                .headers(p.toHeaders())
                .body(p.bytes());
    }

    // ------- DELETE -------

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable("id") String id) {
        userPhotoService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
