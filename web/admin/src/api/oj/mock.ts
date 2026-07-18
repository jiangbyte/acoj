type PageParams = {
  current?: number
  size?: number
  [key: string]: any
}

type ApiResponse<T> = Promise<{ data: T }>

export const now = '2026-07-18 10:30:00'

export const languageRows = [
  {
    id: 'lang-cpp17',
    key: 'cpp17',
    name: 'C++17',
    short_name: 'C++17',
    common_name: 'C++',
    ace_mode: 'c_cpp',
    pygments: 'cpp',
    extension: 'cpp',
    template: '#include <bits/stdc++.h>\nusing namespace std;\nint main() { return 0; }\n',
    compile_command: 'g++ -std=c++17 -O2 -pipe -DONLINE_JUDGE {source} -o {executable}',
    run_command: '{executable}',
    status: 'ENABLED',
    extra: {},
    created_at: now,
    updated_at: now,
  },
  {
    id: 'lang-python3',
    key: 'python3',
    name: 'Python 3',
    short_name: 'Py3',
    common_name: 'Python',
    ace_mode: 'python',
    pygments: 'python',
    extension: 'py',
    template: '',
    compile_command: null,
    run_command: 'python3 {source}',
    status: 'ENABLED',
    extra: {},
    created_at: now,
    updated_at: now,
  },
]

export const problemTags = [
  { id: 'tag-math', code: 'math', name: '数学', color: '#2080f0', description: '数学基础', status: 'ENABLED', created_at: now, updated_at: now },
  { id: 'tag-graph', code: 'graph', name: '图论', color: '#18a058', description: '图与最短路', status: 'ENABLED', created_at: now, updated_at: now },
  { id: 'tag-dp', code: 'dp', name: '动态规划', color: '#722ed1', description: 'DP', status: 'ENABLED', created_at: now, updated_at: now },
]

export const problemRows = [
  {
    id: 'prob-1001',
    code: 'P1001',
    title: 'A+B Problem',
    summary: '读取两个整数并输出它们的和。',
    description: '给定两个整数 a 和 b，计算 a + b。',
    input_description: '输入一行，包含两个整数 a 和 b。',
    output_description: '输出一个整数，表示 a + b。',
    source: 'ACOJ 入门',
    difficulty: 1,
    problem_type: 'PROGRAM',
    judge_mode: 'STANDARD',
    visibility: 'PUBLIC',
    time_limit_ms: 1000,
    memory_limit_kb: 262144,
    stack_limit_kb: null,
    output_limit_kb: 65536,
    points: 100,
    partial: false,
    allow_languages: ['cpp17', 'python3'],
    spj_language_id: null,
    spj_source: null,
    interactor_language_id: null,
    interactor_source: null,
    remote_provider: null,
    remote_problem_id: null,
    accepted_count: 328,
    submit_count: 421,
    ac_rate: 77.91,
    sort: 1,
    status: 'ENABLED',
    extra: {},
    created_at: now,
    updated_at: now,
  },
  {
    id: 'prob-2048',
    code: 'P2048',
    title: 'Shortest Path Lab',
    summary: '多源最短路实验题。',
    description: '给定一张带权有向图，回答若干最短路查询。',
    input_description: '第一行包含 n、m、q。',
    output_description: '对每个查询输出最短距离。',
    source: 'Graph Set',
    difficulty: 4,
    problem_type: 'PROGRAM',
    judge_mode: 'SPECIAL_JUDGE',
    visibility: 'PRIVATE',
    time_limit_ms: 2000,
    memory_limit_kb: 524288,
    stack_limit_kb: null,
    output_limit_kb: 131072,
    points: 100,
    partial: true,
    allow_languages: ['cpp17'],
    spj_language_id: 'lang-cpp17',
    spj_source: '#include <bits/stdc++.h>\nint main(){return 0;}\n',
    interactor_language_id: null,
    interactor_source: null,
    remote_provider: null,
    remote_problem_id: null,
    accepted_count: 45,
    submit_count: 153,
    ac_rate: 29.41,
    sort: 20,
    status: 'ENABLED',
    extra: { checker: 'token' },
    created_at: now,
    updated_at: now,
  },
  {
    id: 'prob-3001',
    code: 'P3001',
    title: '选择题练习',
    summary: '客观题评分示例。',
    description: '选择所有正确选项。',
    input_description: null,
    output_description: null,
    source: 'Objective Demo',
    difficulty: 2,
    problem_type: 'OBJECTIVE',
    judge_mode: 'OBJECTIVE',
    visibility: 'PUBLIC',
    time_limit_ms: 1000,
    memory_limit_kb: 262144,
    stack_limit_kb: null,
    output_limit_kb: null,
    points: 20,
    partial: true,
    allow_languages: [],
    spj_language_id: null,
    spj_source: null,
    interactor_language_id: null,
    interactor_source: null,
    remote_provider: null,
    remote_problem_id: null,
    accepted_count: 12,
    submit_count: 19,
    ac_rate: 63.16,
    sort: 30,
    status: 'ENABLED',
    extra: {},
    created_at: now,
    updated_at: now,
  },
]

