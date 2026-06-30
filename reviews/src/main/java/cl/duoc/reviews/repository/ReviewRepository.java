package cl.duoc.reviews.repository;

import cl.duoc.reviews.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface ReviewRepository extends JpaRepository<Review, UUID> {

    List<Review> findByUserId(UUID userId);

    List<Review> findByDestinationId(UUID destinationId);

    Optional<Review> findByUserIdAndDestinationId(UUID userId, UUID destinationId);
}
