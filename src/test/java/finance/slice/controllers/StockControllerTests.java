package finance.slice.controllers;

import finance.controllers.StockController;
import finance.services.StockService;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;


import static finance.fixtures.StockFixtures.*;
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
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.message").value("No symbols provided"));
    }

    @Test
    void test400NoSymbols() throws Exception {
        mockMvc.perform(get("/api/prices?symbolsStr=")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error.code").value("INVALID_REQUEST"))
                .andExpect(jsonPath("$.error.message").value("No symbols provided"));
    }

    @Test
    void test200PartialFailure() throws Exception {
        when(stockService.fetchPrices(any()))
                .thenReturn(Map.of(
                        "AAPL", APPLE_STOCK_RESULT,
                        "MSFT", MICROSOFT_STOCK_RESULT,
                        "ORCL", ORACLE_STOCK_RESULT,
                        "BANANA", BANANA_STOCK_RESULT,
                        "GOOG", ERROR_STOCK_RESULT
                        ));

        mockMvc.perform(get("/api/prices").param("symbolsStr", "AAPL,ORCL,MSFT,BANANA,GOOG")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stocks[*].symbol")
                        .value(containsInAnyOrder("AAPL", "ORCL", "MSFT")))
                .andExpect(jsonPath("$.error.code").value("OPERATION_PARTIALLY_FAILED"))
                .andExpect(jsonPath("$.error.message")
                        .value("Failed to fetch some prices"))
                .andExpect(jsonPath("$.error.fields.BANANA.code")
                        .value("NOT_FOUND"))
                .andExpect(jsonPath("$.error.fields.BANANA.message")
                        .value("Stock symbol not found"))
                .andExpect(jsonPath("$.error.fields.GOOG.code")
                        .value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.error.fields.GOOG.message")
                        .value("Unexpected error"));
    }

    @Test
    void test422AllFailed() throws Exception {
        when(stockService.fetchPrices(any()))
                .thenReturn(Map.of(
                        "BANANA", BANANA_STOCK_RESULT,
                        "GOOG", ERROR_STOCK_RESULT
                ));

        mockMvc.perform(get("/api/prices").param("symbolsStr", "BANANA,GOOG")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.data.stocks").isEmpty())
                .andExpect(jsonPath("$.error.code").value("OPERATION_FAILED"))
                .andExpect(jsonPath("$.error.message")
                        .value("Failed to fetch all prices"))
                .andExpect(jsonPath("$.error.fields.BANANA.code")
                        .value("NOT_FOUND"))
                .andExpect(jsonPath("$.error.fields.BANANA.message")
                        .value("Stock symbol not found"))
                .andExpect(jsonPath("$.error.fields.GOOG.code")
                        .value("INTERNAL_ERROR"))
                .andExpect(jsonPath("$.error.fields.GOOG.message")
                        .value("Unexpected error"));

    }

    @Test
    void test200Success() throws Exception {
        when(stockService.fetchPrices(any()))
                .thenReturn(Map.of(
                        "AAPL", APPLE_STOCK_RESULT,
                        "MSFT", MICROSOFT_STOCK_RESULT,
                        "ORCL", ORACLE_STOCK_RESULT
                ));

        mockMvc.perform(get("/api/prices").param("symbolsStr", "AAPL,ORCL,MSFT")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.stocks[*].symbol")
                        .value(containsInAnyOrder("AAPL", "ORCL", "MSFT")))
                .andExpect(jsonPath("$.error").value(Matchers.nullValue()));
    }
}
