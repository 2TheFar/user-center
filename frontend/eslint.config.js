import js from '@eslint/js';
import vue from 'eslint-plugin-vue';
import prettier from 'eslint-config-prettier';
import tseslint from 'typescript-eslint';

const browserGlobals = {
  Blob: 'readonly',
  Event: 'readonly',
  File: 'readonly',
  FormData: 'readonly',
  HTMLCanvasElement: 'readonly',
  HTMLElement: 'readonly',
  HTMLImageElement: 'readonly',
  HTMLInputElement: 'readonly',
  PointerEvent: 'readonly',
  URL: 'readonly',
  WheelEvent: 'readonly',
  document: 'readonly',
};

export default [
  {
    ignores: ['dist', 'target', 'node_modules', '.mvn', '.idea'],
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
    files: ['src/**/*.vue'],
    languageOptions: {
      parserOptions: {
        parser: tseslint.parser,
      },
    },
  },
  {
    rules: {
      'vue/multi-word-component-names': 'off',
      'vue/no-v-html': 'off',
    },
  },
];
