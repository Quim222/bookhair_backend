package com.bookhair.backend.services;

import java.io.IOException;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.DigestUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.bookhair.backend.model.User;
import com.bookhair.backend.model.UserPhoto;
import com.bookhair.backend.model.UserRole;
import com.bookhair.backend.repositories.UserPhotoRepository;
import com.bookhair.dto.UploadPhotoResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserPhotoService {

    private final UserPhotoRepository userPhotoRepository;
    private final UserService userService;

    private static final long MAX_SIZE = 10 * 1024 * 1024;

    private static void ensureImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Ficheiro vazio.");
        }
        String ct = file.getContentType();
        if (ct == null || !ct.toLowerCase().startsWith("image/")) {
            throw new IllegalArgumentException("Apenas imagens são permitidas.");
        }
        if (file.getSize() > MAX_SIZE) {
            throw new IllegalArgumentException("Imagem excede 10MB.");
        }
    }

    private static String normalizeContentType(String ct) {
        if (ct == null)
            return MediaType.APPLICATION_OCTET_STREAM_VALUE;
        ct = ct.toLowerCase();
        return switch (ct) {
            case "image/jpg", "image/jpeg" -> MediaType.IMAGE_JPEG_VALUE;
            case "image/png" -> MediaType.IMAGE_PNG_VALUE;
            case "image/webp" -> "image/webp";
            default -> ct;
        };
    }

    /** Upload/atualiza a foto (1:1 por referenceId). */
    @Transactional
    public UploadPhotoResponse upload(String id, MultipartFile file, boolean onlyStaff, String type)
            throws IOException {

        // validação para tipo "user"
        if ("user".equals(type)) {
            User u = userService.getUserById(id);
            if (onlyStaff && u != null && u.getUserRole() != UserRole.FUNCIONARIO) {
                throw new IllegalStateException("Apenas funcionários podem ter foto.");
            }
        }

        ensureImage(file);
        String contentType = normalizeContentType(file.getContentType());

        // upsert por PK = referenceId
        UserPhoto photo = userPhotoRepository.findById(id)
                .orElse(UserPhoto.builder().referenceId(id).build());

        byte[] bytes = file.getBytes();
        photo.setMimeType(contentType);
        photo.setBytes(bytes);
        photo.setType(type);
        photo.setCreatedAt(Instant.now());

        // ETag baseada no conteúdo (entre aspas)
        String etag = "\"" + DigestUtils.md5DigestAsHex(bytes) + "\"";
        userPhotoRepository.save(photo);

        // URL estável para o GET com cache-busting via ?v=<etag>
        String version = etag.replace("\"", "");
        String url = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/photosUser/").path("/").path(id)
                .queryParam("v", version)
                .toUriString();

        return new UploadPhotoResponse(url, etag, true);
    }

    /** Obtém bytes + headers para resposta HTTP. */
    @Transactional(readOnly = true)
    public PhotoResponse get(String id) {
        UserPhoto p = userPhotoRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatusCode.valueOf(404), "Sem foto para este identificador."));

        String ct = normalizeContentType(p.getMimeType());
        byte[] bytes = p.getBytes();

        String etag = "\"" + DigestUtils.md5DigestAsHex(bytes) + "\"";
        Instant lastMod = p.getCreatedAt(); // se tiver updatedAt, preferir esse

        // Cache pública com validação (304 via ETag)
        CacheControl cache = CacheControl.maxAge(30, TimeUnit.DAYS).cachePublic().mustRevalidate();

        return new PhotoResponse(bytes, ct, etag, lastMod, cache);
    }

    @Transactional
    public void delete(String id) {
        if (userPhotoRepository.existsByReferenceId(id)) {
            userPhotoRepository.deleteByReferenceId(id);
        }
    }

    @Transactional(readOnly = true)
    public boolean hasPhoto(String id) {
        return userPhotoRepository.existsByReferenceId(id);
    }

    public record PhotoResponse(
            byte[] bytes,
            String contentType,
            String etag, // com aspas
            Instant lastModified, // pode ser null
            CacheControl cacheControl) {

        public HttpHeaders toHeaders() {
            HttpHeaders h = new HttpHeaders();
            h.set(HttpHeaders.CONTENT_TYPE, contentType);
            if (cacheControl != null)
                h.setCacheControl(cacheControl.getHeaderValue());
            h.add(HttpHeaders.VARY, "Origin");
            if (etag != null && !etag.isBlank())
                h.setETag(etag);
            if (lastModified != null)
                h.setLastModified(lastModified.toEpochMilli());
            return h;
        }
    }

    // util de debug mantido
    @Transactional
    public void debugInsertBytesFixos(String id) {
        byte[] data = new byte[] { 1, 2, 3 };
        UserPhoto p = userPhotoRepository.findByReferenceId(id);
        if (p == null) {
            p = UserPhoto.builder().referenceId(id).build();
        }
        p.setMimeType("application/octet-stream");
        p.setBytes(data);
        userPhotoRepository.save(p);
    }
}
