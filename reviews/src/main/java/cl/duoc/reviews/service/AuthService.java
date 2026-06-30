package cl.duoc.reviews.service;

import cl.duoc.reviews.dto.ApiResponse;
import cl.duoc.reviews.dto.UserDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final WebClient.Builder webClientBuilder;

    public ApiResponse<UserDTO> validateToken(String token) {
        try {
            return webClientBuilder.build()
                    .get()
                    .uri(uriBuilder -> uriBuilder
                            .scheme("http")
                            .host("login")
                            .path("/api/v1/users/validate")
                            .queryParam("token", token)
                            .build())
                    .retrieve()
                    .bodyToMono(new ParameterizedTypeReference<ApiResponse<UserDTO>>() {})
                    .block();
        } catch (Exception e) {
            return new ApiResponse<>(500, "Error al validar token: " + e.getMessage(), null);
        }
    }
}
