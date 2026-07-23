import { balance } from "../balance.js";
import { currentHoldingsMap } from "../holding.js";
import { setTransactionInProgress, transactionInProgress } from "../transactionInProgress.js";
import { collectInputs } from "../utils/collectInputs.js";
import { applyDomUpdates, domUpdates } from "../utils/domUpdates.js";
import { sumInputs } from "../utils/sumInputs.js";
import { handleBuy } from "./handleBuy.js";
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
    const { invalidInput, transactionRequests } = collectInputs(holdings, "buy");
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
    //document.querySelectorAll("input, button").forEach((el) => (el.disabled = true));
    await handleBuy(transactionRequests, domUpdates);
    applyDomUpdates(domUpdates);
    buySpinner.style.setProperty("display", "none", "important");
    buyButton.style.display = "";
    setTransactionInProgress(false);
});
