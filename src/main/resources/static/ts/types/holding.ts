export interface Holding {
	symbol: string;
	companyName: string | null | undefined;
	shares: number | null | undefined;
	latestPrice: number | undefined;
	isPriceUpToDate: boolean | undefined;
	value: number | null | undefined;
}

interface HoldingRow {
	nameCell: HTMLElement | null;
	symbolCell: HTMLElement | null;
	sharesCell: HTMLElement | null;
	priceCell: HTMLElement | null;
	valueCell: HTMLElement | null;
	buyInput: HTMLInputElement | null;
	sellInput: HTMLInputElement | null;
}

export interface HoldingItem {
	holding: Holding;
	row: HoldingRow;
}

export interface HoldingDTO {
	symbol: string;
	companyName: string | null;
	shares: number | null;
}

export const createHolding = (symbol: string): HoldingItem => ({
	holding: {
		symbol: symbol,
		companyName: undefined,
		shares: undefined,
		latestPrice: undefined,
		isPriceUpToDate: undefined,
		value: undefined,
	},
	row: {
		nameCell: document.querySelector(".name"),
		symbolCell: document.querySelector(".symbol"),
		sharesCell: document.querySelector(".current-shares"),
		priceCell: document.querySelector(".price"),
		valueCell: document.querySelector(".value"),
		buyInput: document.querySelector(".buy-input"),
		sellInput: document.querySelector(".sell-input"),
	},
});
