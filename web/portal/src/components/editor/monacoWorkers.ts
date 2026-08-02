import editorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker'
import jsonWorker from 'monaco-editor/esm/vs/language/json/json.worker?worker'
import cssWorker from 'monaco-editor/esm/vs/language/css/css.worker?worker'
import htmlWorker from 'monaco-editor/esm/vs/language/html/html.worker?worker'
import tsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker'

type WorkerConstructor = new () => Worker

const workerFactory = (_: string, label: string): Worker => {
  if (label === 'json') {
    return new jsonWorker()
  }
  if (label === 'css' || label === 'scss' || label === 'less') {
    return new cssWorker()
  }
  if (label === 'html' || label === 'handlebars' || label === 'razor') {
    return new htmlWorker()
  }
  if (label === 'typescript' || label === 'javascript') {
    return new tsWorker()
  }
  return new editorWorker()
}

export function setupMonacoWorkers() {
  const globalScope = self as unknown as { MonacoEnvironment?: { getWorker?: WorkerConstructor } }
  globalScope.MonacoEnvironment = {
    getWorker: workerFactory as unknown as WorkerConstructor,
  }
}
