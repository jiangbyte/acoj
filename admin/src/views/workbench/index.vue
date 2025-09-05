<script setup lang="ts">
import { NButton, NCard, NCollapse, NCollapseItem, NDataTable, NGi, NGrid, NList, NListItem, NSpace, NTag, NText, NThing } from 'naive-ui'
import { useMonitorFetch } from '@/composables'

// 定义类型
interface InstanceDetail {
  ip: string
  port: number
  ephemeral: boolean
  weight: number
  healthy: boolean
  metadata: Record<string, string>
}

interface ClusterDetail {
  clusterName: string
  instances: InstanceDetail[]
}

interface ServiceDetailStatus {
  serviceName: string
  groupName: string
  clusterCount: number
  instanceCount: number
  healthyInstanceCount: number
  protectionThresholdEnabled: boolean
  clusters: ClusterDetail[]
}

// 使用ref存储服务状态数据
const servicesStatus = ref<ServiceDetailStatus[]>([])

const { healthStatus } = useMonitorFetch()
async function loadData() {
  const { data } = await healthStatus()
  servicesStatus.value = data || []
}
loadData()

// 获取服务健康状态
function getServiceHealthStatus(service: ServiceDetailStatus) {
  if (service.healthyInstanceCount === service.instanceCount) {
    return 'running'
  }
  else if (service.healthyInstanceCount === 0) {
    return 'down'
  }
  else {
    return 'partial'
  }
}

function getStatusColor(status: string) {
  switch (status) {
    case 'running': return 'success'
    case 'partial': return 'warning'
    case 'down': return 'error'
    default: return 'default'
  }
}

function getStatusText(status: string) {
  switch (status) {
    case 'running': return '运行正常'
    case 'partial': return '部分运行'
    case 'down': return '服务宕机'
    default: return '未知状态'
  }
}

// 实例表格列定义
const instanceColumns = [
  {
    title: 'IP',
    key: 'ip',
  },
  {
    title: '端口',
    key: 'port',
  },
  {
    title: '类型',
    key: 'ephemeral',
    render: (row: InstanceDetail) => row.ephemeral ? '临时' : '持久',
  },
  {
    title: '权重',
    key: 'weight',
  },
  {
    title: '状态',
    key: 'healthy',
    render: (row: InstanceDetail) => {
      return h(
        NTag,
        {
          type: row.healthy ? 'success' : 'error',
          bordered: false,
        },
        { default: () => row.healthy ? '健康' : '异常' },
      )
    },
  },
]
</script>

