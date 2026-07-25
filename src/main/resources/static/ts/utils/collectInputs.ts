import { CollectInputs } from "../types/CollectInputs.js";
import { HoldingItem } from "../types/Holding.js";
import { Transaction, TransactionType } from "../types/Transaction.js";

export const collectInputs = (holdings: HoldingItem[], type: TransactionType): CollectInputs => {
	const transactionRequests: Transaction[] = [];

	for (const holding of holdings) {
		const { buyInput, sellInput } = holding.row;

		const input = type === "BUY" ? buyInput : sellInput;

		const quantity = Number(input?.value);

		if (quantity < 0 || !Number.isInteger(quantity) || !input?.checkValidity()) {
			return { invalidInput: true, transactionRequests: null };
		}

		if (quantity > 0)
			transactionRequests.push({ symbol: holding.holding.symbol, quantity: quantity });
	}

	return { invalidInput: false, transactionRequests: transactionRequests };
};
