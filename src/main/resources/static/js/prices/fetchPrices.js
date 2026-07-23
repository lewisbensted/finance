import { CustomError } from "./../types/CustomError.js";
export const fetchPrices = async (symbols) => {
    const res = await fetch(`/api/prices?symbolsStr=${encodeURIComponent(symbols.join(","))}`);
    const response = (await res.json());
    if (!res.ok)
        throw new CustomError(response.error?.message ?? `Request failed ${res.status}`, res.status, response.error?.code);
    if (!response.data?.stocks || typeof response.data.stocks !== "object") {
        throw new Error("Invalid response from server");
    }
    return {
        stocks: new Map(Object.entries(response.data.stocks)),
        error: response.error ?? null,
    };
};
