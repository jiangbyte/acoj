<script lang="ts">
import { editorProps } from './CodeEditorConfig'
import jsonWorker from 'monaco-editor/esm/vs/language/json/json.worker?worker'
import cssWorker from 'monaco-editor/esm/vs/language/css/css.worker?worker'
import htmlWorker from 'monaco-editor/esm/vs/language/html/html.worker?worker'
import tsWorker from 'monaco-editor/esm/vs/language/typescript/ts.worker?worker'
import EditorWorker from 'monaco-editor/esm/vs/editor/editor.worker?worker'
import * as monaco from 'monaco-editor'

export default defineComponent({
  name: 'monacoEditor',
  props: editorProps,
  emits: ['update:modelValue', 'change', 'editor-mounted'],
  setup(props, { emit }) {
    // eslint-disable-next-line no-restricted-globals
    self.MonacoEnvironment = {
      getWorker(_: string, label: string) {
        if (label === 'json') {
          // eslint-disable-next-line new-cap
          return new jsonWorker()
        }
        if (['css', 'scss', 'less'].includes(label)) {
          // eslint-disable-next-line new-cap
          return new cssWorker()
        }
        if (['html', 'handlebars', 'razor'].includes(label)) {
          // eslint-disable-next-line new-cap
          return new htmlWorker()
        }
        if (['typescript', 'javascript'].includes(label)) {
          // eslint-disable-next-line new-cap
          return new tsWorker()
        }
        return new EditorWorker()
      },
    }
    let editor: monaco.editor.IStandaloneCodeEditor
    const codeEditBox = ref()
    const init = () => {
      monaco.languages.typescript.javascriptDefaults.setDiagnosticsOptions({
        noSemanticValidation: true,
        noSyntaxValidation: false,
      })
      monaco.languages.typescript.javascriptDefaults.setCompilerOptions({
        target: monaco.languages.typescript.ScriptTarget.ES2020,
        allowNonTsExtensions: true,
      })
      editor = monaco.editor.create(codeEditBox.value, {
        value: props.modelValue,
        language: props.language,
        theme: props.theme,
        ...props.options,
      })
      editor.onDidChangeModelContent(() => {
        const value = editor.getValue()
        emit('update:modelValue', value)
        emit('change', value)
      })
      // eslint-disable-next-line vue/custom-event-name-casing
      emit('editor-mounted', editor)
    }
    watch(
      () => props.modelValue,
      (newValue) => {
        if (editor) {
          const value = editor.getValue()
          if (newValue !== value) {
            editor.setValue(newValue)
          }
        }
      },
    )
    watch(
      () => props.options,
      (newValue) => {
        editor.updateOptions(newValue)
      },
      { deep: true },
    )
    watch(
      () => props.language,
      (newValue) => {
        monaco.editor.setModelLanguage(editor.getModel()!, newValue)
      },
    )
    onBeforeUnmount(() => {
      editor.dispose()
    })
    onMounted(() => {
      init()
    })
    return { codeEditBox }
  },
})
</script>

<template>
  <div ref="codeEditBox" class="codeEditBox" />
</template>

<style lang="scss" scoped>
.codeEditBox {
  width: v-bind(width);
  height: v-bind(height);
}
</style>
