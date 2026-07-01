package finance.controllers;

import finance.dtos.HoldingDTO;
import finance.entities.User;
import finance.services.HoldingService;
import finance.session.SessionUser;
import jakarta.servlet.http.HttpSession;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static finance.controllers.AuthUtils.authenticateUser;

@RestController
public class HoldingController {
    HoldingService holdingService;

    HoldingController(HoldingService holdingService) {
        this.holdingService = holdingService;
    }

    @GetMapping(value = "/api/holdings")
    ResponseEntity<Page<HoldingDTO>> fetchHoldings(HttpSession session, @PageableDefault(
            size = 10
    ) Pageable pageable) {
        SessionUser sessionUser = authenticateUser(session);

        Pageable safePageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "id.symbol")
        );

        Page<HoldingDTO> holdings = holdingService.fetchHoldings(sessionUser.id(), safePageable);
        return ResponseEntity.status(200).body(holdings);
    }
}
