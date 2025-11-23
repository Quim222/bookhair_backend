package com.bookhair.dto;

public record UploadPhotoResponse(String url, String etag, boolean hasPhoto) {
}
