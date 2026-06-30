package finance.slice.controllers;

import finance.controllers.StockController;
import finance.dtos.ItemErrorDTO;
import finance.dtos.StockDTO;
import finance.dtos.StockResultDTO;
import finance.services.StockService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Map;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StockController.class)
@AutoConfigureMockMvc(addFilters = false)
public class StockControllerTests {
    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private StockService stockService;

    @Test
    void test400NoSymbolParam() throws Exception {
        mockMvc.perform(get("/api/prices")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("No symbols provided"));
    }

    @Test
    void test400NoSymbols() throws Exception {
        mockMvc.perform(get("/api/prices?symbolsStr=")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(400))
                .andExpect(jsonPath("$.code").value("BAD_REQUEST"))
                .andExpect(jsonPath("$.message").value("No symbols provided"));
    }

    @Test
    void test200PartialFailure() throws Exception {
        when(stockService.fetchPrices(any()))
                .thenReturn(Map.of(
                        "AAPL", new StockResultDTO(new StockDTO("Apple", "AAPL", BigDecimal.valueOf(5)), null),
                        "MSFT", new StockResultDTO(new StockDTO("Microsoft", "MSFT", BigDecimal.valueOf(10)), null),
                        "ORCL", new StockResultDTO(new StockDTO("Oracle", "ORCL", BigDecimal.valueOf(15)), null),
                        "BANANA", new StockResultDTO(null, new ItemErrorDTO("NOT_FOUND", "Stock symbol not found")),
                        "GOOG", new StockResultDTO(null, new ItemErrorDTO("SERVER_ERROR", "Unexpected error")
                        )));

        mockMvc.perform(get("/api/prices").param("symbolsStr", "AAPL,ORCL,MSFT,BANANA,GOOG")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.stocks[*].symbol")
                        .value(containsInAnyOrder("AAPL", "ORCL", "MSFT")))
                .andExpect(jsonPath("$.error.code").value("PARTIAL_FAILURE"))
                .andExpect(jsonPath("$.error.message")
                        .value("Failed to fetch some prices"))
                .andExpect(jsonPath("$.error.fields.BANANA.code")
                        .value("NOT_FOUND"))
                .andExpect(jsonPath("$.error.fields.BANANA.message")
                        .value("Stock symbol not found"))
                .andExpect(jsonPath("$.error.fields.GOOG.code")
                        .value("SERVER_ERROR"))
                .andExpect(jsonPath("$.error.fields.GOOG.message")
                        .value("Unexpected error"));
    }

    @Test
    void test422AllFailed() throws Exception {
        when(stockService.fetchPrices(any()))
                .thenReturn(Map.of(
                        "BANANA", new StockResultDTO(null, new ItemErrorDTO("NOT_FOUND", "Stock symbol not found")),
                        "GOOG", new StockResultDTO(null, new ItemErrorDTO("SERVER_ERROR", "Unexpected error"))
                ));

        mockMvc.perform(get("/api/prices").param("symbolsStr", "BANANA,GOOG")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(422))
                .andExpect(jsonPath("$.stocks").isEmpty())
                .andExpect(jsonPath("$.error.code").value("UNPROCESSABLE"))
                .andExpect(jsonPath("$.error.message")
                        .value("Failed to fetch all prices"))
                .andExpect(jsonPath("$.error.fields.BANANA.code")
                        .value("NOT_FOUND"))
                .andExpect(jsonPath("$.error.fields.BANANA.message")
                        .value("Stock symbol not found"))
                .andExpect(jsonPath("$.error.fields.GOOG.code")
                        .value("SERVER_ERROR"))
                .andExpect(jsonPath("$.error.fields.GOOG.message")
                        .value("Unexpected error"));

    }

    @Test
    void test200Success() throws Exception {
        when(stockService.fetchPrices(any()))
                .thenReturn(Map.of(
                        "AAPL", new StockResultDTO(new StockDTO("Apple", "AAPL", BigDecimal.valueOf(5)), null),
                        "MSFT", new StockResultDTO(new StockDTO("Microsoft", "MSFT", BigDecimal.valueOf(10)), null),
                        "ORCL", new StockResultDTO(new StockDTO("Oracle", "ORCL", BigDecimal.valueOf(15)), null)
                ));

        mockMvc.perform(get("/api/prices").param("symbolsStr", "AAPL,ORCL,MSFT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().is(200))
                .andExpect(jsonPath("$.stocks[*].symbol")
                        .value(containsInAnyOrder("AAPL", "ORCL", "MSFT")))
                .andExpect(jsonPath("$.error").value(Matchers.nullValue()));

    }
}
