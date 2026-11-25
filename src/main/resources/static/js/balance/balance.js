import { CustomError } from "../types/CustomError.js";
import { isErrorDTO } from "../utils/isErrorDTO.js";
const balanceCell = document.querySelector(".balance");
const tradingFooter = document.querySelector(".table-footer");
export let balance;
export const fetchBalance = async () => {
    const res = await fetch("/api/balance");
    const body = await res.json().catch(() => {
        throw new Error("Invalid response from server");
    });
    if (!res.ok) {
        if (!isErrorDTO(body)) {
            throw new Error(`Request failed ${res.status}`);
        }
        if (body.code === "UNAUTHENTICATED") {
            sessionStorage.removeItem("USER_SESSION");
            balance = null;
            return null;
        }
        throw new CustomError(body.message, res.status, body.code);
    }
    if (typeof body !== "number" || Number.isNaN(body)) {
        throw new Error("Invalid or missing balance");
    }
    balance = body;
    return balance;
};
export const setBalance = (value) => {
    if (typeof value === "number" && !Number.isNaN(value)) {
        balance = value;
    }
    else {
        console.warn("Failed to update invalid balance:", value);
    }
};
export const handleFetchBalance = async () => {
    try {
        await fetchBalance();
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
        tradingFooter.style.display = "";
        balanceCell.style.color = "red";
        balanceCell.textContent = "$--";
    }
};
