package fr.tutofac.config;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    public record ErreurApi(int status, String message) {}

    private ResponseEntity<ErreurApi> reponse(HttpStatus status, String message) {
        return ResponseEntity.status(status).body(new ErreurApi(status.value(), message));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErreurApi> validation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(f -> f.getField() + " : " + f.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return reponse(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler({IllegalArgumentException.class, HttpMessageNotReadableException.class})
    public ResponseEntity<ErreurApi> requeteInvalide(Exception e) {
        return reponse(HttpStatus.BAD_REQUEST, "Requête invalide : " + e.getMessage());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErreurApi> identifiants(BadCredentialsException e) {
        return reponse(HttpStatus.UNAUTHORIZED, e.getMessage());
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErreurApi> interdit(AccessDeniedException e) {
        return reponse(HttpStatus.FORBIDDEN, "Accès refusé");
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErreurApi> introuvable(NotFoundException e) {
        return reponse(HttpStatus.NOT_FOUND, e.getMessage());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErreurApi> conflit(ConflictException e) {
        return reponse(HttpStatus.CONFLICT, e.getMessage());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErreurApi> integrite(DataIntegrityViolationException e) {
        return reponse(HttpStatus.CONFLICT, "Opération impossible : données liées ou en doublon");
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ErreurApi> fichierTropVolumineux(MaxUploadSizeExceededException e) {
        return reponse(HttpStatus.PAYLOAD_TOO_LARGE, "Le relevé de notes ne peut pas dépasser 5 Mo");
    }
}