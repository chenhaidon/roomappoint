<template>
  <div id="app">
    <router-view />
  </div>
</template>

<script>

export default {
  name: 'App',

}
</script>

<style>
@import './css/index.css';
@import "./css/font/iconfont.css";

html,
body {
  background-color: var(--lib-bg-page);
}

.bg-header {
  background-color: var(--lib-accent);
  display: flex;
  height: 60px;
  align-items: center;
  justify-content: space-between;
  padding: 0 var(--lib-space-md);
  box-shadow: var(--lib-shadow-sm);
  color: #fff;
}

.pointer {
  cursor: pointer;
}

.card {
  box-shadow: var(--lib-shadow-sm);
  border: 1px solid var(--lib-border);
  min-height: 100%;
  border-radius: var(--lib-radius-lg);
  padding: var(--lib-space-md);
  background: var(--lib-bg-surface);
}

.margin-top-xs {
  margin-top: var(--lib-space-xs);
}

.margin-top-sm {
  margin-top: var(--lib-space-sm);
}

.margin-top-lg {
  margin-top: var(--lib-space-lg);
}

.margin-bottom-xs {
  margin-bottom: var(--lib-space-xs);
}

.margin-bottom-lg {
  margin-bottom: var(--lib-space-lg);
}

.el-button--primary {
  background-color: var(--lib-accent);
  border-color: var(--lib-accent);
}

.el-button--primary:hover,
.el-button--primary:focus {
  background-color: #3A5B78;
  border-color: #3A5B78;
}

.el-input__inner:focus,
.el-textarea__inner:focus {
  border-color: var(--lib-accent);
}

.el-menu-item.is-active,
.el-submenu__title:hover,
.el-menu-item:hover {
  color: var(--lib-accent) !important;
}

.el-card {
  border-color: var(--lib-border);
  box-shadow: var(--lib-shadow-sm);
  border-radius: var(--lib-radius-lg);
}

.el-pagination.is-background .el-pager li:not(.disabled).active {
  background-color: var(--lib-accent);
}
</style>
