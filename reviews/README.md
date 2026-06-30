# Microservicio Reviews — Examen Full Stack

Microservicio de reseñas de destinos turísticos desarrollado como parte del examen Full Stack.
Se integra al ecosistema Spring Cloud existente (Eureka + API Gateway + Login + Destination).

**Puerto:** `9003`  
**Base de datos:** MySQL `reviews` (puerto `3313`)  
**Ruta Gateway:** `/api/v1/reviews/**`

---

## IE 2.1.3 — Modelado de entidades, relaciones y estructura CSR

### Modelo de datos

El microservicio gestiona dos entidades relacionadas:

```
reviews (1) ──────────────── (N) review_replies
```

**Tabla `reviews`**
| Campo | Tipo | Descripción |
|---|---|---|
| `id` | BINARY(16) | UUID generado con `@PrePersist` |
| `user_id` | BINARY(16) | ID del usuario autor (obtenido del token) |
| `destination_id` | BINARY(16) | ID del destino reseñado (validado remotamente) |
| `rating` | INT | Calificación entre 1 y 5 |
| `title` | VARCHAR(100) | Título de la reseña |
| `comment` | TEXT | Cuerpo de la reseña (mín. 10 caracteres) |
| `created_at` | DATETIME | Fecha de creación |
| `updated_at` | DATETIME | Fecha de última modificación |

**Tabla `review_replies`**
| Campo | Tipo | Descripción |
|---|---|---|
| `id` | BINARY(16) | UUID generado con `@PrePersist` |
| `review_id` | BINARY(16) FK | FK hacia `reviews.id` |
| `user_id` | BINARY(16) | ID del usuario que responde |
| `reply_text` | VARCHAR(500) | Texto de la respuesta |
| `created_at` | DATETIME | Fecha de creación |

La relación `@OneToMany`/`@ManyToOne` entre `Review` y `ReviewReply` cumple la normalización en 3FN: cada atributo depende únicamente de la clave primaria de su tabla.

### Estructura CSR

```
cl.duoc.reviews
├── config/        SecurityConfig, TokenValidationFilter, WebClientConfig, OpenApiConfig
├── controller/    ReviewController, ReviewReplyController
├── service/       ReviewService, ReviewReplyService, AuthService, DestinationService
├── repository/    ReviewRepository, ReviewReplyRepository
├── model/         Review, ReviewReply
├── dto/           ApiResponse<T>, ReviewRequestDTO, ReviewResponseDTO,
│                  ReplyRequestDTO, ReplyResponseDTO, UserDTO
└── exception/     GlobalExceptionHandler, ResourceNotFoundException,
                   BusinessException, UnauthorizedException
```

---

## IE 2.4.2 — Comunicación remota entre microservicios

El microservicio realiza dos tipos de llamadas remotas usando **WebClient con `@LoadBalanced`** para aprovechar el balanceador de carga de Spring Cloud:

### 1. Validación de token → Login Service

```
GET http://login/api/v1/users/validate?token={token}
→ ApiResponse<UserDTO>  { id, username }
```

Clase: `AuthService.validateToken(String token)`.  
Si el token es inválido o el servicio no responde, se retorna `401 Unauthorized`.

### 2. Validación de destino → Destination Service

```
GET http://destination/api/v1/destination/destinations/exists?id={destinationId}
Headers: Authorization: Bearer {token}
→ ApiResponse<Boolean>
```

Clase: `DestinationService.validateDestination(UUID id, String token)`.  
Si el destino no existe (`false`) se retorna `400 Bad Request`.

### Registro en Eureka

El microservicio se registra automáticamente en Eureka (`http://localhost:8761/eureka/`) bajo el nombre `reviews`. El API Gateway resuelve la ruta `/api/v1/reviews/**` con `lb://reviews`, delegando a Eureka la resolución de instancias.

---

## IE 2.5.2 — Aporte individual de cada integrante

### Bastián Jofré (`Bast-JFR`)

| Commit | Descripción |
|---|---|
| `Configuración inicial del microservicio reviews y registro en Eureka` | Scaffolding base del proyecto: `pom.xml`, `application.yml`, `ReviewsApplication.java`, Eureka client |
| `Modela entidades Review y ReviewReply con migración Flyway` | Entidades JPA con `@PrePersist` para UUID y migración `V1__create_reviews_tables.sql` |
| `Define DTOs con validaciones de entrada` | Todos los DTOs: `ApiResponse`, `UserDTO`, `ReviewRequestDTO` con Bean Validation, `ReviewResponseDTO`, `ReplyRequestDTO`, `ReplyResponseDTO` |
| `Implementa lógica de negocio y CRUD de reviews` | `ReviewService`: createReview (valida token + destino + duplicados), getBy*, updateReview, deleteReview con control de autoría |
| `Expone endpoints REST de reviews y replies con Swagger` | `ReviewController` y `ReviewReplyController` con `@Tag/@Operation` y extracción de token del header |
| `Agrega pruebas unitarias de la lógica de negocio` | `ReviewServiceTest`: 9 tests con Mockito cubriendo flujos exitosos y de error |

