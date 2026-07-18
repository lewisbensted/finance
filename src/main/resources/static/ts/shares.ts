import { ApiResponse } from "./types/ApiResponse";
import { CustomError } from "./types/CustomError";
import { Holding } from "./types/Holding";

const sellButton = document.querySelector(".sell-button");

const buyForm = document.querySelector("#buy-form");
const sellForm = document.querySelector("#sell-form");

const fetchShares = async (symbol: string) => {
	const res = await fetch(`/api/holding?symbol=${encodeURIComponent(symbol)}`);
	const response = (await res.json()) as ApiResponse<{ shares: number }>;
	if (!res.ok) {
		if (response.error?.code === "UNAUTHENTICATED") return null;
		if (response.error?.code === "NOT_FOUND") return 0;
		throw new CustomError(
			response.error?.message ?? `Request failed ${res.status}`,
			res.status,
			response.error?.code,
		);
	}

	if (
		response.data?.shares === undefined ||
    typeof response.data.shares !== "number"
	)
		throw new Error("Invalid response from server");
	return response.data.shares;
};

export const updateShares = async (holding: Holding) => {
	try {
		const symbol = holding.symbol;
		const shares = await fetchShares(symbol);
		if (shares === null) {
			holding.sharesCell.textContent = "";
			buyForm.current_shares.value = sellForm.current_shares.value = "";
			holding.sellInput.max = 0;
			holding.sellInput.disabled = sellButton.disabled = true;
			holding.sellInput.min = 1;
			holding.valueCell.dataset.value = holding.valueCell.textContent = "";

			return null;
		}
		holding.sharesCell.dataset.value = holding.sharesCell.textContent = shares;
		buyForm.current_shares.value = sellForm.current_shares.value = shares;
		holding.sellInput.max = shares;
		holding.shares = shares;
		if (shares > 0) {
			holding.sellInput.disabled = false;
			sellButton.disabled = false;
			holding.sellInput.min = 1;
		}
		return shares;
	} catch (error) {
		console.error(error);
	}
};
