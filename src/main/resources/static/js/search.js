import { fetchBalance } from "./balance.js";
import { updatePrices } from "./price.js";
import { updateShares } from "./shares.js";
import { CustomError } from "./types/CustomError.js";
const message = document.querySelector(".message");
const INTERVAL = JSON.parse(localStorage.getItem("INTERVAL"));
const balanceCell = document.querySelector(".balance");
const tradingFooter = document.querySelector(".table-footer");
const quoteSpinner = document.querySelector(".quote-spinner");
const quoteButton = document.querySelector(".quote-button");
const quoteTable = document.querySelector("table");
const buyForm = document.getElementById("buy-form");
const sellForm = document.getElementById("sell-form");
const buyTotal = document.querySelector(".buy-total");
const sellTotal = document.querySelector(".sell-total");
let priceIntervalId = undefined;
const transactionInProgress = false;
let balance = null;
let holding = null;
try {
    balance = await fetchBalance();
    if (balance === null) {
        tradingFooter.style.display = "none";
    }
    else {
        tradingFooter.style.display = "";
        balanceCell.style.color = "";
        balanceCell.textContent = `$${balance.toFixed(2)}`;
    }
}
catch (error) {
    console.error(error);
    balanceCell.textContent = "$--";
    balanceCell.style.color = "red";
    tradingFooter.style.display = "";
}
const quoteForm = document.getElementById("quote-form");
quoteForm.addEventListener("submit", async (e) => {
    e.preventDefault();
    if (priceIntervalId) {
        clearInterval(priceIntervalId);
        priceIntervalId = undefined;
    }
    const shareSymbol = document.querySelector(".quote-input").value.trim().toUpperCase();
    if (!shareSymbol) {
        message.textContent = "Invalid input - please enter a symbol.";
        return;
    }
    holding = {
        symbol: shareSymbol,
        companyName: null,
        shares: null,
        latestPrice: null,
        value: null,
        nameCell: document.querySelector(".name"),
        symbolCell: document.querySelector(".symbol"),
        sharesCell: document.querySelector(".current-shares"),
        priceCell: document.querySelector(".price"),
        valueCell: document.querySelector(".value"),
        buyInput: document.querySelector(".buy-input"),
        sellInput: document.querySelector(".sell-input"),
    };
    quoteButton.style.display = "none";
    quoteSpinner.style.setProperty("display", "flex", "important");
    quoteTable.style.display = "none";
    message.textContent = "";
    holding.sellInput.value = holding.buyInput.value = "";
    try {
        const holdingMap = new Map();
        holdingMap.set(shareSymbol, holding);
        await Promise.all([
            updateShares(holding),
            updatePrices(holdingMap, balance, transactionInProgress, true, true),
        ]);
        const shares = holding.shares;
        const sharePrice = holding.latestPrice;
        buyForm.symbol.value = sellForm.symbol.value = shareSymbol;
        holding.symbolCell.textContent = shareSymbol;
        if (shares === null) {
            holding.valueCell.textContent = "";
        }
        else if (shares === undefined) {
            holding.valueCell.textContent = holding.sharesCell.textContent = "$--";
            holding.valueCell.style.color = holding.sharesCell.style.color = "red";
        }
        else {
            holding.valueCell.style.color = "";
            holding.valueCell.textContent = `$${(shares * sharePrice).toFixed(2)}`;
        }
        quoteTable.style.display = "";
        priceIntervalId = setInterval(() => {
            updatePrices(holdingMap, balance, transactionInProgress, true).then(() => {
                if (!transactionInProgress) {
                    if (buyTotal?.textContent)
                        holding.buyInput.dispatchEvent(new Event("input"));
                    if (sellTotal?.textContent)
                        holding.sellInput.dispatchEvent(new Event("input"));
                }
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
        quoteButton.style.display = "";
        quoteSpinner.style.setProperty("display", "none", "important");
    }
});
