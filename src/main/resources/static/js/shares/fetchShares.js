import { CustomError } from "./../types/CustomError.js";
export const fetchShares = async (symbol) => {
    const res = await fetch(`/api/holding?symbol=${encodeURIComponent(symbol)}`);
    const response = (await res.json());
    if (!res.ok) {
        switch (response.error?.code) {
            case "UNAUTHENTICATED":
                return null;
            case "NOT_FOUND":
                return 0;
            default:
                throw new CustomError(response.error?.message ?? `Request failed ${res.status}`, res.status, response.error?.code);
        }
    }
    if (response.data?.shares === undefined || typeof response.data.shares !== "number")
        throw new Error("Invalid response from server");
    return response.data.shares;
};
