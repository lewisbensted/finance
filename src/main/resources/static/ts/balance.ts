import { ApiResponse } from "./types/ApiResponse.js";
import { CustomError } from "./types/CustomError.js";

export let balance: number | null;

export const fetchBalance = async () => {
	const res = await fetch("/api/balance");
	const response = (await res.json()) as ApiResponse<number>;

	if (!res.ok) {
		if (response.error?.code === "UNAUTHENTICATED") {
			sessionStorage.removeItem("USER_SESSION");
			balance = null;
			return null;
		}

		throw new CustomError(
			response.error?.message ?? `Request failed ${res.status}`,
			res.status,
			response.error?.code,
		);
	}

	if (typeof response.data !== "number" || Number.isNaN(response.data)) {
		throw new Error("Invalid or missing balance");
	}

	balance = response.data;
	return balance;
};

export const setBalance = (value: number) => {
	if (typeof value === "number" && !Number.isNaN(value)) {
		balance = value;
	} else {
		console.warn("Failed to update invalid balance:", value);
	}
};

