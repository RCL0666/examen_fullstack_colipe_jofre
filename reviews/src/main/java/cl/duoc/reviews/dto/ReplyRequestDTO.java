package cl.duoc.reviews.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.UUID;

@Data
public class ReplyRequestDTO {

    @NotNull(message = "El ID de la reseña es obligatorio")
    private UUID reviewId;

    @NotBlank(message = "El texto de la respuesta es obligatorio")
    @Size(min = 1, max = 500, message = "La respuesta no puede superar los 500 caracteres")
    private String replyText;
}
