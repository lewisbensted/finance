import js from "@eslint/js";
import globals from "globals";
import tseslint from "typescript-eslint";
import stylistic from "@stylistic/eslint-plugin";

export default tseslint.config(
	{
		ignores: [
			"node_modules",
			"dist",
			"build",
		],
	},

	js.configs.recommended,

	{
		files: ["**/*.{ts,js}"],

		extends: [
			tseslint.configs.strictTypeChecked,
			tseslint.configs.stylisticTypeChecked,
		],

		languageOptions: {
			parser: tseslint.parser,

			parserOptions: {
				project: [
					"./tsconfig.json",
				],
			},

			globals: {
				...globals.browser,
				...globals.node,
			},
		},

		rules: {
			// Async functions used where void callbacks are expected
			"@typescript-eslint/no-misused-promises": [
				"warn",
				{
					checksVoidReturn: false,
				},
			],

			// Allow numbers in template strings
			"@typescript-eslint/restrict-template-expressions": [
				"warn",
				{
					allowNumber: true,
				},
			],

			// Allow deliberate non-null assertions
			"@typescript-eslint/no-non-null-assertion": "off",

			// Unused vars, ignore args starting with _
			"@typescript-eslint/no-unused-vars": [
				"warn",
				{
					argsIgnorePattern: "^_",
					varsIgnorePattern: "^_",
				},
			],
		},
	},

	{
		files: ["**/*.{ts, js}"],

		plugins: {
			"@stylistic": stylistic,
		},

		rules: {
			// Style
			"@stylistic/indent": [
				"warn",
				"tab",
			],

			"@stylistic/linebreak-style": [
				"warn",
				"windows",
			],

			"@stylistic/quotes": [
				"warn",
				"double",
			],

			"@stylistic/semi": [
				"warn",
				"always",
			],

			// JS rules
			eqeqeq: [
				"warn",
				"smart",
			],

			"arrow-body-style": [
				"warn",
				"as-needed",
			],
		},
	},
);