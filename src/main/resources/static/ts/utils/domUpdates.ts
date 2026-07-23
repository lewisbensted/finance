export const domUpdates: (() => void)[] = [];

export const applyDomUpdates = (domUpdates: (() => void)[]) => {
	const updates = domUpdates.splice(0);
	requestAnimationFrame(() => {
		for (const update of updates) {
			update();
		}
	});
};