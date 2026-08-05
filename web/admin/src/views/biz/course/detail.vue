<script setup lang="tsx">
import { ojCourseApi, ojTeamApi } from '@/api'
import { displayValue, formatDateTime, hasPermission, toApiDateTime } from '@/utils'
import { NButton, NFlex, NTag } from 'naive-ui'
import { ProCard } from 'pro-naive-ui'
import { computed, onMounted, reactive } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()

const courseId = computed(() => String(route.query.id ?? ''))

const modeOptions = [
  { label: '实时', value: 'REALTIME' },
  { label: '异步', value: 'ASYNC' },
]

const state = reactive({
  loading: false,
  activeTab: 'basic',
  detail: {} as any,
  announcements: [] as any[],
  announcementsLoading: false,
  tasks: [] as any[],
  tasksLoading: false,
  teams: [] as any[],
  teamsLoading: false,
  teamShowModal: false,
  teamSubmitLoading: false,
  teamForm: { name: '', description: '', max_members: 50, member_account_ids_text: '' },
  actionLoading: false,
  // announcement form
  annShowModal: false,
  annSubmitLoading: false,
  annEditId: null as string | null,
  annForm: { title: '', content: '' },
  // task form
  taskShowModal: false,
  taskSubmitLoading: false,
  taskEditId: null as string | null,
  taskForm: {
    title: '',
    description: '',
    mode: 'ASYNC' as string,
    open_at: null as string | null,
    close_at: null as string | null,
    due_at: null as string | null,
    sort: 0,
  },
  // set problems
  problemsShowModal: false,
  problemsSubmitLoading: false,
  problemsTaskId: '',
  problemIdsText: '',
  // progress board
  progressShowModal: false,
  progressLoading: false,
  progressTaskId: '',
  progressRows: [] as any[],
})

const statusTagType: Record<string, 'default' | 'info' | 'success' | 'warning' | 'error'> = {
  DRAFT: 'default',
  PUBLISHED: 'success',
  ARCHIVED: 'warning',
}

onMounted(() => {
  if (!courseId.value) {
    window.$message.error('缺少课程 ID')
    goBack()
    return
  }
  state.activeTab = route.query.tab ? String(route.query.tab) : 'basic'
  void fetchDetail()
})

async function fetchDetail() {
  state.loading = true
  try {
    const response = await ojCourseApi.detail({ id: courseId.value })
    state.detail = response.data ?? {}
  } finally {
    state.loading = false
  }
}

async function fetchAnnouncements() {
  state.announcementsLoading = true
  try {
    const response = await ojCourseApi.announcementList({ course_id: courseId.value })
    state.announcements = response.data ?? []
  } finally {
    state.announcementsLoading = false
  }
}

async function fetchTasks() {
  state.tasksLoading = true
  try {
    const response = await ojCourseApi.taskList({ course_id: courseId.value })
    state.tasks = response.data ?? []
  } finally {
    state.tasksLoading = false
  }
}

async function fetchTeams() {
  state.teamsLoading = true
  try {
    const response = await ojTeamApi.page({ current: 1, size: 100, course_id: courseId.value, scope: 'COURSE' })
    state.teams = response.data?.records ?? []
  } finally {
    state.teamsLoading = false
  }
}

function onTabChange(name: string) {
  state.activeTab = name
  if (name === 'announcements' && !state.announcements.length) void fetchAnnouncements()
  if (name === 'tasks' && !state.tasks.length) void fetchTasks()
  if (name === 'teams') void fetchTeams()
}

async function submitCourseTeam() {
  if (!state.teamForm.name.trim()) {
    window.$message.warning('请输入小组名称')
    return
  }
  state.teamSubmitLoading = true
  try {
    const memberIds = state.teamForm.member_account_ids_text
      .split(/[\s,，]+/)
      .map((s: string) => s.trim())
      .filter(Boolean)
    await ojTeamApi.createCourseTeam({
      course_id: courseId.value,
      name: state.teamForm.name.trim(),
      description: state.teamForm.description || null,
      max_members: state.teamForm.max_members,
      member_account_ids: memberIds,
    })
    window.$message.success('课内小组已创建')
    state.teamShowModal = false
    state.teamForm = { name: '', description: '', max_members: 50, member_account_ids_text: '' }
    await fetchTeams()
  } finally {
    state.teamSubmitLoading = false
  }
}

function goBack() {
  router.push('/biz/course')
}