### Renato Colipe (`RCL0666`)

| Commit | Descripción |
|---|---|
| `Agrega base de datos reviews en Docker y ruta en API Gateway` | `mysql_reviews` en `docker-compose.yml` (puerto 3313) y ruta `reviews-service` en `gateway/application.yml` |
| `Implementa repositorios JPA de reviews y replies` | `ReviewRepository` y `ReviewReplyRepository` con métodos de consulta derivados (`findByUserId`, `findByDestinationId`, `findByUserIdAndDestinationId`, `findByReviewId`) |
| `Integra comunicación REST con Login y Destination vía WebClient` | `WebClientConfig` con `@LoadBalanced`, `AuthService` y `DestinationService` usando `WebClient` reactivo |
| `Implementa lógica de respuestas a reviews` | `ReviewReplyService`: `addReply` (valida token, busca reseña) y `getRepliesByReview` |
| `Agrega seguridad por token, manejo global de excepciones y logs` | `SecurityConfig` + `TokenValidationFilter` (OncePerRequestFilter), `GlobalExceptionHandler` (@RestControllerAdvice) y `OpenApiConfig` |
| `Documenta el microservicio y actualiza la colección Postman` | Este README con IEs explicativos |

---

## IE 3.1.2 — Pruebas unitarias

Las pruebas se encuentran en `src/test/java/cl/duoc/reviews/service/ReviewServiceTest.java`.

**Ejecutar:**
```bash
./mvnw test
```

### Casos cubiertos

| Test | Escenario | Resultado esperado |
|---|---|---|
| `createReview_whenValidTokenAndDestination_returnsCreated` | Token válido, destino existe, sin duplicado | HTTP 200, reseña creada |
| `createReview_whenInvalidToken_returns401` | Token no válido | HTTP 401, no llama a save() |
| `createReview_whenDestinationNotFound_returns400` | Destino retorna false | HTTP 400, no llama a save() |
| `createReview_whenDuplicateReview_throwsBusinessException` | Ya existe reseña del mismo usuario para ese destino | `BusinessException` lanzada |
| `getReviewById_whenNotFound_throwsNotFoundException` | ID inexistente | `ResourceNotFoundException` lanzada |
| `getReviewById_whenFound_returns200` | ID existente | HTTP 200, datos correctos |
| `updateReview_whenNotAuthor_throwsUnauthorizedException` | Usuario ≠ autor | `UnauthorizedException` lanzada |
| `deleteReview_whenNotAuthor_throwsUnauthorizedException` | Usuario ≠ autor | `UnauthorizedException` lanzada |
| `deleteReview_whenAuthor_returns200` | Usuario = autor | HTTP 200, `delete()` invocado |

Las pruebas usan **Mockito** para aislar `ReviewService` de `ReviewRepository`, `AuthService` y `DestinationService`, verificando comportamiento sin necesidad de base de datos ni servicios externos.

---

## IE 3.2.2 — Documentación Swagger

La documentación interactiva está disponible en:

```
http://localhost:9003/index.html
```

O a través del Gateway:
```
http://localhost:9999/api/v1/reviews/index.html
```

### Recorrido

1. Acceder a la URL de Swagger UI.
2. En la sección **Authorize** (botón candado), ingresar el token JWT obtenido del Login Service:
   ```
   POST http://localhost:9999/api/v1/users/login
   Body: { "username": "...", "password": "..." }
   ```
3. **Crear una reseña** (`POST /api/v1/reviews/reviews`):
   ```json
   {
     "destinationId": "<uuid-del-destino>",
     "rating": 5,
     "title": "Destino increíble",
     "comment": "Totalmente recomendado para visitar en familia"
   }
   ```
4. **Listar reseñas por destino** (`GET /api/v1/reviews/reviews/destination/{destinationId}`) — endpoint público.
5. **Agregar respuesta** (`POST /api/v1/reviews/replies`):
   ```json
   {
     "reviewId": "<uuid-de-la-reseña>",
     "replyText": "Gracias por tu comentario"
   }
   ```
6. **Actualizar/eliminar** reseña propia usando el ID retornado en el paso 3.

Todos los endpoints protegidos requieren el header `Authorization: Bearer <token>`.

---

## Levantar el servicio

### 1. Prerequisitos

- Java 21
- Docker Desktop (para MySQL)
- Los microservicios del ecosistema clonados y ejecutándose

### 2. Base de datos

```bash
docker compose up -d mysql_reviews
```

### 3. Compilar y ejecutar

```bash
cd reviews
./mvnw -DskipTests package
java -jar target/reviews-0.0.1-SNAPSHOT.jar
```

### 4. Orden de arranque

```
1. eurekaserver   (http://localhost:8761)
2. login          (http://localhost:9001)
3. destination    (http://localhost:9002)
4. reviews        (http://localhost:9003)  ← este servicio
5. gateway        (http://localhost:9999)
```

### 5. Verificar registro en Eureka

Abrir http://localhost:8761 → debe aparecer `REVIEWS` con estado `UP`.
