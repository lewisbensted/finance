import { balance } from "./balance.js";
import { updateHoldingUI } from "./holding.js";
import { ApiResponse } from "./types/ApiResponse.js";
import { CustomError } from "./types/CustomError.js";
import { HoldingDTO, HoldingItem } from "./types/Holding.js";

const sellButton = document.querySelector(".sell-button");

const buyForm = document.querySelector("#buy-form");
const sellForm = document.querySelector("#sell-form");

const fetchShares = async (symbol: string) => {
	const res = await fetch(`/api/holding?symbol=${encodeURIComponent(symbol)}`);
	const response = (await res.json()) as ApiResponse<HoldingDTO>;

	if (!res.ok) {
		switch (response.error?.code) {
			case "UNAUTHENTICATED":
				return null;
			case "NOT_FOUND":
				return 0;
			default:
				throw new CustomError(
					response.error?.message ?? `Request failed ${res.status}`,
					res.status,
					response.error?.code,
				);
		}
	}

	if (response.data?.shares === undefined || typeof response.data.shares !== "number")
		throw new Error("Invalid response from server");
	return response.data.shares;
};

const updateUI = (holding: HoldingItem, domUpdates: (() => void)[]) => {
	const shares = holding.holding.shares;
	domUpdates.push(updateHoldingUI(holding));
	domUpdates.push(() => {
		if (shares === null || shares === undefined) {
			buyForm.current_shares.value = "";
			sellForm.current_shares.value = "";
			sellButton.disabled = true;
			return;
		}
		buyForm.current_shares.value = sellForm.current_shares.value = shares;
		if (shares > 0) {
			sellButton.disabled = false;
		}
	});
};

export const handleShares = async (
	holding: HoldingItem,
	domUpdates: (() => void)[] = [],
) => {
	try {
		const symbol = holding.holding.symbol;
		const shares = await fetchShares(symbol);
		holding.holding.shares = shares;
		updateUI(holding, domUpdates);
	} catch (error) {
		console.error(error);
	}
};
