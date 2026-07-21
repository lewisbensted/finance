import { balance } from "./balance.js";
export const updateHolding = (holding, stock) => {
    const { shares } = holding;
    holding.companyName ??= stock.companyName;
    holding.latestPrice = stock.latestPrice;
    holding.isPriceUpToDate = true;
    if (shares === undefined) {
        holding.value = undefined;
    }
    else if (shares === null) {
        holding.value = null;
    }
    else {
        holding.value = shares * stock.latestPrice;
    }
};
const disableInput = (inputCell) => {
    inputCell.max = "0";
    inputCell.min = "0";
    inputCell.disabled = true;
};
export const updateHoldingUI = (holding, buyInputsSum = 0) => {
    const { priceCell, valueCell, buyInput, sellInput, nameCell, sharesCell, symbolCell } = holding.row;
    const { isPriceUpToDate, latestPrice, value, companyName, shares, symbol } = holding.holding;
    return () => {
        nameCell.textContent = companyName;
        symbolCell.textContent = symbol;
        if (latestPrice === undefined) {
            priceCell.textContent = "$--";
            priceCell.style.color = "red";
        }
        else {
            if (!isPriceUpToDate) {
                priceCell.style.color = "red";
            }
            else {
                priceCell.textContent = `$${latestPrice.toFixed(2)}`;
                priceCell.style.color = "";
            }
        }
        if (balance == null) {
            disableInput(buyInput);
            disableInput(sellInput);
            return;
        }
        if (shares === null) {
            sharesCell.textContent = "";
        }
        else if (shares === undefined) {
            sharesCell.textContent = "$--";
            sharesCell.style.color = "red";
        }
        else {
            sharesCell.textContent = String(shares);
        }
        if (value === null) {
            valueCell.textContent = "";
        }
        else if (value === undefined) {
            valueCell.textContent = "$--";
            valueCell.style.color = "red";
        }
        else {
            if (isPriceUpToDate) {
                valueCell.style.color = "";
                valueCell.textContent = `$${value.toFixed(2)}`;
            }
            else {
                valueCell.style.color = "red";
            }
        }
        const isSellDisabled = latestPrice === undefined || !isPriceUpToDate || !shares;
        if (isSellDisabled) {
            disableInput(sellInput);
        }
        else {
            sellInput.disabled = false;
            sellInput.min = "0";
            sellInput.max = String(shares);
        }
        const isBuyDisabled = latestPrice === undefined || !isPriceUpToDate;
        if (isBuyDisabled) {
            disableInput(buyInput);
        }
        else {
            const remaining = balance - buyInputsSum + latestPrice * (Number(buyInput.value) || 0);
            const availableShares = Math.floor(remaining / latestPrice);
            const availableSharesMax = Math.floor(balance / latestPrice);
            buyInput.max = String(availableShares);
            buyInput.disabled = availableSharesMax > 0 ? false : true;
            buyInput.min = String(availableShares > 0 ? 1 : 0);
        }
    };
};
