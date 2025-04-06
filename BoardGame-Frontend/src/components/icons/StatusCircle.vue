<template>
    <span class="status-circle" :class="statusClass"></span>
  </template>
  
  <script>
  export default {
    name: 'StatusCircle',
    props: {
      status: {
        type: String,
        required: true
      }
    },
    computed: {
      statusClass() {
        return {
          'available': this.status === 'Available',
          'borrowed': this.status === 'Borrowed',
          'damaged': this.status === 'Damaged',
          'lost': this.status === 'Lost'
        }
      }
    }
  }
  </script>
  
  <style scoped>
  .status-circle {
    display: inline-block;
    width: 10px;
    height: 10px;
    border-radius: 50%;
    margin-right: 8px;
  }
  
  .available {
    background-color: #4CAF50;
  }
  
  .borrowed {
    background-color: #FFC107;
  }
  
  .damaged {
    background-color: #FF5252;
  }
  
  .lost {
    background-color: #212121;
    border: 2px solid #000;
  }
  </style>