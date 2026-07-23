
import { updateRowUI } from "../utils/updateRowUI.js";
import { HoldingItem } from "./../types/Holding.js";

const sellButton = document.querySelector(".sell-button");
const buyForm = document.querySelector("#buy-form");
const sellForm = document.querySelector("#sell-form");

export const updateSharesUI = (holding: HoldingItem, domUpdates: (() => void)[]) => {
	const shares = holding.holding.shares;
	domUpdates.push(updateRowUI(holding));
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
