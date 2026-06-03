package finance.repositories;

import finance.dtos.HoldingDTO;
import finance.entities.Holding;
import finance.entities.HoldingId;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface HoldingRepository extends JpaRepository<Holding, HoldingId> {
    Page<Holding> findByIdUserId(Long userId, Pageable pageable);
}
