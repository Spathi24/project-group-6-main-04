<template>
  <main>
    <h1>Unauthenticated</h1>
    <h3>You must login to use other functions</h3>
    <h2>Please Login</h2>
    <div>
      <button id="navigate-login-btn" @click="navigateToLogin">Go to Login</button>
    </div>
  </main>>
</template>

<style>
main {
  display: flex;
  flex-direction: column;
  align-items: stretch;
}

h1{
  color: red;
}

h3{
  color: red;
}
</style>

<script>
export default{
  name: 'secure',

  methods: {
    async navigateToLogin(){
      this.$router.push("/login");
    }
  }
}
</script>