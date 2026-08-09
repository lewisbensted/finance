import { ApiResponse, BatchErrorDTO } from "../types/ApiResponse.js";
import { CustomError } from "../types/CustomError.js";
import { Transaction, TransactionResponse, TransactionType } from "../types/Transaction.js";
import { isErrorDTO } from "../utils/isErrorDTO.js";

export const executeTransaction = async (buyRequests: Transaction[], type: TransactionType) => {
	const res = await fetch(`/api/${type.toLowerCase()}`, {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
		},
		body: JSON.stringify(buyRequests),
	});
	const body: unknown = await res.json().catch(() => {
		throw new Error("Invalid response from server");
	});

	if (!res.ok) {
		if (isErrorDTO(body)) throw new CustomError(body.message, res.status, body.code);
		const response = body as ApiResponse<TransactionResponse, BatchErrorDTO>;
		if (response.error?.code !== "OPERATION_FAILED")
			throw new Error(`Request failed ${res.status}`);
	}

	const transaction = body as ApiResponse<TransactionResponse, BatchErrorDTO>;
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
