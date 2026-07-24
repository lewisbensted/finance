import { domUpdates } from "../utils/domUpdates.js";
import { updateRowUI } from "../utils/updateRowUI.js";
const sellButton = document.querySelector(".sell-button");
const buyForm = document.querySelector("#buy-form");
const sellForm = document.querySelector("#sell-form");
export const updateSharesUI = (holding) => {
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
