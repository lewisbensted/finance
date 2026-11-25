export const domUpdates = [];
export const applyDomUpdates = (domUpdates) => {
    const updates = domUpdates.splice(0);
    requestAnimationFrame(() => {
        for (const update of updates) {
            update();
        }
    });
};
