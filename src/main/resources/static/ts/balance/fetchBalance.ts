import { CustomError } from "../types/CustomError.js";
import { isErrorDTO } from "../utils/isErrorDTO.js";

export const fetchBalance = async () => {
    const res = await fetch("/api/balance");
    const body: unknown = await res.json().catch(() => {
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
