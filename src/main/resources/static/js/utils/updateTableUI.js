import { balance } from "../balance/balance.js";
import { domUpdates } from "./domUpdates.js";
import { updateRowUI } from "./updateRowUI.js";
const totalCell = document.querySelector(".total");
const buyButton = document.querySelector(".buy-button");
const sellButton = document.querySelector(".sell-button");
const balanceCell = document.querySelector(".balance");
const buySpinner = document.querySelector(".buy-spinner");
export const updateTableUI = (holdings, resetInputs = false, buySum = 0) => {
    let buyButtonDisabled = true;
    let sellButtonDisabled = true;
    let totalValue = 0;
    let totalValueUpToDate = true;
    for (const holding of holdings) {
        if (resetInputs) {
            holding.row.buyInput.value = "";
            holding.row.sellInput.value = "";
            buySpinner.style.setProperty("display", "none", "important");
            buyButton.style.display = "";
        }
        const { isSellDisabled, isBuyDisabled, domUpdate } = updateRowUI(holding, buySum);
        domUpdates.push(domUpdate);
        const { shares, value, isPriceUpToDate } = holding.holding;
        totalValue += value ?? 0;
        if (value === undefined || !isPriceUpToDate) {
            totalValueUpToDate = false;
        }
        if (!isSellDisabled)
            sellButtonDisabled = false;
        if (!isBuyDisabled)
            buyButtonDisabled = false;
    }
    domUpdates.push(() => {
        if (totalCell)
            totalCell.style.color = totalValueUpToDate ? "" : "red";
        if (totalCell && totalValueUpToDate)
            totalCell.textContent = totalValue.toFixed(2);
        buyButton.disabled = buyButtonDisabled;
        sellButton.disabled = sellButtonDisabled;
        if (balance != null) {
            balanceCell.style.color = "";
            balanceCell.textContent = `$${balance.toFixed(2)}`;
        }
    });
};
