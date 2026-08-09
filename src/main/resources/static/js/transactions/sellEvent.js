import { currentHoldingsMap } from "../holding/holdingOperations.js";
import { setTransactionInProgress, transactionInProgress } from "../transactionInProgress.js";
import { collectInputs } from "../utils/collectInputs.js";
import { displayToast } from "../toast.js";
import { applyDomUpdates, domUpdates } from "../utils/domUpdates.js";
import { updateTableUI } from "../utils/updateTableUI.js";
import { updateTradeTotals } from "../utils/updateTradeTotals.js";
import { handleTransaction } from "./handleTransaction.js";
import { displayMessages } from "../modal.js";
import { CustomError } from "../types/CustomError.js";
const message = document.querySelector(".message");
const sellSpinner = document.querySelector(".sell-spinner");
const sellButton = document.querySelector(".sell-button");
const sellForm = document.getElementById("sell-form");
sellForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    if (transactionInProgress)
        return;
    setTransactionInProgress(true);
    const holdings = [...currentHoldingsMap.values()];
    message.textContent = "";
    const { invalidInput, transactionRequests } = collectInputs(holdings, "SELL");
    if (invalidInput) {
        message.textContent = "Invalid input - must be positive integers.";
        setTransactionInProgress(false);
        return;
    }
    if (!Object.keys(transactionRequests).length) {
        message.textContent = "No shares selected to trade.";
        setTransactionInProgress(false);
        return;
    }
    sellSpinner.style.setProperty("display", "flex", "important");
    sellButton.style.display = "none";
    try {
        const { successful, failed } = await handleTransaction(transactionRequests, "SELL");
        updateTableUI(holdings, true);
        displayToast(successful, failed, "SELL");
    }
    catch (error) {
        console.error(error);
        displayMessages(error instanceof CustomError && error.code !== "INTERNAL_ERROR"
            ? [error.message]
            : ["An unexpected error occured - please try again"]);
        updateTableUI(holdings, false);
    }
    finally {
        applyDomUpdates(domUpdates);
        sellSpinner.style.setProperty("display", "none", "important");
        sellButton.style.display = "";
        setTransactionInProgress(false);
    }
});
document.querySelectorAll(".sell-input").forEach((input) => {
    input.addEventListener("input", updateTradeTotals);
});