export const problemTagRelations = [
  { id: 'ptr-1', problem_id: 'prob-1001', tag_id: 'tag-math' },
  { id: 'ptr-2', problem_id: 'prob-2048', tag_id: 'tag-graph' },
  { id: 'ptr-3', problem_id: 'prob-3001', tag_id: 'tag-math' },
]

export const problemSamples = [
  { id: 'sample-1', problem_id: 'prob-1001', input: '1 2\n', output: '3\n', explanation: '1 + 2 = 3', sort: 1, created_at: now, updated_at: now },
  { id: 'sample-2', problem_id: 'prob-1001', input: '-1 5\n', output: '4\n', explanation: null, sort: 2, created_at: now, updated_at: now },
  { id: 'sample-3', problem_id: 'prob-2048', input: '3 3 1\n1 2 4\n2 3 5\n1 3 10\n1 3\n', output: '9\n', explanation: '1 -> 2 -> 3', sort: 1, created_at: now, updated_at: now },
]

export const problemDatasets = [
  { id: 'dataset-1001-v1', problem_id: 'prob-1001', name: '正式数据', version: 'v1', is_active: true, data_zip_url: 'oj/problems/prob-1001/datasets/v1/data.zip', generator_url: null, checker: 'standard', checker_args: {}, output_prefix: 4096, output_limit: 67108864, unicode_enabled: true, extra: { sha256: 'demo-sha256-1001-v1' }, created_at: now, updated_at: now },
  { id: 'dataset-1001-pre', problem_id: 'prob-1001', name: '预评测数据', version: 'pretest', is_active: false, data_zip_url: 'oj/problems/prob-1001/datasets/pretest/data.zip', generator_url: null, checker: 'standard', checker_args: {}, output_prefix: 4096, output_limit: 67108864, unicode_enabled: true, extra: {}, created_at: now, updated_at: now },
  { id: 'dataset-2048-v1', problem_id: 'prob-2048', name: '正式数据', version: 'v1', is_active: true, data_zip_url: 'oj/problems/prob-2048/datasets/v1/data.zip', generator_url: null, checker: 'spj', checker_args: { eps: '1e-6' }, output_prefix: 4096, output_limit: 134217728, unicode_enabled: true, extra: {}, created_at: now, updated_at: now },
]

