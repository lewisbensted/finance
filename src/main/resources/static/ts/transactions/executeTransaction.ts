import { ApiResponse, BatchErrorDTO } from "../types/ApiResponse.js";
import { CustomError } from "../types/CustomError.js";
import { Transaction, TransactionResponse, TransactionType } from "../types/Transaction.js";

export const executeTransaction = async (buyRequests: Transaction[], type: TransactionType) => {
	const res = await fetch(`/api/${type.toLowerCase()}`, {
		method: "POST",
		headers: {
			"Content-Type": "application/json",
		},
		body: JSON.stringify(buyRequests),
	});
	const response = (await res.json().catch(() => {
		throw new Error("Invalid response from server");
	})) as ApiResponse<TransactionResponse, BatchErrorDTO>;

	const error = response.error
		? new CustomError(
			response.error.message,
			res.status,
			response.error.code,
			response.error.fields,
		)
		: null;

	if (!res.ok && error?.code !== "OPERATION_FAILED") {
		throw error ?? new Error(`Request failed ${res.status}`);
	}

	if (!response.data?.transactions && error?.code !== "OPERATION_FAILED") {
		throw new Error("Invalid response from server");
	}

	return {
		successful: new Map<string, Transaction>(Object.entries(response.data?.transactions ?? {})),
		updatedBalance: response.data?.balance,
		failed: error?.fields ?? {},
	};
};
