<template>
  <AResult status="404" title="404" sub-title="页面不存在或已经移动">
    <template #extra>
      <RouterLink to="/dashboard">
        <AButton type="primary">返回仪表盘</AButton>
      </RouterLink>
    </template>
  </AResult>
</template>
