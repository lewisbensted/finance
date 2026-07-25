import { isErrorDTO } from "../utils/isErrorDTO.js";
import { CustomError } from "./../types/CustomError.js";
export const fetchShares = async (symbol) => {
    const res = await fetch(`/api/holding?symbol=${encodeURIComponent(symbol)}`);
    const body = await res.json().catch(() => {
        throw new Error("Invalid response from server");
    });
    if (!res.ok) {
        if (!isErrorDTO(body)) {
            throw new Error(`Request failed ${res.status}`);
        }
        switch (body.code) {
            case "UNAUTHENTICATED":
                return null;
            case "NOT_FOUND":
                return 0;
            default:
                throw new CustomError(body.message, res.status, body.code);
        }
    }
    if (body.shares === undefined || typeof body.shares !== "number")
        throw new Error("Invalid response from server");
    return body.shares;
};
