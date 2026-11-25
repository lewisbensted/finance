import { getBalanceState, setBalanceState } from "./balance.js";
import { fetchBalance } from "./fetchBalance.js";
import { updateBalanceUI } from "./updateBalanceUI.js";


export const handleFetchBalance = async () => {
	try {
		const balance = await fetchBalance();
		if (balance === null) {
			setBalanceState(null);
			return;
		}
		setBalanceState({ balance, isStale: false });
	} catch (error) {
		console.error(error);
		const balanceState = getBalanceState();
		if (balanceState) setBalanceState({ ...balanceState, isStale: true });
	}

	updateBalanceUI();
};
