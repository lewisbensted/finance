import { getBalanceState } from "./balance.js";

const balanceCell = document.querySelector<HTMLElement>(".balance")!;
const tradingFooter = document.querySelector<HTMLElement>(".table-footer")!;

export const updateBalanceUI = () => {
    const balanceState = getBalanceState();
    const balance = balanceState?.balance;

    tradingFooter.style.display = balanceState ? "" : "none";
    balanceCell.textContent = balance !== undefined ? `$${balance?.toFixed(2)}` : "$--";
    balanceCell.style.color = balanceState?.isStale ? "red" : "";
};