export const problemTestCases = [
  { id: 'case-1001-1', dataset_id: 'dataset-1001-v1', case_no: 1, case_type: 'NORMAL', input_file: 'cases/1.in', output_file: 'cases/1.out', input_inline: null, output_inline: null, generator_args: null, points: 20, is_pretest: true, batch_no: 1, batch_dependencies: [], time_limit_ms: 1000, memory_limit_kb: 262144, checker: null, checker_args: {}, sort: 1, created_at: now, updated_at: now },
  { id: 'case-1001-2', dataset_id: 'dataset-1001-v1', case_no: 2, case_type: 'NORMAL', input_file: 'cases/2.in', output_file: 'cases/2.out', input_inline: null, output_inline: null, generator_args: null, points: 80, is_pretest: false, batch_no: 2, batch_dependencies: [1], time_limit_ms: 1000, memory_limit_kb: 262144, checker: null, checker_args: {}, sort: 2, created_at: now, updated_at: now },
  { id: 'case-2048-1', dataset_id: 'dataset-2048-v1', case_no: 1, case_type: 'NORMAL', input_file: 'cases/1.in', output_file: 'cases/1.ans', input_inline: null, output_inline: null, generator_args: null, points: 10, is_pretest: true, batch_no: 1, batch_dependencies: [], time_limit_ms: 2000, memory_limit_kb: 524288, checker: 'spj', checker_args: { eps: '1e-6' }, sort: 1, created_at: now, updated_at: now },
]

export const problemAssets = [
  { id: 'asset-1001-statement', problem_id: 'prob-1001', asset_type: 'STATEMENT', name: '题面图片', url: '/files/oj/p1001.png', storage_key: 'oj/problems/prob-1001/assets/p1001.png', checksum: 'demo-checksum', size: 12420, version: 'v1', extra: {}, created_at: now, updated_at: now },
  { id: 'asset-2048-checker', problem_id: 'prob-2048', asset_type: 'CHECKER', name: 'spj.cpp', url: null, storage_key: 'oj/problems/prob-2048/checker/spj.cpp', checksum: 'demo-checker', size: 4096, version: 'v1', extra: {}, created_at: now, updated_at: now },
]

export const problemMembers = [
  { id: 'pm-1', problem_id: 'prob-1001', account_type: 'ADMIN', account_id: '1', role: 'AUTHOR', created_at: now, updated_at: now },
  { id: 'pm-2', problem_id: 'prob-2048', account_type: 'ADMIN', account_id: '1', role: 'CURATOR', created_at: now, updated_at: now },
]

export const objectiveAnswers = [
  { id: 'obj-3001', problem_id: 'prob-3001', answer_type: 'MULTIPLE', answer: { options: ['A', 'C'] }, score_rule: { mode: 'all_or_nothing' }, explanation: 'A 和 C 正确。', extra: {}, created_at: now, updated_at: now },
]

export const contestRows = [
  {
    id: 'contest-summer',
    key: 'SUMMER-2026',
    name: '2026 Summer Training',
    description: '暑期训练赛。',
    summary: 'ICPC 训练赛',
    start_at: '2026-07-20 09:00:00',
    end_at: '2026-07-20 14:00:00',
    duration_seconds: null,
    visibility: 'PUBLIC',
    contest_format: 'ICPC',
    format_config: { penalty: 20 },
    scoreboard_visibility: 'VISIBLE',
    is_rated: true,
    rating_floor: 800,
    rating_ceiling: 2600,
    access_code_hash: null,
    allow_virtual: true,
    freeze_at: '2026-07-20 13:00:00',
    unfreeze_at: '2026-07-20 14:30:00',
    status: 'ENABLED',
    extra: {},
    created_at: now,
    updated_at: now,
  },
  {
    id: 'contest-oi',
    key: 'OI-MOCK',
    name: 'OI 模拟赛',
    description: 'OI 赛制示例。',
    summary: '部分分与封榜配置',
    start_at: '2026-08-01 08:00:00',
    end_at: '2026-08-01 12:00:00',
    duration_seconds: 14400,
    visibility: 'PRIVATE',
    contest_format: 'OI',
    format_config: { submit_to_latest: true },
    scoreboard_visibility: 'AFTER_CONTEST',
    is_rated: false,
    rating_floor: null,
    rating_ceiling: null,
    access_code_hash: 'mocked-hash',
    allow_virtual: false,
    freeze_at: null,
    unfreeze_at: null,
    status: 'ENABLED',
    extra: {},
    created_at: now,
    updated_at: now,
  },
]

