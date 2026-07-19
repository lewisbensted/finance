export const createHolding = (symbol) => ({
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
