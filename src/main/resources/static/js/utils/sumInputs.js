export const sumInputs = (holdings) => holdings.reduce((total, { holding, row }) => {
    const quantity = Number(row.buyInput.value);
    return total + quantity * (holding.latestPrice ?? 0);
}, 0);
