export interface Stock {
	symbol: string;
	companyName: string;
	latestPrice: number;
}

export interface StockResponse {
	stocks: Record<string, Stock>;
}