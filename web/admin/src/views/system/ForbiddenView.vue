<template>
  <AResult status="403" title="403" sub-title="当前账号没有访问该页面的权限">
    <template #extra>
      <RouterLink to="/dashboard">
        <AButton type="primary">返回仪表盘</AButton>
      </RouterLink>
    </template>
  </AResult>
</template>
