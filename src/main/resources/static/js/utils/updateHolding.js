export const updateHolding = (holding, stock) => {
    const { shares } = holding;
    holding.companyName ??= stock.companyName;
    holding.latestPrice = stock.latestPrice;
    holding.isPriceUpToDate = true;
    if (shares === undefined) {
        holding.value = undefined;
    }
    else if (shares === null) {
        holding.value = null;
    }
    else {
        holding.value = shares * stock.latestPrice;
    }
};
