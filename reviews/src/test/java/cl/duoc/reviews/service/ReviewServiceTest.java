package cl.duoc.reviews.service;

import cl.duoc.reviews.dto.ApiResponse;
import cl.duoc.reviews.dto.ReviewRequestDTO;
import cl.duoc.reviews.dto.ReviewResponseDTO;
import cl.duoc.reviews.dto.UserDTO;
import cl.duoc.reviews.exception.BusinessException;
import cl.duoc.reviews.exception.ResourceNotFoundException;
import cl.duoc.reviews.exception.UnauthorizedException;
import cl.duoc.reviews.model.Review;
import cl.duoc.reviews.repository.ReviewRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private AuthService authService;

    @Mock
    private DestinationService destinationService;

    @InjectMocks
    private ReviewService reviewService;

    private UUID userId;
    private UUID destinationId;
    private UUID reviewId;
    private UserDTO userDTO;
    private ReviewRequestDTO requestDTO;
    private Review existingReview;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        destinationId = UUID.randomUUID();
        reviewId = UUID.randomUUID();

        userDTO = new UserDTO();
        userDTO.setId(userId);
        userDTO.setUsername("testUser");

        requestDTO = new ReviewRequestDTO();
        requestDTO.setDestinationId(destinationId);
        requestDTO.setRating(4);
        requestDTO.setTitle("Excelente destino");
        requestDTO.setComment("Muy buen lugar para visitar, totalmente recomendado");

        existingReview = new Review();
        existingReview.setId(reviewId);
        existingReview.setUserId(userId);
        existingReview.setDestinationId(destinationId);
        existingReview.setRating(4);
        existingReview.setTitle("Excelente destino");
        existingReview.setComment("Muy buen lugar para visitar, totalmente recomendado");
        existingReview.setCreatedAt(LocalDateTime.now());
    }

    // -----------------------------------------------------------------------
    // createReview
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Crear reseña exitosamente con token y destino válidos")
    void createReview_whenValidTokenAndDestination_returnsCreated() {
        when(authService.validateToken("token-valido"))
                .thenReturn(new ApiResponse<>(200, "OK", userDTO));
        when(destinationService.validateDestination(destinationId, "token-valido"))
                .thenReturn(new ApiResponse<>(200, "OK", true));
        when(reviewRepository.findByUserIdAndDestinationId(userId, destinationId))
                .thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class)))
                .thenReturn(existingReview);

        ApiResponse<ReviewResponseDTO> response = reviewService.createReview("token-valido", requestDTO);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).isNotNull();
        verify(reviewRepository, times(1)).save(any(Review.class));
    }

    @Test
    @DisplayName("Crear reseña falla con token inválido — retorna 401")
    void createReview_whenInvalidToken_returns401() {
        when(authService.validateToken("token-invalido"))
                .thenReturn(new ApiResponse<>(401, "No autorizado", null));

        ApiResponse<ReviewResponseDTO> response = reviewService.createReview("token-invalido", requestDTO);

        assertThat(response.getCode()).isEqualTo(401);
        assertThat(response.getData()).isNull();
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear reseña falla cuando el destino no existe — retorna 400")
    void createReview_whenDestinationNotFound_returns400() {
        when(authService.validateToken("token-valido"))
                .thenReturn(new ApiResponse<>(200, "OK", userDTO));
        when(destinationService.validateDestination(destinationId, "token-valido"))
                .thenReturn(new ApiResponse<>(200, "OK", false));

        ApiResponse<ReviewResponseDTO> response = reviewService.createReview("token-valido", requestDTO);

        assertThat(response.getCode()).isEqualTo(400);
        verify(reviewRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear reseña duplicada lanza BusinessException")
    void createReview_whenDuplicateReview_throwsBusinessException() {
        when(authService.validateToken("token-valido"))
                .thenReturn(new ApiResponse<>(200, "OK", userDTO));
        when(destinationService.validateDestination(destinationId, "token-valido"))
                .thenReturn(new ApiResponse<>(200, "OK", true));
        when(reviewRepository.findByUserIdAndDestinationId(userId, destinationId))
                .thenReturn(Optional.of(existingReview));

        assertThatThrownBy(() -> reviewService.createReview("token-valido", requestDTO))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ya existe una reseña");

        verify(reviewRepository, never()).save(any());
    }

    // -----------------------------------------------------------------------
    // getReviewById
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Obtener reseña inexistente lanza ResourceNotFoundException")
    void getReviewById_whenNotFound_throwsNotFoundException() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> reviewService.getReviewById(reviewId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Reseña no encontrada");
    }

    @Test
    @DisplayName("Obtener reseña existente retorna 200")
    void getReviewById_whenFound_returns200() {
        when(reviewRepository.findById(reviewId)).thenReturn(Optional.of(existingReview));

        ApiResponse<ReviewResponseDTO> response = reviewService.getReviewById(reviewId);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData().getId()).isEqualTo(reviewId);
    }

    // -----------------------------------------------------------------------
    // updateReview
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Actualizar reseña por usuario que no es el autor lanza UnauthorizedException")
    void updateReview_whenNotAuthor_throwsUnauthorizedException() {
        UUID otherUserId = UUID.randomUUID();
        UserDTO otherUser = new UserDTO();
        otherUser.setId(otherUserId);
        otherUser.setUsername("otroUsuario");

        when(authService.validateToken("otro-token"))
                .thenReturn(new ApiResponse<>(200, "OK", otherUser));
        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(existingReview));

        assertThatThrownBy(() -> reviewService.updateReview("otro-token", reviewId, requestDTO))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Solo el autor");
    }

    // -----------------------------------------------------------------------
    // deleteReview
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Eliminar reseña por usuario que no es el autor lanza UnauthorizedException")
    void deleteReview_whenNotAuthor_throwsUnauthorizedException() {
        UUID otherUserId = UUID.randomUUID();
        UserDTO otherUser = new UserDTO();
        otherUser.setId(otherUserId);
        otherUser.setUsername("intruso");

        when(authService.validateToken("otro-token"))
                .thenReturn(new ApiResponse<>(200, "OK", otherUser));
        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(existingReview));

        assertThatThrownBy(() -> reviewService.deleteReview("otro-token", reviewId))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Solo el autor");
    }

    @Test
    @DisplayName("Eliminar reseña propia retorna 200")
    void deleteReview_whenAuthor_returns200() {
        when(authService.validateToken("token-valido"))
                .thenReturn(new ApiResponse<>(200, "OK", userDTO));
        when(reviewRepository.findById(reviewId))
                .thenReturn(Optional.of(existingReview));
        doNothing().when(reviewRepository).delete(existingReview);

        ApiResponse<Void> response = reviewService.deleteReview("token-valido", reviewId);

        assertThat(response.getCode()).isEqualTo(200);
        verify(reviewRepository, times(1)).delete(existingReview);
    }

    // -----------------------------------------------------------------------
    // getReviewsByDestination
    // -----------------------------------------------------------------------

    @Test
    @DisplayName("Listar reseñas por destino retorna lista")
    void getReviewsByDestination_returnsList() {
        when(reviewRepository.findByDestinationId(destinationId))
                .thenReturn(List.of(existingReview));

        ApiResponse<List<ReviewResponseDTO>> response = reviewService.getReviewsByDestination(destinationId);

        assertThat(response.getCode()).isEqualTo(200);
        assertThat(response.getData()).hasSize(1);
    }
}
