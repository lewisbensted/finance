export class CustomError extends Error {   
	constructor(
		message: string,
		public httpCode: number,
		public code?: string,
	){
		super(message);
		this.httpCode = httpCode;
		this.code = code;
	}
}