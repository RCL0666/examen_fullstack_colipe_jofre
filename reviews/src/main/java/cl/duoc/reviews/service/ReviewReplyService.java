package cl.duoc.reviews.service;

import cl.duoc.reviews.dto.ApiResponse;
import cl.duoc.reviews.dto.ReplyRequestDTO;
import cl.duoc.reviews.dto.ReplyResponseDTO;
import cl.duoc.reviews.dto.UserDTO;
import cl.duoc.reviews.exception.ResourceNotFoundException;
import cl.duoc.reviews.model.Review;
import cl.duoc.reviews.model.ReviewReply;
import cl.duoc.reviews.repository.ReviewRepository;
import cl.duoc.reviews.repository.ReviewReplyRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewReplyService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewReplyService.class);

    private final ReviewReplyRepository replyRepository;
    private final ReviewRepository reviewRepository;
    private final AuthService authService;

    public ApiResponse<ReplyResponseDTO> addReply(String token, ReplyRequestDTO dto) {
        logger.info("Agregando respuesta a reseña {}", dto.getReviewId());

        ApiResponse<UserDTO> authResponse = authService.validateToken(token);
        if (authResponse.getCode() != 200 || authResponse.getData() == null) {
            logger.warn("Token inválido al agregar respuesta");
            return new ApiResponse<>(401, "Token inválido", null);
        }
        UserDTO user = authResponse.getData();

        Review review = reviewRepository.findById(dto.getReviewId())
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada"));

        ReviewReply reply = new ReviewReply();
        reply.setReview(review);
        reply.setUserId(user.getId());
        reply.setReplyText(dto.getReplyText());

        ReviewReply saved = replyRepository.save(reply);
        logger.info("Respuesta {} creada en reseña {}", saved.getId(), review.getId());
        return new ApiResponse<>(200, "Respuesta creada correctamente", toDTO(saved));
    }

    public ApiResponse<List<ReplyResponseDTO>> getRepliesByReview(UUID reviewId) {
        if (!reviewRepository.existsById(reviewId)) {
            throw new ResourceNotFoundException("Reseña no encontrada");
        }
        List<ReplyResponseDTO> replies = replyRepository.findByReviewId(reviewId)
                .stream().map(this::toDTO).collect(Collectors.toList());
        return new ApiResponse<>(200, "Respuestas de la reseña", replies);
    }

    private ReplyResponseDTO toDTO(ReviewReply r) {
        return new ReplyResponseDTO(r.getId(), r.getReview().getId(),
                r.getUserId(), r.getReplyText(), r.getCreatedAt());
    }
}
