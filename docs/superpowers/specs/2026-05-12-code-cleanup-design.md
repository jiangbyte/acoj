# Code Cleanup: Reducing Redundancy in hei-admin-vue

## Goal

Reduce codebase size by ~20% (from ~10,959 lines) by removing dead code, extracting repeated CRUD/import-export patterns into composables, creating an API factory, and unifying duplicated logic.

## Changes

### 1. Dead Code Removal
- `utils/http/handle.ts`: remove unused `handleServiceResult`
- `hooks/usePermission.ts` + `hooks/index.ts`: remove entire files (never imported)
- Empty `<style scoped></style>` blocks in views

### 2. API Factory (`utils/http/crud.ts`)
- `createCrudApi(config)` generates: page, create, modify, remove, detail, export, template, importFile
- Also conditionally adds `tree()` when `config.hasTree` is true
- Existing API files get compressed, with special functions (grant, own-permission, etc.) kept inline

### 3. `useCrud` Composable (`hooks/useCrud.ts`)
- Wraps: tableRef, selectedKeys, rowSelection, handleSearch, resetSearch, handleDelete, handleBatchDelete, handleFormSuccess
- Accepts `{ deleteApi, name, onSuccess? }`
- Each page drops ~40 lines of boilerplate

### 4. `useImportExport` Composable (`hooks/useImportExport.ts`)
- Wraps: importOpen, exportOpen, templateLoading, importModalRef, handleDownloadTemplate, handleExportWithParams, handleImport
- Accepts `{ exportApi, templateApi, importApi, fileName, templateName, onSuccess? }`
- Each page drops ~35 lines of boilerplate

### 5. `AppTreePanel` Component
- Wraps AppSplitPanel + left tree + search + mobile drawer
- Props: fetchTree, fieldNames, title, searchPlaceholder
- org/index.vue and dict/index.vue each drop ~100 lines

### 6. Unify isMobile Detection
- Fix `store/app.ts` isMobileRef redundancy
- AppDrawerForm, AppSplitPanel → use `useAppStore().isMobile` instead of local mediaQuery

### 7. Bug Fixes
- dict/index.vue: deduplicate 3 repeated lines in handleTreeSelect
- alova.ts: remove unnecessary type assertion cast
