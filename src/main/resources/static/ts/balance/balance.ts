
const BALANCE_KEY = "BALANCE";

type BalanceState = {
	balance: number | undefined;
	isStale: boolean;
};

export const getBalanceState = (): BalanceState | null => {
	const balanceState = sessionStorage.getItem(BALANCE_KEY);
	if (balanceState === null) return null;

	const state = JSON.parse(balanceState);

	return {
		balance: Number.isFinite(state.balance) ? state.balance : null,
		isStale: state.isStale,
	};
};

export const setBalanceState = (value: BalanceState | null) => {
	if (value === null) {
		sessionStorage.removeItem(BALANCE_KEY);
		return;
	}
	if (Number.isFinite(value.balance)) {
		sessionStorage.setItem(
			BALANCE_KEY,
			JSON.stringify({ balance: value.balance, isStale: value.isStale === true }),
		);
	} else {
		console.warn("Failed to update invalid balance:", value.balance);
	}
};



