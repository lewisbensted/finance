export interface Holding {
    symbol: string;
    companyName: string | null;
    shares: number | null;
    latestPrice: number | null;
     value: number | null;

    nameCell: HTMLElement | null;
    symbolCell: HTMLElement | null;
    sharesCell: HTMLElement | null;
    priceCell: HTMLElement | null; 
    valueCell: HTMLElement | null;
    buyInput: HTMLInputElement | null;
    sellInput: HTMLInputElement | null;
}