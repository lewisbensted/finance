package finance.dto;

public record StockDTO(String companyName, String symbol, Double latestPrice, String error) {
    public StockDTO(String symbol, String error){
        this("INVALID", symbol, null, error);
    }
}