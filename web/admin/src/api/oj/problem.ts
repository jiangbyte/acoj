import {
  createMockCrud,
  mockResponse,
  objectiveAnswers,
  problemAssets,
  problemDatasets,
  problemMembers,
  problemRows,
  problemSamples,
  problemTagRelations,
  problemTags,
  problemTestCases,
} from './mock'

const problemCrud = createMockCrud(problemRows)

export const page = problemCrud.page
export const detail = problemCrud.detail
export const create = problemCrud.create
export const update = problemCrud.update
export const remove = problemCrud.remove

export async function workspace(params: { id: string }) {
  const problem = problemRows.find((item) => item.id === params.id) ?? null
  const datasets = problemDatasets.filter((item) => item.problem_id === params.id)
  const tagRelations = problemTagRelations.filter((item) => item.problem_id === params.id)
  return mockResponse({
    problem,
    samples: problemSamples.filter((item) => item.problem_id === params.id),
    datasets,
    test_cases: problemTestCases.filter((item) =>
      datasets.some((dataset) => dataset.id === item.dataset_id),
    ),
    tags: problemTags.filter((tag) => tagRelations.some((relation) => relation.tag_id === tag.id)),
    tag_relations: tagRelations,
    assets: problemAssets.filter((item) => item.problem_id === params.id),
    members: problemMembers.filter((item) => item.problem_id === params.id),
    objective_answers: objectiveAnswers.filter((item) => item.problem_id === params.id),
  })
}
