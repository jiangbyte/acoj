import {
  createMockCrud,
  judgeNodeRows,
  judgeTaskRows,
  languageRows,
  runtimeVersionRows,
} from './mock'

const taskCrud = createMockCrud(judgeTaskRows)
const nodeCrud = createMockCrud(judgeNodeRows)
const languageCrud = createMockCrud(languageRows)
const runtimeVersionCrud = createMockCrud(runtimeVersionRows)

export const task = taskCrud
export const node = nodeCrud
export const language = languageCrud
export const runtimeVersion = runtimeVersionCrud
