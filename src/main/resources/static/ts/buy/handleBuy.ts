import { currentHoldingsMap } from "../holding.js";
import { displayToast } from "../utils/displayToast.js";
import { updateTableUI } from "../utils/updateTableUI.js";
import { setBalance } from "./../balance.js";

import { CustomError } from "./../types/CustomError.js";
import { Transaction } from "./../types/Transaction.js";
import { buyShares } from "./buyShares.js";

export const handleBuy = async (transactionRequests: Transaction[]) => {
	const { successful, updatedBalance, failed } = await buyShares(transactionRequests);

	if (updatedBalance !== undefined) setBalance(updatedBalance);

	for (const [symbol, transaction] of successful) {
		const quantity = transaction.quantity;
		const holding = currentHoldingsMap.get(symbol);
		if (!holding) continue;

		if (quantity) {
			holding.holding.shares += quantity;
			holding.holding.value = holding.holding.latestPrice * holding.holding.shares;
		}
	}
	return { successful, failed };
};
