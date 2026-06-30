package cl.duoc.reviews.service;

import cl.duoc.reviews.dto.*;
import cl.duoc.reviews.exception.BusinessException;
import cl.duoc.reviews.exception.ResourceNotFoundException;
import cl.duoc.reviews.exception.UnauthorizedException;
import cl.duoc.reviews.model.Review;
import cl.duoc.reviews.repository.ReviewRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ReviewService {

    private static final Logger logger = LoggerFactory.getLogger(ReviewService.class);

    private final ReviewRepository reviewRepository;
    private final AuthService authService;
    private final DestinationService destinationService;

    public ApiResponse<ReviewResponseDTO> createReview(String token, ReviewRequestDTO dto) {
        logger.info("Creando reseña para destino {}", dto.getDestinationId());

        ApiResponse<UserDTO> authResponse = authService.validateToken(token);
        if (authResponse.getCode() != 200 || authResponse.getData() == null) {
            logger.warn("Token inválido al crear reseña");
            return new ApiResponse<>(401, "Token inválido", null);
        }
        UserDTO user = authResponse.getData();

        ApiResponse<Boolean> destResponse = destinationService.validateDestination(dto.getDestinationId(), token);
        if (destResponse == null || destResponse.getCode() != 200 || Boolean.FALSE.equals(destResponse.getData())) {
            logger.warn("Destino {} no existe", dto.getDestinationId());
            return new ApiResponse<>(400, "Destino no existe", null);
        }

        reviewRepository.findByUserIdAndDestinationId(user.getId(), dto.getDestinationId())
                .ifPresent(r -> {
                    throw new BusinessException("Ya existe una reseña para este destino");
                });

        Review review = new Review();
        review.setUserId(user.getId());
        review.setDestinationId(dto.getDestinationId());
        review.setRating(dto.getRating());
        review.setTitle(dto.getTitle());
        review.setComment(dto.getComment());

        Review saved = reviewRepository.save(review);
        logger.info("Reseña creada con id {}", saved.getId());
        return new ApiResponse<>(200, "Reseña creada correctamente", toDTO(saved));
    }

    public ApiResponse<List<ReviewResponseDTO>> getReviewsByUser(String token) {
        ApiResponse<UserDTO> authResponse = authService.validateToken(token);
        if (authResponse.getCode() != 200 || authResponse.getData() == null) {
            return new ApiResponse<>(401, "Token inválido", null);
        }
        List<ReviewResponseDTO> reviews = reviewRepository.findByUserId(authResponse.getData().getId())
                .stream().map(this::toDTO).collect(Collectors.toList());
        return new ApiResponse<>(200, "Reseñas del usuario", reviews);
    }

    public ApiResponse<List<ReviewResponseDTO>> getReviewsByDestination(UUID destinationId) {
        List<ReviewResponseDTO> reviews = reviewRepository.findByDestinationId(destinationId)
                .stream().map(this::toDTO).collect(Collectors.toList());
        return new ApiResponse<>(200, "Reseñas del destino", reviews);
    }

    public ApiResponse<ReviewResponseDTO> getReviewById(UUID id) {
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada"));
        return new ApiResponse<>(200, "Reseña encontrada", toDTO(review));
    }

    public ApiResponse<ReviewResponseDTO> updateReview(String token, UUID id, ReviewRequestDTO dto) {
        ApiResponse<UserDTO> authResponse = authService.validateToken(token);
        if (authResponse.getCode() != 200 || authResponse.getData() == null) {
            return new ApiResponse<>(401, "Token inválido", null);
        }
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada"));
        if (!review.getUserId().equals(authResponse.getData().getId())) {
            throw new UnauthorizedException("Solo el autor puede editar esta reseña");
        }
        review.setRating(dto.getRating());
        review.setTitle(dto.getTitle());
        review.setComment(dto.getComment());
        review.setUpdatedAt(LocalDateTime.now());
        Review saved = reviewRepository.save(review);
        logger.info("Reseña {} actualizada", id);
        return new ApiResponse<>(200, "Reseña actualizada", toDTO(saved));
    }

    public ApiResponse<Void> deleteReview(String token, UUID id) {
        ApiResponse<UserDTO> authResponse = authService.validateToken(token);
        if (authResponse.getCode() != 200 || authResponse.getData() == null) {
            return new ApiResponse<>(401, "Token inválido", null);
        }
        Review review = reviewRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Reseña no encontrada"));
        if (!review.getUserId().equals(authResponse.getData().getId())) {
            throw new UnauthorizedException("Solo el autor puede eliminar esta reseña");
        }
        reviewRepository.delete(review);
        logger.info("Reseña {} eliminada", id);
        return new ApiResponse<>(200, "Reseña eliminada", null);
    }

    private ReviewResponseDTO toDTO(Review r) {
        return new ReviewResponseDTO(r.getId(), r.getUserId(), r.getDestinationId(),
                r.getRating(), r.getTitle(), r.getComment(), r.getCreatedAt(), r.getUpdatedAt());
    }
}
