export const displayToast = (successes, failures, operation) => {
    console.log("This will be converted into a Toast");
    if (successes.size > 0)
        console.log(`Successesful ${operation}:`);
    for (const transaction of successes.values())
        console.log(`${transaction.quantity} of ${transaction.symbol}`);
    if (failures && Object.keys(failures).length > 0) {
        console.log(`Unuccessesful ${operation}:`);
        for (const [symbol, itemError] of Object.entries(failures)) {
            console.warn(`Failed transaction ${symbol}: ${itemError.message}`);
            console.log(`${symbol}: ${itemError.message}`);
        }
    }
};
