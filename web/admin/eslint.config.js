import js from '@eslint/js'
import prettier from 'eslint-config-prettier'
import tseslint from 'typescript-eslint'
import vue from 'eslint-plugin-vue'

const browserGlobals = {
  Blob: 'readonly',
  console: 'readonly',
  document: 'readonly',
  FormData: 'readonly',
  MediaQueryList: 'readonly',
  MediaQueryListEvent: 'readonly',
  MouseEvent: 'readonly',
  Response: 'readonly',
  URLSearchParams: 'readonly',
  window: 'readonly',
}

export default tseslint.config(
  {
    ignores: ['dist', 'node_modules'],
  },
  {
    languageOptions: {
      globals: browserGlobals,
    },
  },
  js.configs.recommended,
  ...tseslint.configs.recommended,
  ...vue.configs['flat/recommended'],
  prettier,
  {
    files: ['**/*.vue'],
    languageOptions: {
      parserOptions: {
        parser: tseslint.parser,
        extraFileExtensions: ['.vue'],
      },
    },
  },
)