async function publishCourse() {
  state.actionLoading = true
  try {
    await ojCourseApi.publish({ id: courseId.value })
    window.$message.success('已发布')
    await fetchDetail()
  } finally {
    state.actionLoading = false
  }
}

async function archiveCourse() {
  state.actionLoading = true
  try {
    await ojCourseApi.archive({ id: courseId.value })
    window.$message.success('已归档')
    await fetchDetail()
  } finally {
    state.actionLoading = false
  }
}

function openAnnCreate() {
  state.annEditId = null
  state.annForm = { title: '', content: '' }
  state.annShowModal = true
}

function openAnnEdit(row: any) {
  state.annEditId = row.id
  state.annForm = { title: row.title ?? '', content: row.content ?? '' }
  state.annShowModal = true
}

async function submitAnn() {
  if (!state.annForm.title.trim()) {
    window.$message.warning('请输入标题')
    return
  }
  state.annSubmitLoading = true
  try {
    if (state.annEditId) {
      await ojCourseApi.announcementUpdate({ id: state.annEditId, ...state.annForm })
      window.$message.success('更新成功')
    } else {
      await ojCourseApi.announcementCreate({ course_id: courseId.value, ...state.annForm })
      window.$message.success('创建成功')
    }
    state.annShowModal = false
    await fetchAnnouncements()
  } finally {
    state.annSubmitLoading = false
  }
}

async function deleteAnn(id: string) {
  await ojCourseApi.announcementDelete({ id })
  window.$message.success('删除成功')
  await fetchAnnouncements()
}

function openTaskCreate() {
  state.taskEditId = null
  state.taskForm = { title: '', description: '', mode: 'ASYNC', open_at: null, close_at: null, due_at: null, sort: 0 }
  state.taskShowModal = true
}

function openTaskEdit(row: any) {
  state.taskEditId = row.id
  state.taskForm = {
    title: row.title ?? '',
    description: row.description ?? '',
    mode: row.mode ?? 'ASYNC',
    open_at: row.open_at ?? null,
    close_at: row.close_at ?? null,
    due_at: row.due_at ?? null,
    sort: row.sort ?? 0,
  }
  state.taskShowModal = true
}

async function submitTask() {
  if (!state.taskForm.title.trim()) {
    window.$message.warning('请输入任务标题')
    return
  }
  state.taskSubmitLoading = true
  try {
    const payload = {
      title: state.taskForm.title,
      description: state.taskForm.description || null,
      mode: state.taskForm.mode,
      open_at: toApiDateTime(state.taskForm.open_at),
      close_at: toApiDateTime(state.taskForm.close_at),
      due_at: toApiDateTime(state.taskForm.due_at),
      sort: state.taskForm.sort,
    }
    if (state.taskEditId) {
      await ojCourseApi.taskUpdate({ id: state.taskEditId, ...payload })
      window.$message.success('更新成功')
    } else {
      await ojCourseApi.taskCreate({ course_id: courseId.value, ...payload })
      window.$message.success('创建成功')
    }
    state.taskShowModal = false
    await fetchTasks()
  } finally {
    state.taskSubmitLoading = false
  }
}

async function deleteTask(id: string) {
  await ojCourseApi.taskDelete({ id })
  window.$message.success('删除成功')
  await fetchTasks()
}

async function publishTask(id: string) {
  await ojCourseApi.taskPublish({ id })
  window.$message.success('任务已发布')
  await fetchTasks()
}

async function closeTask(id: string) {
  await ojCourseApi.taskClose({ id })
  window.$message.success('任务已关闭')
  await fetchTasks()
}

function openSetProblems(row: any) {
  state.problemsTaskId = row.id
  state.problemIdsText = (row.problems ?? []).map((p: any) => p.problem_id).join('\n')
  state.problemsShowModal = true
}

function parseIds(text: string) {
  return text.split(/[\s,;，；]+/).map(s => s.trim()).filter(Boolean)
}

async function submitProblems() {
  state.problemsSubmitLoading = true
  try {
    await ojCourseApi.taskSetProblems({
      task_id: state.problemsTaskId,
      problem_ids: parseIds(state.problemIdsText),
    })
    window.$message.success('题目已设置')
    state.problemsShowModal = false
    await fetchTasks()
  } finally {
    state.problemsSubmitLoading = false
  }
}

