import { balance, fetchBalance } from "./balance.js";
import { collectInputs, handleBuy } from "./buy.js";
import { handleFetchPrices } from "./price.js";
import { handleShares } from "./shares.js";
import { setTransactionInProgress, transactionInProgress } from "./transactionInProgress.js";
import { CustomError } from "./types/CustomError.js";
import { createHolding } from "./types/Holding.js";
const message = document.querySelector(".message");
const balanceCell = document.querySelector(".balance");
const tradingFooter = document.querySelector(".table-footer");
const quoteSpinner = document.querySelector(".quote-spinner");
const quoteButton = document.querySelector(".quote-button");
const quoteTable = document.querySelector("table");
const buyTotal = document.querySelector(".buy-total");
const sellTotal = document.querySelector(".sell-total");
const buySpinner = document.querySelector(".buy-spinner");
const buyButton = document.querySelector(".buy-button");
let priceIntervalId = undefined;
let holdingsMap;
const INTERVAL = 3000;
const domUpdates = [];
try {
    await fetchBalance();
    if (balance === null) {
        tradingFooter.style.display = "none";
    }
    else {
        balanceCell.style.color = "";
        balanceCell.textContent = `$${balance.toFixed(2)}`;
    }
}
catch (error) {
    console.error(error);
    balanceCell.style.color = "red";
    balanceCell.textContent = "$--";
}
const setQuoteLoading = (loading) => {
    quoteButton.style.display = loading ? "none" : "";
    quoteButton.disabled = false;
    quoteSpinner.style.setProperty("display", loading ? "flex" : "none", "important");
    if (loading) {
        quoteTable.style.display = "none";
        message.textContent = "";
    }
};
const applyDomUpdates = (domUpdates) => {
    const updates = domUpdates.splice(0);
    requestAnimationFrame(() => {
        for (const update of updates) {
            update();
        }
    });
};
const quoteForm = document.getElementById("quote-form");
quoteForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    const shareSymbol = document.querySelector(".quote-input").value.trim().toUpperCase();
    if (!shareSymbol) {
        message.textContent = "Invalid input - please enter a symbol.";
        return;
    }
    if (priceIntervalId) {
        clearInterval(priceIntervalId);
        priceIntervalId = undefined;
    }
    const holding = createHolding(shareSymbol);
    holdingsMap = new Map();
    holdingsMap.set(shareSymbol, holding);
    setQuoteLoading(true);
    try {
        await Promise.all([
            handleShares(holding, domUpdates),
            handleFetchPrices(holdingsMap, domUpdates, true, true),
        ]);
        applyDomUpdates(domUpdates);
        quoteTable.style.display = "";
        //add abortcontroller
        priceIntervalId = setInterval(() => {
            handleFetchPrices(holdingsMap, domUpdates, true)
                .then(() => {
                if (!transactionInProgress) {
                    if (buyTotal?.textContent)
                        holding.row.buyInput.dispatchEvent(new Event("input"));
                    if (sellTotal?.textContent)
                        holding.row.sellInput.dispatchEvent(new Event("input"));
                }
                applyDomUpdates(domUpdates);
            })
                .catch((error) => {
                console.error(error);
            });
        }, INTERVAL);
    }
    catch (error) {
        console.error(error);
        message.textContent =
            error instanceof CustomError && error.code === "OPERATION_FAILED"
                ? "Symbol not found"
                : "Unexpected error";
    }
    finally {
        setQuoteLoading(false);
    }
});
const buyForm = document.getElementById("buy-form");
buyForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    if (transactionInProgress)
        return;
    setTransactionInProgress(true);
    message.textContent = "";
    const { invalidInput, transactionRequests, purchaseTotal } = collectInputs([
        ...holdingsMap.values(),
    ]);
    if (purchaseTotal > balance) {
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
    await handleBuy(holdingsMap, transactionRequests, domUpdates);
    applyDomUpdates(domUpdates);
    buySpinner.style.setProperty("display", "none", "important");
    buyButton.style.display = "";
    setTransactionInProgress(false);
});
