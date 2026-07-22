import { Transaction } from "./Transaction";

export interface CollectInputs {
	invalidInput: boolean;
	transactionRequests: Transaction[] | null;
	purchaseTotal: number | null;
}