async function openProgressBoard(taskId: string) {
  state.progressTaskId = taskId
  state.progressRows = []
  state.progressShowModal = true
  state.progressLoading = true
  try {
    const response = await ojCourseApi.taskProgressBoard({ task_id: taskId })
    state.progressRows = response.data ?? []
  } finally {
    state.progressLoading = false
  }
}

const annColumns = [
  { title: '标题', key: 'title' },
  { title: '状态', key: 'status', width: 100 },
  { title: '发布时间', key: 'published_at', width: 180, render: (row: any) => formatDateTime(row.published_at) },
  {
    title: '操作',
    key: 'actions',
    width: 180,
    render: (row: any) => hasPermission('biz:course:update') ? (
      <NFlex size={8}>
        {row.status !== 'PUBLISHED' ? (
          <NButton
            size="small"
            text
            type="success"
            onClick={async () => {
              await ojCourseApi.announcementUpdate({ id: row.id, status: 'PUBLISHED' })
              window.$message.success('已发布')
              await fetchAnnouncements()
            }}
          >
            发布
          </NButton>
        ) : null}
        <NButton size="small" text type="primary" onClick={() => openAnnEdit(row)}>编辑</NButton>
        <NButton size="small" text type="error" onClick={() => deleteAnn(row.id)}>删除</NButton>
      </NFlex>
    ) : null,
  },
]

const taskColumns = [
  { title: '标题', key: 'title' },
  { title: '模式', key: 'mode', width: 80 },
  { title: '状态', key: 'status', width: 90 },
  { title: '排序', key: 'sort', width: 60 },
  {
    title: '操作',
    key: 'actions',
    width: 280,
    render: (row: any) => hasPermission('biz:course:update') ? (
      <NFlex size={4} wrap>
        <NButton size="small" text type="primary" onClick={() => openTaskEdit(row)}>编辑</NButton>
        <NButton size="small" text type="info" onClick={() => openSetProblems(row)}>设题</NButton>
        <NButton size="small" text type="info" onClick={() => openProgressBoard(row.id)}>进度</NButton>
        {row.status === 'DRAFT' ? <NButton size="small" text type="success" onClick={() => publishTask(row.id)}>发布</NButton> : null}
        {row.status === 'PUBLISHED' ? <NButton size="small" text type="warning" onClick={() => closeTask(row.id)}>关闭</NButton> : null}
        <NButton size="small" text type="error" onClick={() => deleteTask(row.id)}>删除</NButton>
      </NFlex>
    ) : null,
  },
]

const progressColumns = [
  { title: '账户 ID', key: 'account_id' },
  { title: '已解决', key: 'solved_count', width: 80 },
  { title: '总数', key: 'total_count', width: 80 },
  { title: '状态', key: 'status', width: 100 },
  { title: '完成时间', key: 'finished_at', width: 180, render: (row: any) => formatDateTime(row.finished_at) },
]

const teamColumns = [
  { title: '名称', key: 'name' },
  { title: '邀请码', key: 'invite_code', width: 120 },
  { title: '成员数', key: 'member_count', width: 80 },
  { title: '状态', key: 'status', width: 100 },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    render: (row: any) => (
      <NButton
        size="small"
        text
        type="primary"
        onClick={() => router.push({ path: '/biz/team', query: { highlight: row.id } })}
      >
        管理
      </NButton>
    ),
  },
]
</script>

