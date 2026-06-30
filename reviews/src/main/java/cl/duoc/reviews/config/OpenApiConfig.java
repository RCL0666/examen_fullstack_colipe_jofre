package cl.duoc.reviews.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Reviews Service API",
        version = "1.0",
        description = "Microservicio de reseñas de destinos turísticos. Permite crear, consultar, " +
                      "actualizar y eliminar reseñas, así como agregar respuestas a las mismas. " +
                      "Integrado con Login Service (validación de token) y Destination Service " +
                      "(validación de destino) vía Eureka + WebClient."
    )
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT"
)
public class OpenApiConfig {
}
