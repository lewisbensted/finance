package finance.services;

import finance.dtos.HoldingDTO;
import finance.entities.Holding;
import finance.exceptions.NotFoundException;
import finance.repositories.HoldingRepository;
import finance.repositories.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class HoldingService {

    private final HoldingRepository holdingRepository;
    private final UserRepository userRepository;

    public HoldingService(HoldingRepository holdingRepository,
                          UserRepository userRepository) {
        this.holdingRepository = holdingRepository;
        this.userRepository = userRepository;
    }

    public Page<HoldingDTO> fetchHoldings(Long userId, Pageable pageable) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Page<Holding> holdings = holdingRepository.findByIdUserId(userId, pageable);
        return holdings.map(Holding::toDTO);
    }

    public HoldingDTO fetchHolding(Long userId, String symbol) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User not found"));
        Holding holding = holdingRepository.findByIdUserIdAndIdSymbol(userId, symbol)
                .orElseThrow(() -> new NotFoundException("Holding not found"));
        return holding.toDTO();
    }
}