export const contestProblems = [
  { id: 'cp-1', contest_id: 'contest-summer', problem_id: 'prob-1001', label: 'A', points: 100, partial: false, is_pretest: false, max_submissions: null, sort: 1, created_at: now, updated_at: now },
  { id: 'cp-2', contest_id: 'contest-summer', problem_id: 'prob-2048', label: 'B', points: 100, partial: true, is_pretest: false, max_submissions: null, sort: 2, created_at: now, updated_at: now },
  { id: 'cp-3', contest_id: 'contest-oi', problem_id: 'prob-2048', label: 'T1', points: 100, partial: true, is_pretest: true, max_submissions: 30, sort: 1, created_at: now, updated_at: now },
]

export const contestMembers = [
  { id: 'cm-1', contest_id: 'contest-summer', account_type: 'ADMIN', account_id: '1', role: 'AUTHOR', created_at: now, updated_at: now },
  { id: 'cm-2', contest_id: 'contest-summer', account_type: 'PORTAL', account_id: '7481668347524943872', role: 'CONTESTANT', created_at: now, updated_at: now },
]

export const contestParticipations = [
  { id: 'part-1', contest_id: 'contest-summer', account_type: 'PORTAL', account_id: '7481668347524943872', participation_type: 'LIVE', started_at: '2026-07-20 09:00:12', ended_at: null, score: 200, penalty: 36, rank: 1, is_disqualified: false, format_data: { solved: 2 }, created_at: now, updated_at: now },
]

export const contestProblemResults = [
  { id: 'cpr-1', contest_id: 'contest-summer', participation_id: 'part-1', contest_problem_id: 'cp-1', best_submission_id: 'sub-1', score: 100, penalty: 12, attempts: 1, accepted_at: '2026-07-20 09:12:00', is_first_ac: true, extra: {}, created_at: now, updated_at: now },
  { id: 'cpr-2', contest_id: 'contest-summer', participation_id: 'part-1', contest_problem_id: 'cp-2', best_submission_id: 'sub-2', score: 100, penalty: 24, attempts: 2, accepted_at: '2026-07-20 09:24:00', is_first_ac: false, extra: {}, created_at: now, updated_at: now },
]

export const contestRatings = [
  { id: 'rating-1', contest_id: 'contest-summer', participation_id: 'part-1', account_type: 'PORTAL', account_id: '7481668347524943872', rank: 1, old_rating: 1500, new_rating: 1542, performance: 1680, rated_at: '2026-07-20 15:00:00', created_at: now, updated_at: now },
]

export const submissionRows = [
  {
    id: 'sub-1',
    problem_id: 'prob-1001',
    problem_code: 'P1001',
    account_type: 'PORTAL',
    account_id: '7481668347524943872',
    language_id: 'lang-cpp17',
    judge_mode: 'STANDARD',
    status: 'COMPLETED',
    result: 'AC',
    score: 100,
    time_ms: 12,
    memory_kb: 2048,
    current_case: 2,
    case_points: 100,
    case_total: 100,
    compile_output: null,
    judge_node_id: 'node-1',
    submitted_at: '2026-07-18 09:30:00',
    judged_at: '2026-07-18 09:30:04',
    rejudged_at: null,
    contest_id: 'contest-summer',
    contest_problem_id: 'cp-1',
    participation_id: 'part-1',
    is_pretest: false,
    is_archived: false,
    source_visibility: 'PUBLIC',
    extra: {},
    created_at: now,
    updated_at: now,
  },
  {
    id: 'sub-2',
    problem_id: 'prob-2048',
    problem_code: 'P2048',
    account_type: 'PORTAL',
    account_id: '7481668347524943872',
    language_id: 'lang-cpp17',
    judge_mode: 'SPECIAL_JUDGE',
    status: 'COMPLETED',
    result: 'WA',
    score: 40,
    time_ms: 82,
    memory_kb: 16000,
    current_case: 7,
    case_points: 40,
    case_total: 100,
    compile_output: null,
    judge_node_id: 'node-2',
    submitted_at: '2026-07-18 09:42:00',
    judged_at: '2026-07-18 09:42:06',
    rejudged_at: null,
    contest_id: 'contest-summer',
    contest_problem_id: 'cp-2',
    participation_id: 'part-1',
    is_pretest: false,
    is_archived: false,
    source_visibility: 'CONTEST',
    extra: {},
    created_at: now,
    updated_at: now,
  },
]

