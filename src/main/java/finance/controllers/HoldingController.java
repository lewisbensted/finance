package finance.controllers;

import finance.dtos.ApiResponse;
import finance.dtos.HoldingDTO;
import finance.dtos.PageResponse;
import finance.services.HoldingService;
import finance.session.SessionUser;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.constraints.NotBlank;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import static finance.controllers.AuthUtils.authenticateUser;

@RestController
@Validated
public class HoldingController {
    HoldingService holdingService;

    HoldingController(HoldingService holdingService) {
        this.holdingService = holdingService;
    }

    @GetMapping(value = "/api/holdings")
    ResponseEntity<PageResponse<HoldingDTO>> fetchHoldings(HttpSession session, @PageableDefault(
            size = 10
    ) Pageable pageable) {
        SessionUser sessionUser = authenticateUser(session);

        Pageable safePageable = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by(Sort.Direction.ASC, "id.symbol")
        );

        Page<HoldingDTO> holdings = holdingService.fetchHoldings(sessionUser.id(), safePageable);
        return ResponseEntity.ok().body(PageResponse.from(holdings));
    }

    @GetMapping(value = "/api/holding")
    ResponseEntity<HoldingDTO> fetchHolding(HttpSession session, @RequestParam @NotBlank String symbol) {
        SessionUser sessionUser = authenticateUser(session);

        HoldingDTO holding = holdingService.fetchHolding(sessionUser.id(), symbol);
        return ResponseEntity.ok().body(holding);
    }
}
