import { CustomError } from "../types/CustomError.js";
import { isErrorDTO } from "../utils/isErrorDTO.js";
export const executeTransaction = async (buyRequests, type) => {
    const res = await fetch(`/api/${type.toLowerCase()}`, {
        method: "POST",
        headers: {
            "Content-Type": "application/json",
        },
        body: JSON.stringify(buyRequests),
    });
    const body = await res.json().catch(() => {
        throw new Error("Invalid response from server");
    });
    if (!res.ok) {
        if (isErrorDTO(body))
            throw new CustomError(body.message, res.status, body.code);
        const response = body;
        if (response.error?.code !== "OPERATION_FAILED")
            throw new Error(`Request failed ${res.status}`);
    }
    const transaction = body;
    if (!transaction.data && transaction.error?.code !== "OPERATION_FAILED") {
        throw new Error("Invalid response from server");
    }
    const { data, error } = transaction;
    return {
        successful: new Map(Object.entries(data?.transactions ?? {})),
        updatedBalance: data?.balance,
        failed: error?.fields ?? {},
    };
};
