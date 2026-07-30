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
	symbolCell: HTMLElement;
	sharesCell: HTMLElement;
	priceCell: HTMLElement;
	valueCell: HTMLElement;
	buyInput: HTMLInputElement;
	sellInput: HTMLInputElement;
}

export interface HoldingItem {
	holding: Holding;
	row: HoldingRow;
}

export interface HoldingDTO {
	symbol: string;
	companyName: string;
	shares: number;
}
