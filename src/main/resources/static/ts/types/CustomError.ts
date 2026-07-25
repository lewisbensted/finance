import { ErrorDTO } from "./ApiResponse.js";


export class CustomError extends Error {   
	constructor(
		message: string,
		public httpCode: number,
		public code?: string,
		public fields?: Record<string, ErrorDTO>
	){
		super(message);
	}
}