<template>
  <NCard>
    <NGrid
      :x-gap="16"
      :y-gap="16"
    >
      <NGi :span="16">
        <NSpace
          vertical
          :size="16"
        >
          <NCard title="快速操作">
            <NGrid :cols="6" :x-gap="8" :y-gap="8">
              <NGi>
                <NButton type="primary" strong block>
                  创建新题目
                </NButton>
              </NGi>
              <NGi>
                <NButton type="info" strong block>
                  人工执行
                </NButton>
              </NGi>
              <NGi>
                <NButton type="success" strong block>
                  限时题集
                </NButton>
              </NGi>
              <NGi>
                <NButton type="warning" strong block>
                  系统设置
                </NButton>
              </NGi>
              <NGi>
                <NButton type="error" strong block>
                  清理沙箱
                </NButton>
              </NGi>
              <NGi>
                <NButton type="default" strong block>
                  查看日志
                </NButton>
              </NGi>
            </NGrid>
          </NCard>
          <NCard title="服务详细信息">
            <NScrollbar style="max-height: 420px;">
              <NCollapse>
                <NCollapseItem v-for="service in servicesStatus" :key="service.serviceName" :title="service.serviceName">
                  <NSpace vertical>
                    <NGrid :cols="4" :x-gap="12" :y-gap="12">
                      <NGi>
                        <NCard content-style="padding: 12px;">
                          <NSpace vertical align="center">
                            <NText depth="3">
                              分组名称
                            </NText>
                            <NText strong>
                              {{ service.groupName ? service.groupName : '未分组' }}
                            </NText>
                          </NSpace>
                        </NCard>
                      </NGi>
                      <NGi>
                        <NCard content-style="padding: 12px;">
                          <NSpace vertical align="center">
                            <NText depth="3">
                              集群数量
                            </NText>
                            <NText strong>
                              {{ service.clusterCount }}
                            </NText>
                          </NSpace>
                        </NCard>
                      </NGi>
                      <NGi>
                        <NCard content-style="padding: 12px;">
                          <NSpace vertical align="center">
                            <NText depth="3">
                              实例总数
                            </NText>
                            <NText strong>
                              {{ service.instanceCount }}
                            </NText>
                          </NSpace>
                        </NCard>
                      </NGi>
                      <NGi>
                        <NCard content-style="padding: 12px;">
                          <NSpace vertical align="center">
                            <NText depth="3">
                              健康实例
                            </NText>
                            <NText strong :type="service.healthyInstanceCount === service.instanceCount ? 'success' : 'error'">
                              {{ service.healthyInstanceCount }}
                            </NText>
                          </NSpace>
                        </NCard>
                      </NGi>
                    </NGrid>

                    <NText strong>
                      集群详情
                    </NText>
                    <NCollapse v-for="cluster in service.clusters" :key="cluster.clusterName" accordion>
                      <NCollapseItem :title="`集群: ${cluster.clusterName} (${cluster.instances.length}个实例)`">
                        <NDataTable
                          :columns="instanceColumns"
                          :data="cluster.instances"
                          :bordered="false"
                        />
                      </NCollapseItem>
                    </NCollapse>
                  </NSpace>
                </NCollapseItem>
              </NCollapse>
            </NScrollbar>
          </NCard>
        </NSpace>
      </NGi>
      <NGi :span="8">
        <NSpace
          vertical
          :size="16"
        >
          <NCard title="服务概览">
            <NGrid :cols="2" :x-gap="8" :y-gap="8">
              <NGi>
                <NCard content-style="padding: 12px;">
                  <NSpace vertical align="center">
                    <NText depth="3">
                      运行中服务
                    </NText>
                    <NText strong style="font-size: 24px;">
                      {{ servicesStatus.filter(s => getServiceHealthStatus(s) === 'running').length }}
                    </NText>
                    <NText depth="3">
                      共{{ servicesStatus.length }}个服务
                    </NText>
                  </NSpace>
                </NCard>
              </NGi>
              <NGi>
                <NCard content-style="padding: 12px;">
                  <NSpace vertical align="center">
                    <NText depth="3">
                      异常服务
                    </NText>
                    <NText strong style="font-size: 24px; color: var(--error-color)">
                      {{ servicesStatus.filter(s => getServiceHealthStatus(s) !== 'running').length }}
                    </NText>
                    <NText depth="3">
                      需立即处理
                    </NText>
                  </NSpace>
                </NCard>
              </NGi>
            </NGrid>
          </NCard>

          <NCard title="服务详情">
            <NList hoverable>
              <NListItem v-for="service in servicesStatus" :key="service.serviceName">
                <template #prefix>
                  <NTag
                    :bordered="false"
                    :type="getStatusColor(getServiceHealthStatus(service))"
                    size="small"
                  >
                    {{ getStatusText(getServiceHealthStatus(service)) }}
                  </NTag>
                </template>
                <NThing>
                  <template #header>
                    {{ service.serviceName }}
                  </template>
                  <template #description>
                    实例: {{ service.healthyInstanceCount }}/{{ service.instanceCount }} | 集群: {{ service.clusterCount }}
                  </template>
                </NThing>
              </NListItem>
            </NList>
          </NCard>
        </NSpace>
      </NGi>
    </NGrid>
  </NCard>
</template>

<style scoped>
</style>
