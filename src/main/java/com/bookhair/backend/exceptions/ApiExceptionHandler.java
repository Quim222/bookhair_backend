package com.bookhair.backend.exceptions;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.server.ResponseStatusException;

import jakarta.persistence.EntityNotFoundException;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class ApiExceptionHandler {

        private static Map<String, Object> body(int status, String error, String detail) {
                return Map.of(
                                "status", status,
                                "error", error,
                                "detail", detail,
                                "timestamp", OffsetDateTime.now().toString());
        }

        // 400 - @Valid
        @ExceptionHandler(MethodArgumentNotValidException.class)
        public ResponseEntity<?> handleValidationErrors(MethodArgumentNotValidException ex) {
                var errors = ex.getBindingResult()
                                .getFieldErrors()
                                .stream()
                                .map(fe -> Map.of("field", fe.getField(), "error", fe.getDefaultMessage()))
                                .toList();

                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                                .body(Map.of(
                                                "status", 400,
                                                "error", "Dados inválidos",
                                                "errors", errors,
                                                "timestamp", OffsetDateTime.now().toString()));
        }

        // 409 - Regras de negócio (ex.: funcionário indisponível)
        @ExceptionHandler(IllegalArgumentException.class)
        public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException ex) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(body(409, "Conflito", ex.getMessage()));
        }

        // 409 - Regras de negócio
        @ExceptionHandler(IllegalStateException.class)
        public ResponseEntity<?> handleIllegalState(IllegalStateException ex) {
                return ResponseEntity.status(409).contentType(MediaType.APPLICATION_JSON)
                                .body(body(409, "Conflito", ex.getMessage()));
        }

        // 415 - Content-Type não suportado
        @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
        public ResponseEntity<?> handleUnsupportedMedia(HttpMediaTypeNotSupportedException ex) {
                String supported = ex.getSupportedMediaTypes().stream()
                                .map(MediaType::toString).collect(Collectors.joining(", "));
                String detail = "Content-Type não suportado. Use um dos seguintes: " +
                                (supported.isBlank() ? "multipart/form-data, application/json" : supported);
                return ResponseEntity.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(body(415, "Tipo de conteúdo não suportado", detail));
        }

        // 413 - Ficheiro excede limite configurado
        @ExceptionHandler(MaxUploadSizeExceededException.class)
        public ResponseEntity<?> handleMaxUpload(MaxUploadSizeExceededException ex) {
                return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(body(413, "Ficheiro demasiado grande",
                                                "O tamanho máximo permitido foi excedido."));
        }

        // 400 - multipart mal formado (boundary, etc.)
        @ExceptionHandler(MultipartException.class)
        public ResponseEntity<?> handleMultipart(MultipartException ex) {
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                                .body(body(400, "Pedido multipart inválido",
                                                "Verifique se está a enviar como multipart/form-data e não define manualmente o Content-Type."));
        }

        // 400 - parte 'file' em falta
        @ExceptionHandler(MissingServletRequestPartException.class)
        public ResponseEntity<?> handleMissingPart(MissingServletRequestPartException ex) {
                return ResponseEntity.badRequest().contentType(MediaType.APPLICATION_JSON)
                                .body(body(400, "Parte em falta",
                                                "Campo '" + ex.getRequestPartName() + "' é obrigatório."));
        }

        // 404 - entidade não encontrada
        @ExceptionHandler(EntityNotFoundException.class)
        public ResponseEntity<?> handleNotFound(EntityNotFoundException ex) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(body(404, "Não encontrado", ex.getMessage()));
        }

        // 409 - violação de integridade (FK/unique/etc.)
        @ExceptionHandler(DataIntegrityViolationException.class)
        public ResponseEntity<?> handleDataIntegrity(DataIntegrityViolationException ex) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(body(409, "Violação de integridade de dados",
                                                "Operação não permitida pelos constrangimentos da base de dados."));
        }

        @ExceptionHandler(ResponseStatusException.class)
        public ResponseEntity<?> handleResponseStatus(ResponseStatusException ex) {
                var status = ex.getStatusCode();
                var reason = ex.getReason() != null ? ex.getReason() : status.toString();
                return ResponseEntity.status(status)
                                .contentType(MediaType.APPLICATION_JSON)
                                .body(body(status.value(), "Erro", reason));
        }

        // 500 - genérico
        @ExceptionHandler(Exception.class)
        public ResponseEntity<?> handleGeneral(Exception ex) {
                return ResponseEntity.status(500).contentType(MediaType.APPLICATION_JSON)
                                .body(body(500, "Erro interno no servidor", "Ocorreu um erro inesperado."));
        }
}
