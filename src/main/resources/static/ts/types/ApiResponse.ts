interface ItemError {
	code: string;
	message: string;
}

interface ErrorDTO {
	code: string;
	message: string;
	fields?: Record<string, ItemError>;
}

export interface ApiResponse<T> {
	data: T | null
	error: null | ErrorDTO
}