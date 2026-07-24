import { balance } from "../balance.js";
const disableInput = (inputCell) => {
    inputCell.max = "0";
    inputCell.min = "0";
    inputCell.disabled = true;
};
export const updateRowUI = (holding, buyInputsSum = 0) => {
    const { priceCell, valueCell, buyInput, sellInput, nameCell, sharesCell, symbolCell } = holding.row;
    const { isPriceUpToDate, latestPrice, value, companyName, shares, symbol } = holding.holding;
    const availableSharesMax = balance != null && latestPrice !== undefined ? Math.floor(balance / latestPrice) : 0;
    const isBuyDisabled = balance == null || latestPrice === undefined || !isPriceUpToDate || availableSharesMax <= 0;
    const isSellDisabled = balance == null || latestPrice === undefined || !isPriceUpToDate || !shares;
    const domUpdate = () => {
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
        if (isSellDisabled) {
            disableInput(sellInput);
        }
        else {
            sellInput.disabled = false;
            sellInput.min = "0";
            sellInput.max = String(shares);
        }
        if (isBuyDisabled) {
            disableInput(buyInput);
        }
        else {
            buyInput.disabled = false;
            const remaining = balance - buyInputsSum + latestPrice * (Number(buyInput.value) || 0);
            const availableShares = Math.floor(remaining / latestPrice);
            buyInput.max = String(availableShares);
            buyInput.min = String(availableShares > 0 ? 1 : 0);
        }
    };
    return { isSellDisabled, isBuyDisabled, domUpdate };
};
