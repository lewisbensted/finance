
import { domUpdates } from "../utils/domUpdates.js";
import { updateRowUI } from "../utils/updateRowUI.js";
import { HoldingItem } from "./../types/Holding.js";

const sellButton = document.querySelector(".sell-button");

export const updateSharesUI = (holding: HoldingItem) => {
	const shares = holding.holding.shares;
	domUpdates.push(updateRowUI(holding).domUpdate);
	domUpdates.push(() => {
		if (shares === null || shares === undefined) {
			sellButton.disabled = true;
			return;
		}
		if (shares > 0) {
			sellButton.disabled = false;
		}
	});
};
