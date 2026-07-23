import { ItemError } from "../types/ApiResponse.js";
import { OperationType, Transaction } from "../types/Transaction.js";

export const displayToast = (
	successes: Map<string, Transaction>,
	failures: Record<string, ItemError> | undefined,
	operation: OperationType,
) => {
	console.log("This will be converted into a Toast");
	console.log(`Successesful ${operation}:`);
	for (const transaction of [...successes.values()])
		console.log(`${transaction.quantity} of ${transaction.symbol}`);
	console.log(`Unuccessesful ${operation}:`);
	if (failures) {
		for (const [symbol, itemError] of Object.entries(failures)) {
			console.warn(`Failed transaction ${symbol}: ${itemError.message}`);
			console.log(`${symbol}: ${itemError.message}`);
		}
	}
};