export const submissionCases = [
  { id: 'sc-1', submission_id: 'sub-1', case_no: 1, status: 'COMPLETED', result: 'AC', time_ms: 4, memory_kb: 2048, points: 20, total: 20, batch_no: 1, feedback: null, extended_feedback: null, output: '3\n', stderr: null, sort: 1, created_at: now, updated_at: now },
  { id: 'sc-2', submission_id: 'sub-1', case_no: 2, status: 'COMPLETED', result: 'AC', time_ms: 8, memory_kb: 2048, points: 80, total: 80, batch_no: 2, feedback: null, extended_feedback: null, output: '4\n', stderr: null, sort: 2, created_at: now, updated_at: now },
  { id: 'sc-3', submission_id: 'sub-2', case_no: 1, status: 'COMPLETED', result: 'AC', time_ms: 25, memory_kb: 16000, points: 40, total: 40, batch_no: 1, feedback: null, extended_feedback: null, output: '9\n', stderr: null, sort: 1, created_at: now, updated_at: now },
  { id: 'sc-4', submission_id: 'sub-2', case_no: 2, status: 'COMPLETED', result: 'WA', time_ms: 57, memory_kb: 16000, points: 0, total: 60, batch_no: 2, feedback: 'Wrong answer', extended_feedback: 'Expected shortest distance mismatch.', output: '10\n', stderr: null, sort: 2, created_at: now, updated_at: now },
]

export const submissionSources = [
  { id: 'src-1', submission_id: 'sub-1', source: '#include <bits/stdc++.h>\nusing namespace std;\nint main(){long long a,b;cin>>a>>b;cout<<a+b<<"\\n";}\n', source_hash: 'hash-sub-1', answer_files: [], size: 92, created_at: now, updated_at: now },
  { id: 'src-2', submission_id: 'sub-2', source: '#include <bits/stdc++.h>\nusing namespace std;\nint main(){return 0;}\n', source_hash: 'hash-sub-2', answer_files: [], size: 68, created_at: now, updated_at: now },
]

export const judgeTaskRows = [
  { id: 'task-1', submission_id: 'sub-1', problem_id: 'prob-1001', judge_node_id: 'node-1', task_type: 'JUDGE', priority: 10, status: 'DONE', attempts: 1, locked_at: '2026-07-18 09:30:01', started_at: '2026-07-18 09:30:01', finished_at: '2026-07-18 09:30:04', error: null, payload: { dataset_id: 'dataset-1001-v1' }, result_payload: { result: 'AC' }, created_at: now, updated_at: now },
  { id: 'task-2', submission_id: 'sub-2', problem_id: 'prob-2048', judge_node_id: 'node-2', task_type: 'JUDGE', priority: 5, status: 'DONE', attempts: 1, locked_at: '2026-07-18 09:42:01', started_at: '2026-07-18 09:42:01', finished_at: '2026-07-18 09:42:06', error: null, payload: { dataset_id: 'dataset-2048-v1' }, result_payload: { result: 'WA' }, created_at: now, updated_at: now },
]

