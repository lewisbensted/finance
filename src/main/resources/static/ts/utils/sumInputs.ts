import { HoldingItem } from "../types/Holding";

export const sumInputs = (holdings: HoldingItem[]): number =>
	holdings.reduce((total, { holding, row }) => {
		const quantity = Number(row.buyInput.value);
		return total + quantity * (holding.latestPrice ?? 0);
	}, 0);
