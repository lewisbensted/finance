export interface CollectInputs {
	invalidInput: boolean;
	transactionRequests: Record<string, number> | null;
	purchaseTotal: number | null;
}
