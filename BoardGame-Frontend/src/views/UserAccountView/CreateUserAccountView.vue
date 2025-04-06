<template>
  <main>
    <h1>User Account Registration</h1>
    <h4>*password must be at least eight characters long</h4>
    <div>
      <select v-model="newAccountType">
        <option value="PLAYER">player</option>
        <option value="GAMEOWNER">game owner</option>
      </select>
      <input type="text" placeholder="Name" v-model="newName"/>
      <input type="text" placeholder="Password" v-model="newPassword"/>
      <input type="text" placeholder="Email" v-model="newEmail"/>
      <button id="create-btn" @click="createUserAccount" v-bind:disabled="!isUserAccountValid()">Create User Account</button>
      <button class="danger-btn" @click="clearInputs">Clear</button>
    </div>
    <h2>Go Back to Login</h2>
    <div>
      <button id="navigate-login-btn" @click="navigateToLogin">Go to Login</button>
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

const axiosClient = axios.create({
  baseURL: "http://localhost:8080"
});

export default {
  // ...
  data() {
    return {
      newName: null,
      newPassword: null,
      newEmail: null,
      newAccountType: "PLAYER"
    };
  },

  methods: {
    async createUserAccount() {
      const newUserAccount = {
        name: this.newName,
        password: this.newPassword,
        email: this.newEmail,
        accountType: this.newAccountType
      };

      try {
        const response = await axiosClient.post("/UserAccount/userAccountID", newUserAccount);
        alert("The user account is successfully created");
        alert("*** Important!!!\nThe user account has ID " + response.data + "\nPlease remember the ID for login ***")
      } catch (error) {
        alert(error.response.data.errors + "\nThe user account cannot be created")
      }
      this.clearInputs();
    },
    clearInputs() {
      this.newAccountType = "PLAYER";
      this.newName = null;
      this.newPassword = null;
      this.newEmail = null;
    },
    isUserAccountValid() {
      return this.newName
          && this.newPassword
          && this.newEmail
    },
    async navigateToLogin(){
      this.$router.push("/login");
    }
  }
}
</script>