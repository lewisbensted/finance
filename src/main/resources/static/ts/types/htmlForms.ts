export type RegisterForm = HTMLFormElement & {
	username: HTMLInputElement;
	lastName: HTMLInputElement;
	email: HTMLInputElement;
	password: HTMLInputElement;
	passwordRepeat: HTMLInputElement;
};

export type loginForm = HTMLFormElement & {
	username: HTMLInputElement;
	password: HTMLInputElement;
};