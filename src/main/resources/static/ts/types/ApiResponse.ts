export interface ErrorDTO {
	code: string;
	message: string;
}

export interface BatchErrorDTO extends ErrorDTO {
	fields: Record<string, ErrorDTO>;
}

export interface ApiResponse<T, E extends ErrorDTO = ErrorDTO> {
	data: T | null;
	error: null | E;
}
