export interface Transaction {
	symbol: string;
	quantity: number;
}

export interface TransactionResponse {
	transactions: Record<string, Transaction>;
	balance: number;
}
