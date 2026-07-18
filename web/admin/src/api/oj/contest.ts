import {
  contestMembers,
  contestParticipations,
  contestProblemResults,
  contestProblems,
  contestRatings,
  contestRows,
  createMockCrud,
  mockResponse,
} from './mock'

const contestCrud = createMockCrud(contestRows)

export const page = contestCrud.page
export const detail = contestCrud.detail
export const create = contestCrud.create
export const update = contestCrud.update
export const remove = contestCrud.remove

export async function workspace(params: { id: string }) {
  return mockResponse({
    contest: contestRows.find((item) => item.id === params.id) ?? null,
    problems: contestProblems.filter((item) => item.contest_id === params.id),
    members: contestMembers.filter((item) => item.contest_id === params.id),
    participations: contestParticipations.filter((item) => item.contest_id === params.id),
    problem_results: contestProblemResults.filter((item) => item.contest_id === params.id),
    ratings: contestRatings.filter((item) => item.contest_id === params.id),
  })
}
