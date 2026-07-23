import { ItemError } from "./ApiResponse";

export class CustomError extends Error {   
	constructor(
		message: string,
		public httpCode: number,
		public code?: string,
		public fields?: Record<string, ItemError>
	){
		super(message);
	}
}