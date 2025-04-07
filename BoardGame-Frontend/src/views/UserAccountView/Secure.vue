<template>
  <div class="user-account-container">
    <div class="account-card">
      <h1 class="title">Authentication Required</h1>
      <p class="subtitle">You need to sign in to access this page</p>
      
      <div class="warning">
        <AlertTriangle class="icon" />
        <span>You must be logged in to use this feature</span>
      </div>
      
      <div class="actions">
        <button class="btn btn-primary" @click="navigateToLogin">
          <LogIn class="icon" />
          Go to Login
        </button>
      </div>
    </div>
  </div>
</template>

<style src="./UserAccountStyles.css"></style>

<script>
import AlertTriangle from "@/components/icons/AlertTriangle.vue";
import LogIn from "@/components/icons/LogIn.vue";

export default{
  name: 'secure',
  components: {
    AlertTriangle,
    LogIn
  },
  methods: {
    async navigateToLogin(){
      this.$router.push("/login");
    }
  }
}
</script>

<style scoped>
.warning {
  display: flex;
  align-items: center;
  background-color: rgba(244, 67, 54, 0.1);
  padding: 1rem;
  border-radius: 8px;
  margin: 2rem 0;
  gap: 0.5rem;
}

.warning .icon {
  color: #f44336;
}
</style>