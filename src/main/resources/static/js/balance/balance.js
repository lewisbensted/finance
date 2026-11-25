import { CustomError } from "../types/CustomError.js";
import { isErrorDTO } from "../utils/isErrorDTO.js";
const balanceCell = document.querySelector(".balance");
const tradingFooter = document.querySelector(".table-footer");
const BALANCE_KEY = "BALANCE";
export const getBalanceState = () => {
    const balanceState = sessionStorage.getItem(BALANCE_KEY);
    if (balanceState === null)
        return null;
    const state = JSON.parse(balanceState);
    return {
        balance: Number.isFinite(state.balance) ? state.balance : null,
        isStale: state.isStale,
    };
};
export const setBalanceState = (value) => {
    if (value === null) {
        sessionStorage.removeItem(BALANCE_KEY);
        return;
    }
    if (Number.isFinite(value.balance)) {
        sessionStorage.setItem(BALANCE_KEY, JSON.stringify({ balance: value.balance, isStale: value.isStale === true }));
    }
    else {
        console.warn("Failed to update invalid balance:", value.balance);
    }
};
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
            return null;
        }
        throw new CustomError(body.message, res.status, body.code);
    }
    if (typeof body !== "number" || !Number.isFinite(body)) {
        throw new Error("Invalid or missing balance");
    }
    return body;
};
export const handleFetchBalance = async () => {
    try {
        const balance = await fetchBalance();
        if (balance === null) {
            setBalanceState(null);
            return;
        }
        setBalanceState({ balance, isStale: false });
    }
    catch (error) {
        console.error(error);
        const balanceState = getBalanceState();
        if (balanceState)
            setBalanceState({ ...balanceState, isStale: true });
    }
    updateBalanceUI();
};
export const updateBalanceUI = () => {
    const balanceState = getBalanceState();
    const balance = balanceState?.balance;
    tradingFooter.style.display = balanceState ? "" : "none";
    balanceCell.textContent = balance !== undefined ? `$${balance?.toFixed(2)}` : "$--";
    balanceCell.style.color = balanceState?.isStale ? "red" : "";
};
