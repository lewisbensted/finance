import { CustomError } from "./../types/CustomError.js";
export const buyShares = async (buyRequests) => {
    const res = await fetch("/api/buy", {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(buyRequests),
    });
    const response = (await res.json());
    if (!res.ok) {
        throw new CustomError(response.error?.message ?? `Request failed ${res.status}`, res.status, response.error?.code, response.error?.fields);
    }
    if (!response.data?.transactions ||
        typeof response.data.transactions !== "object" ||
        typeof response.data.balance !== "number") {
        throw new Error("Invalid response from server");
    }
    return {
        transactions: new Map(Object.entries(response.data.transactions)),
        updatedBalance: response.data.balance,
        error: response.error ?? null,
    };
};
