import { ApiResponse, BatchErrorDTO } from "./../types/ApiResponse.js";
import { CustomError } from "./../types/CustomError.js";
import { Stock, StockResponse } from "./../types/Stock.js";

export const fetchPrices = async (symbols: string[]) => {
	const res = await fetch(`/api/prices?symbolsStr=${encodeURIComponent(symbols.join(","))}`);
	const response = (await res.json().catch(() => {
		throw new Error("Invalid response from server");
	})) as ApiResponse<StockResponse, BatchErrorDTO>;

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

	if (!response.data?.stocks && error?.code !== "OPERATION_FAILED") {
		throw new Error("Invalid response from server");
	}

	return {
		successful: new Map<string, Stock>(Object.entries(response.data?.stocks ?? {})),
		failed: error?.fields ?? {},
	};
};
