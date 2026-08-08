export interface Transaction {
	symbol: string;
	quantity: number;
}

export interface TransactionResponse {
	transactions: Record<string, number>;
	balance: number;
}

export type OperationType = "BUY" | "SELL" | "FETCH" | "DEPOSIT" | "WITHDRAW" ;
export type TransactionType = "BUY" | "SELL";
export type Page = "SEARCH" | "PORTFOLIO";
