export const collectInputs = (holdings, type) => {
    const transactionRequests = [];
    for (const holding of holdings) {
        const { buyInput, sellInput } = holding.row;
        const input = type === "buy" ? buyInput : sellInput;
        const quantity = Number(input?.value);
        if (quantity < 0 || !Number.isInteger(quantity) || !input?.checkValidity()) {
            return { invalidInput: true, transactionRequests: null };
        }
        if (quantity > 0)
            transactionRequests.push({ symbol: holding.holding.symbol, quantity: quantity });
    }
    return { invalidInput: false, transactionRequests: transactionRequests };
};
