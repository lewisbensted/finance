export const createHolding = ({symbol, companyName = null, shares = null, ...elements} ) => ({
    symbol: symbol,
    companyName: companyName,
    shares: shares,
    price: null,
    ...elements
})