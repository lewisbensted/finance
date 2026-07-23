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

