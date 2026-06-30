package cl.duoc.reviews.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class ReplyResponseDTO {
    private UUID id;
    private UUID reviewId;
    private UUID userId;
    private String replyText;
    private LocalDateTime createdAt;
}
