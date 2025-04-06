<template>
  <main>
    <h1>Login</h1>
    <div>
      <input type="text" placeholder="ID" v-model="userAccountID"/>
      <input type="text" placeholder="Password" v-model="password"/>
      <button id="login-btn" @click="login" v-bind:disabled="!isInputValid()">Login</button>
      <button class="danger-btn" @click="clearInputs">Clear</button>
    </div>
    <h2>Don't have Account?</h2>
    <div>
      <button id="createAccount-btn" @click="navigateToCreateAccount">Create Account</button>
    </div>
  </main>
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

h2 {
  padding-top: 1em;
  text-decoration: underline;
}

.danger-btn {
  border: 1px solid red;
  color: red;
}
</style>

<script>

import axios from "axios";
import {ref} from "vue";

const axiosClient = axios.create({
  baseURL: "http://localhost:8080"
});


export default {
  // ...

  name: 'Login',
  data() {
    return {
      userAccountID: null,
      password: null
    };
  },
  // ...

  methods: {
    async login() {
      const newUserAccount = {
        userAccountID: this.userAccountID,
        password: this.password
      };
      try {
        const response = await axiosClient.post("/UserAccount/login/"+newUserAccount.userAccountID+"/"+newUserAccount.password);
        localStorage.setItem("userAccountID", newUserAccount.userAccountID);
        alert("You logged in successfully!")
        this.$router.push("/");
      } catch (error) {
        alert("You provided the wrong ID or password" + "\nPlease try again");
      }
      this.clearInputs();
    },
    clearInputs() {
      this.userAccountID = null;
      this.password = null;
    },
    isInputValid() {
      return this.userAccountID &&
          this.password;
    },
    async navigateToCreateAccount(){
      this.$router.push("/create");
    }
  }
}
</script>