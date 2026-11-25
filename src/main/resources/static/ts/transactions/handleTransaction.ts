import { currentHoldingsMap } from "../holding/holdingOperations.js";
import { getBalanceState, setBalanceState } from "../balance/balance.js";

import { Transaction, TransactionType } from "../types/Transaction.js";
import { executeTransaction } from "./executeTransaction.js";

export const handleTransaction = async (
	transactionRequests: Transaction[],
	type: TransactionType,
) => {
	const { successful, updatedBalance, failed } = await executeTransaction(
		transactionRequests,
		type,
	);

	setBalanceState({ balance: updatedBalance, isStale: false });

	for (const [symbol, quantity] of successful) {
		const holding = currentHoldingsMap.get(symbol);
		if (!holding) throw new Error(`Holding for ${symbol} not found.`);

		if (
			quantity &&
			holding.holding.shares != undefined &&
			holding.holding.latestPrice != undefined
		) {
			const delta = type === "BUY" ? quantity : -quantity;
			holding.holding.shares = holding.holding.shares + delta;
			holding.holding.value = holding.holding.latestPrice * holding.holding.shares;
		}
	}
	return { successful, failed };
};
