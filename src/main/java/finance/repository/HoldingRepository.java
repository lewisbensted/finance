package finance.repository;

import finance.entity.Holding;
import finance.entity.HoldingId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface HoldingRepository extends JpaRepository<Holding, HoldingId> {
    Optional<Holding> findByIdUserIdAndIdSymbol(Long userId, String symbol);
}
