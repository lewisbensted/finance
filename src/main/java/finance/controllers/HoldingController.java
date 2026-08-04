package finance.controllers;

import finance.dtos.HoldingDTO;
import finance.dtos.PageResponse;
import finance.exceptions.AuthenticationException;
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
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import static finance.controllers.AuthUtils.authenticateUser;

@Controller
@Validated
public class HoldingController {
    HoldingService holdingService;

    HoldingController(HoldingService holdingService) {
        this.holdingService = holdingService;
    }

     @GetMapping(value = "/api/holdings")
     ResponseEntity<PageResponse<HoldingDTO>> fetchHoldings(HttpSession session,
             @PageableDefault(size = 5) Pageable pageable) {
         SessionUser sessionUser = authenticateUser(session);

         Pageable safePageable = PageRequest.of(
                 pageable.getPageNumber(),
                 pageable.getPageSize(),
                 Sort.by(Sort.Direction.ASC, "id.symbol"));

         Page<HoldingDTO> holdings = holdingService.fetchHoldings(sessionUser.id(), safePageable);
         return ResponseEntity.ok().body(PageResponse.from(holdings));
     }

    @GetMapping(value = "/api/holding")
    ResponseEntity<HoldingDTO> fetchHolding(HttpSession session, @RequestParam @NotBlank String symbol) {
        SessionUser sessionUser = authenticateUser(session);

        HoldingDTO holding = holdingService.fetchHolding(sessionUser.id(), symbol);
        return ResponseEntity.ok().body(holding);
    }

    @GetMapping("/portfolio")
    public String portfolio(HttpSession session, Model model) {
        SessionUser sessionUser;

        try {
            sessionUser = authenticateUser(session);
        } catch (AuthenticationException e) {
            return "redirect:/search";
        }
        Page<HoldingDTO> holdings = holdingService.fetchHoldings(sessionUser.id(), PageRequest.of(0, 5));

        model.addAttribute("holdings", holdings);
        model.addAttribute("holdingsList", holdings.getContent());
        return "portfolio";
    }
}
