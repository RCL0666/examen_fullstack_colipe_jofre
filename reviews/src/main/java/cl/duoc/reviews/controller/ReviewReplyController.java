package cl.duoc.reviews.controller;

import cl.duoc.reviews.dto.ApiResponse;
import cl.duoc.reviews.dto.ReplyRequestDTO;
import cl.duoc.reviews.dto.ReplyResponseDTO;
import cl.duoc.reviews.service.ReviewReplyService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@Tag(name = "Review Reply Controller", description = "Endpoints para gestión de respuestas a reseñas")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/reviews/replies")
public class ReviewReplyController {

    private final ReviewReplyService replyService;

    @PostMapping
    @Operation(summary = "Agregar respuesta", description = "Agrega una respuesta a una reseña existente")
    public ResponseEntity<ApiResponse<ReplyResponseDTO>> addReply(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody ReplyRequestDTO dto) {

        String token = authHeader.replace("Bearer ", "");
        ApiResponse<ReplyResponseDTO> response = replyService.addReply(token, dto);
        return ResponseEntity.status(response.getCode()).body(response);
    }

    @GetMapping("/review/{reviewId}")
    @Operation(summary = "Listar respuestas", description = "Lista todas las respuestas de una reseña")
    public ResponseEntity<ApiResponse<List<ReplyResponseDTO>>> getRepliesByReview(
            @PathVariable UUID reviewId) {

        ApiResponse<List<ReplyResponseDTO>> response = replyService.getRepliesByReview(reviewId);
        return ResponseEntity.status(response.getCode()).body(response);
    }
}
