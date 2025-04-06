<script setup>
import { RouterView } from 'vue-router'
</script>

<template>
  <RouterView />
</template>

<style>
html, body {
  margin: 0;
  padding: 0;
  height: 100%;
  width: 100%;
}

#app {
  min-height: 100vh;
  width: 100%;
  display: flex;
  background-color: #1e1e2f;
}

.router-view {
  flex: 1;
  width: 100%;
}
</style>
