import { balance } from "../balance/balance.js";
import { currentHoldingsMap } from "../holding/holdingOperations.js";
import { setTransactionInProgress, transactionInProgress } from "../transactionInProgress.js";
import { collectInputs } from "../utils/collectInputs.js";
import { displayToast } from "../utils/displayToast.js";
import { applyDomUpdates, domUpdates } from "../utils/domUpdates.js";
import { sumInputs } from "../utils/sumInputs.js";
import { updateTableUI } from "../utils/updateTableUI.js";
import { updateTradeTotals } from "../utils/updateTradeTotals.js";
import { handleTransaction } from "./handleTransaction.js";
const message = document.querySelector(".message");
const buySpinner = document.querySelector(".buy-spinner");
const buyButton = document.querySelector(".buy-button");
const buyForm = document.getElementById("buy-form");
buyForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    if (transactionInProgress)
        return;
    setTransactionInProgress(true);
    const holdings = [...currentHoldingsMap.values()];
    message.textContent = "";
    const { invalidInput, transactionRequests } = collectInputs(holdings, "BUY");
    const totalValue = sumInputs(holdings);
    if (totalValue > balance) {
        message.textContent = "Could not complete trade - insufficient funds.";
        setTransactionInProgress(false);
        return;
    }
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
    buySpinner.style.setProperty("display", "flex", "important");
    buyButton.style.display = "none";
    try {
        const { successful, failed } = await handleTransaction(transactionRequests, "BUY");
        updateTableUI(holdings, true);
        displayToast(successful, failed, "BUY");
    }
    catch (error) {
        console.error(error);
        console.log("There will be toast here");
        updateTableUI(holdings, false);
    }
    finally {
        applyDomUpdates(domUpdates);
        buySpinner.style.setProperty("display", "none", "important");
        buyButton.style.display = "";
        setTransactionInProgress(false);
    }
});
document.querySelectorAll(".buy-input").forEach((input) => {
    input.addEventListener("input", updateTradeTotals);
});
