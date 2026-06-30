package cl.duoc.reviews.controller;

import cl.duoc.reviews.dto.ApiResponse;
import cl.duoc.reviews.dto.ReviewRequestDTO;
import cl.duoc.reviews.dto.ReviewResponseDTO;
import cl.duoc.reviews.service.ReviewService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Review Controller", description = "Endpoints para gestión de reseñas de destinos turísticos")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @PostMapping
    @Operation(summary = "Crear reseña", description = "Crea una nueva reseña validando token y destino existente")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> createReview(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ReviewRequestDTO dto) {

        String token = authHeader.replace("Bearer ", "");
        ApiResponse<ReviewResponseDTO> response = reviewService.createReview(token, dto);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/user")
    @Operation(summary = "Mis reseñas", description = "Lista todas las reseñas del usuario autenticado")
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getMyReviews(
            @RequestHeader("Authorization") String authHeader) {

        String token = authHeader.replace("Bearer ", "");
        ApiResponse<List<ReviewResponseDTO>> response = reviewService.getReviewsByUser(token);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/destination/{destinationId}")
    @Operation(summary = "Reseñas por destino", description = "Lista todas las reseñas de un destino específico")
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getByDestination(
            @PathVariable UUID destinationId) {

        ApiResponse<List<ReviewResponseDTO>> response = reviewService.getReviewsByDestination(destinationId);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener reseña por ID", description = "Retorna una reseña específica por su identificador")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> getById(@PathVariable UUID id) {
        ApiResponse<ReviewResponseDTO> response = reviewService.getReviewById(id);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar reseña", description = "Actualiza una reseña existente (solo el autor)")
    public ResponseEntity<ApiResponse<ReviewResponseDTO>> updateReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id,
            @Valid @RequestBody ReviewRequestDTO dto) {

        String token = authHeader.replace("Bearer ", "");
        ApiResponse<ReviewResponseDTO> response = reviewService.updateReview(token, id, dto);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar reseña", description = "Elimina una reseña (solo el autor)")
    public ResponseEntity<ApiResponse<Void>> deleteReview(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable UUID id) {

        String token = authHeader.replace("Bearer ", "");
        ApiResponse<Void> response = reviewService.deleteReview(token, id);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