export const judgeNodeRows = [
  { id: 'node-1', name: 'judge-node-a', auth_key_hash: 'hashed-secret-a', status: 'ENABLED', online: true, tier: 1, last_ip: '10.0.0.11', last_heartbeat_at: '2026-07-18 10:29:55', load: 0.42, supported_languages: ['cpp17', 'python3'], supported_modes: ['STANDARD', 'SPECIAL_JUDGE'], description: '主判题节点', extra: { cache_gb: 48 }, created_at: now, updated_at: now },
  { id: 'node-2', name: 'judge-node-b', auth_key_hash: 'hashed-secret-b', status: 'ENABLED', online: true, tier: 2, last_ip: '10.0.0.12', last_heartbeat_at: '2026-07-18 10:29:30', load: 0.73, supported_languages: ['cpp17'], supported_modes: ['STANDARD', 'SPECIAL_JUDGE', 'INTERACTIVE'], description: '高性能节点', extra: { cache_gb: 96 }, created_at: now, updated_at: now },
]

export const runtimeVersionRows = [
  { id: 'rv-1', judge_node_id: 'node-1', language_id: 'lang-cpp17', runtime_name: 'gcc', runtime_version: '13.2.0', priority: 10, created_at: now, updated_at: now },
  { id: 'rv-2', judge_node_id: 'node-1', language_id: 'lang-python3', runtime_name: 'python', runtime_version: '3.12.4', priority: 9, created_at: now, updated_at: now },
  { id: 'rv-3', judge_node_id: 'node-2', language_id: 'lang-cpp17', runtime_name: 'gcc', runtime_version: '14.1.0', priority: 20, created_at: now, updated_at: now },
]

export function createMockCrud<T extends { id: string }>(rows: T[]) {
  return {
    async page(params: PageParams = {}): ApiResponse<{
      records: T[]
      total: number
      current: number
      size: number
    }> {
      const current = Number(params.current ?? 1)
      const size = Number(params.size ?? 20)
      const filtered = filterRows(rows, params)
      const start = (current - 1) * size
      return mockResponse({
        records: filtered.slice(start, start + size),
        total: filtered.length,
        current,
        size,
      })
    },
    async detail(params: { id: string }): ApiResponse<T | null> {
      return mockResponse(rows.find((item) => item.id === params.id) ?? null)
    },
    async create(data: Partial<T>): ApiResponse<null> {
      rows.unshift({ ...(data as T), id: `mock-${Date.now()}`, created_at: now, updated_at: now })
      return mockResponse(null)
    },
    async update(data: Partial<T> & { id: string }): ApiResponse<null> {
      const index = rows.findIndex((item) => item.id === data.id)
      if (index > -1) {
        rows[index] = { ...rows[index], ...data, updated_at: now }
      }
      return mockResponse(null)
    },
    async remove(data: { ids: string[] }): ApiResponse<null> {
      const ids = new Set(data.ids)
      for (let index = rows.length - 1; index >= 0; index -= 1) {
        if (ids.has(rows[index].id)) {
          rows.splice(index, 1)
        }
      }
      return mockResponse(null)
    },
  }
}

export function pageOf<T extends Record<string, any>>(rows: T[], params: PageParams = {}) {
  const current = Number(params.current ?? 1)
  const size = Number(params.size ?? 20)
  const filtered = filterRows(rows, params)
  const start = (current - 1) * size
  return mockResponse({
    records: filtered.slice(start, start + size),
    total: filtered.length,
    current,
    size,
  })
}

export function mockResponse<T>(data: T): ApiResponse<T> {
  return Promise.resolve({ data })
}

function filterRows<T extends Record<string, any>>(rows: T[], params: PageParams) {
  const ignored = new Set(['current', 'size', 'pagination'])
  return rows.filter((row) =>
    Object.entries(params).every(([key, value]) => {
      if (ignored.has(key) || value === undefined || value === null || value === '') {
        return true
      }
      const rowValue = row[key]
      if (Array.isArray(rowValue)) {
        return rowValue.includes(value)
      }
      return String(rowValue ?? '').toLowerCase().includes(String(value).toLowerCase())
    }),
  )
}
