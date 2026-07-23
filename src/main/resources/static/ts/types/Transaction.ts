export interface Transaction {
	symbol: string;
	quantity: number;
}

export interface TransactionResponse {
	transactions: Record<string, Transaction>;
	balance: number;
}

export type OperationType = "buy" | "sell" | "fetch";
export type TransactionType = "buy" | "sell";
export type Page = "search" | "portfolio";
