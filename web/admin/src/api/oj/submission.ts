import {
  createMockCrud,
  mockResponse,
  submissionCases,
  submissionRows,
  submissionSources,
} from './mock'

const submissionCrud = createMockCrud(submissionRows)

export const page = submissionCrud.page
export const detail = submissionCrud.detail
export const create = submissionCrud.create
export const update = submissionCrud.update
export const remove = submissionCrud.remove

export async function fullDetail(params: { id: string }) {
  return mockResponse({
    submission: submissionRows.find((item) => item.id === params.id) ?? null,
    cases: submissionCases.filter((item) => item.submission_id === params.id),
    source: submissionSources.find((item) => item.submission_id === params.id) ?? null,
  })
}
