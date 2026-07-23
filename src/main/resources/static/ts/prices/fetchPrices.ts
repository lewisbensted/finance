import { ApiResponse, BatchErrorDTO } from "./../types/ApiResponse.js";
import { CustomError } from "./../types/CustomError.js";
import { Stock, StockResponse } from "./../types/Stock.js";

export const fetchPrices = async (symbols: string[]) => {
	const res = await fetch(`/api/prices?symbolsStr=${encodeURIComponent(symbols.join(","))}`);
	const response = (await res.json()) as ApiResponse<StockResponse, BatchErrorDTO>;
	if (!res.ok)
		throw new CustomError(
			response.error?.message ?? `Request failed ${res.status}`,
			res.status,
			response.error?.code,
		);
	if (!response.data?.stocks || typeof response.data.stocks !== "object") {
		throw new Error("Invalid response from server");
	}

	return {
		stocks: new Map<string, Stock>(Object.entries(response.data.stocks)),
		error: response.error ?? null,
	};
};
