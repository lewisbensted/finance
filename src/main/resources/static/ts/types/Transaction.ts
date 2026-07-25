export interface Transaction {
	symbol: string;
	quantity: number;
}

export interface TransactionResponse {
	transactions: Record<string, Transaction>;
	balance: number;
}

export type OperationType = "BUY" | "SELL" | "FETCH";
export type TransactionType = "BUY" | "SELL";
export type Page = "SEARCH" | "PORTFOLIO";