<template>
  <NFlex class="h-full min-h-0" vertical :size="12">
    <NFlex align="center" justify="space-between" class="shrink-0 px-2px">
      <NFlex align="center" :size="12">
        <NButton quaternary @click="goBack">返回</NButton>
        <span class="text-16px font-medium">课程详情</span>
        <NTag v-if="state.detail.status" size="small" :type="statusTagType[state.detail.status] ?? 'default'" :bordered="false">
          {{ state.detail.status }}
        </NTag>
        <span v-if="state.detail.name" class="text-gray-500">{{ state.detail.name }}</span>
      </NFlex>
      <NSpace v-if="hasPermission('biz:course:update')">
        <NButton v-if="state.detail.status === 'DRAFT'" type="primary" :loading="state.actionLoading" @click="publishCourse">发布</NButton>
        <NButton v-if="state.detail.status === 'PUBLISHED'" type="warning" :loading="state.actionLoading" @click="archiveCourse">归档</NButton>
      </NSpace>
    </NFlex>

    <NSpin :show="state.loading" class="min-h-0 flex-1">
      <ProCard class="h-full" content-class="h-full flex flex-col min-h-0" :segmented="{ content: true }">
        <NTabs :value="state.activeTab" type="line" class="h-full min-h-0 flex flex-col" @update:value="onTabChange">
          <NTabPane name="basic" tab="基本信息">
            <NScrollbar class="max-h-[calc(100vh-220px)] pr-8px">
              <NDescriptions label-placement="left" bordered :column="2">
                <NDescriptionsItem label="主键">{{ displayValue(state.detail.id) }}</NDescriptionsItem>
                <NDescriptionsItem label="开课模式">
                  <template v-if="state.detail.access_scope === 'OPEN'">
                    —
                  </template>
                  <template v-else>
                    {{ state.detail.binding_mode === 'SHARED' ? '合班上课' : state.detail.binding_mode === 'PER_CLASS' ? '分班开课' : displayValue(state.detail.binding_mode) }}
                  </template>
                </NDescriptionsItem>
                <NDescriptionsItem label="课程类型">
                  {{ state.detail.access_scope === 'OPEN' ? '公开课' : state.detail.access_scope === 'CLASS' ? '私有课' : displayValue(state.detail.access_scope) }}
                </NDescriptionsItem>
                <NDescriptionsItem label="所属班级" :span="2">
                  <template v-if="Array.isArray(state.detail.classes) && state.detail.classes.length">
                    {{ state.detail.classes.map((c: any) => `${c.name}（${c.code}）`).join('、') }}
                  </template>
                  <template v-else>
                    {{ displayValue((state.detail.class_ids || []).join('、') || state.detail.class_id) }}
                  </template>
                </NDescriptionsItem>
                <NDescriptionsItem label="课程名称">{{ displayValue(state.detail.name) }}</NDescriptionsItem>
                <NDescriptionsItem label="状态">{{ displayValue(state.detail.status) }}</NDescriptionsItem>
                <NDescriptionsItem label="可见性">
                  {{ state.detail.visibility === 'PUBLIC' ? '公开' : state.detail.visibility === 'PRIVATE' ? '私有' : displayValue(state.detail.visibility) }}
                </NDescriptionsItem>
                <NDescriptionsItem label="排序">{{ displayValue(state.detail.sort) }}</NDescriptionsItem>
                <NDescriptionsItem label="创建时间">{{ formatDateTime(state.detail.created_at) }}</NDescriptionsItem>
                <NDescriptionsItem label="简介" :span="2">
                  <div class="whitespace-pre-wrap">{{ displayValue(state.detail.summary) }}</div>
                </NDescriptionsItem>
              </NDescriptions>
            </NScrollbar>
          </NTabPane>

          <NTabPane name="announcements" tab="公告">
            <NFlex vertical :size="12">
              <NButton v-if="hasPermission('biz:course:update')" type="primary" size="small" @click="openAnnCreate">新增公告</NButton>
              <NSpin :show="state.announcementsLoading">
                <NDataTable :columns="annColumns" :data="state.announcements" size="small" />
              </NSpin>
            </NFlex>
          </NTabPane>

          <NTabPane name="tasks" tab="任务">
            <NFlex vertical :size="12">
              <NButton v-if="hasPermission('biz:course:update')" type="primary" size="small" @click="openTaskCreate">新增任务</NButton>
              <NSpin :show="state.tasksLoading">
                <NDataTable :columns="taskColumns" :data="state.tasks" size="small" :scroll-x="900" />
              </NSpin>
            </NFlex>
          </NTabPane>

          <NTabPane name="teams" tab="课内小组">
            <NFlex vertical :size="12">
              <NButton v-if="hasPermission('biz:team:create')" type="primary" size="small" @click="state.teamShowModal = true">新建小组</NButton>
              <NSpin :show="state.teamsLoading">
                <NDataTable :columns="teamColumns" :data="state.teams" size="small" />
              </NSpin>
            </NFlex>
          </NTabPane>
        </NTabs>
      </ProCard>
    </NSpin>

    <!-- Announcement modal -->
    <NModal v-model:show="state.annShowModal" preset="card" :title="state.annEditId ? '编辑公告' : '新增公告'" style="width: 640px">
      <NForm label-placement="left" label-width="80">
        <NFormItem label="标题">
          <NInput v-model:value="state.annForm.title" />
        </NFormItem>
        <NFormItem label="内容">
          <NInput v-model:value="state.annForm.content" type="textarea" :autosize="{ minRows: 4, maxRows: 10 }" />
        </NFormItem>
      </NForm>
      <template #action>
        <NSpace justify="end">
          <NButton @click="state.annShowModal = false">取消</NButton>
          <NButton type="primary" :loading="state.annSubmitLoading" @click="submitAnn">确认</NButton>
        </NSpace>
      </template>
    </NModal>

    <!-- Task modal -->
    <NModal v-model:show="state.taskShowModal" preset="card" :title="state.taskEditId ? '编辑任务' : '新增任务'" style="width: 720px">
      <NForm label-placement="left" label-width="100">
        <NFormItem label="标题">
          <NInput v-model:value="state.taskForm.title" />
        </NFormItem>
        <NFormItem label="描述">
          <NInput v-model:value="state.taskForm.description" type="textarea" :autosize="{ minRows: 2, maxRows: 6 }" />
        </NFormItem>
        <NFormItem label="模式">
          <NSelect v-model:value="state.taskForm.mode" :options="modeOptions" />
        </NFormItem>
        <NFormItem v-if="state.taskForm.mode === 'REALTIME'" label="开始时间">
          <NDatePicker v-model:formatted-value="state.taskForm.open_at" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" class="w-full" clearable />
        </NFormItem>
        <NFormItem v-if="state.taskForm.mode === 'REALTIME'" label="结束时间">
          <NDatePicker v-model:formatted-value="state.taskForm.close_at" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" class="w-full" clearable />
        </NFormItem>
        <NFormItem v-if="state.taskForm.mode === 'ASYNC'" label="截止时间">
          <NDatePicker v-model:formatted-value="state.taskForm.due_at" type="datetime" value-format="yyyy-MM-dd HH:mm:ss" class="w-full" clearable />
        </NFormItem>
        <NFormItem label="排序">
          <NInputNumber v-model:value="state.taskForm.sort" class="w-full" />
        </NFormItem>
      </NForm>
      <template #action>
        <NSpace justify="end">
          <NButton @click="state.taskShowModal = false">取消</NButton>
          <NButton type="primary" :loading="state.taskSubmitLoading" @click="submitTask">确认</NButton>
        </NSpace>
      </template>
    </NModal>

    <!-- Set problems modal -->
    <NModal v-model:show="state.problemsShowModal" preset="card" title="设置题目" style="width: 640px">
      <NInput v-model:value="state.problemIdsText" type="textarea" placeholder="题目 ID，多个用逗号或换行分隔" :autosize="{ minRows: 6, maxRows: 12 }" />
      <template #action>
        <NSpace justify="end">
          <NButton @click="state.problemsShowModal = false">取消</NButton>
          <NButton type="primary" :loading="state.problemsSubmitLoading" @click="submitProblems">确认</NButton>
        </NSpace>
      </template>
    </NModal>

    <!-- Progress board modal -->
    <NModal v-model:show="state.progressShowModal" preset="card" title="任务进度看板" style="width: 800px">
      <NSpin :show="state.progressLoading">
        <NDataTable :columns="progressColumns" :data="state.progressRows" size="small" />
      </NSpin>
    </NModal>

    <!-- Course team modal -->
    <NModal v-model:show="state.teamShowModal" preset="card" title="新建课内小组" style="width: 640px">
      <NForm label-placement="left" label-width="100">
        <NFormItem label="名称">
          <NInput v-model:value="state.teamForm.name" />
        </NFormItem>
        <NFormItem label="简介">
          <NInput v-model:value="state.teamForm.description" type="textarea" :autosize="{ minRows: 2, maxRows: 5 }" />
        </NFormItem>
        <NFormItem label="人数上限">
          <NInputNumber v-model:value="state.teamForm.max_members" :min="2" :max="500" class="w-full" />
        </NFormItem>
        <NFormItem label="成员账户">
          <NInput
            v-model:value="state.teamForm.member_account_ids_text"
            type="textarea"
            placeholder="Portal 账户 ID，多个用逗号或换行分隔（须为班级成员）"
            :autosize="{ minRows: 3, maxRows: 8 }"
          />
        </NFormItem>
      </NForm>
      <template #action>
        <NSpace justify="end">
          <NButton @click="state.teamShowModal = false">取消</NButton>
          <NButton type="primary" :loading="state.teamSubmitLoading" @click="submitCourseTeam">确认</NButton>
        </NSpace>
      </template>
    </NModal>
  </NFlex>
</template>
