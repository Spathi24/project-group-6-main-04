<template>
    <div :class="['cell', type, type2]">
      <input v-if="type === 'checkbox'" type="checkbox">
      <slot></slot>
      {{ text }}
    </div>
  </template>
  
  <script>
  export default {
    name: 'Cell',
    props: {
      type: String,
      type2: String,
      text: String
    }
  }
  </script>