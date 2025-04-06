<template>
  <main>
    <h1>User Account Update</h1>
    <h2>Update All the Attributes</h2>
    <h4>*password must be at least eight characters long</h4>
    <div>
      <select v-model="newAccountType">
        <option value="PLAYER">player</option>
        <option value="GAMEOWNER">game owner</option>
      </select>
      <input type="text" placeholder="Name" v-model="newName"/>
      <input type="text" placeholder="Password" v-model="newPassword"/>
      <input type="text" placeholder="Email" v-model="newEmail"/>
      <button id="update-btn-all" @click="updateUserAccountAll" v-bind:disabled="!isUserAccountValid()">Update User Account</button>
      <button class="danger-btn-1" @click="clearInputsAll">Clear</button>
    </div>
    <h2>Update Only the Password</h2>
    <h4>*password must be at least eight characters long</h4>
    <div>
      <input type="text" placeholder="Password" v-model = "newOnlyPassword"/>
      <button id="update-btn-password" @click="updatePassword" v-bind:disabled="!isPasswordValid()">Update User Account</button>
      <button class="danger-btn-2" @click="clearPassword">Clear</button>
    </div>
    <h2>Go Back To Setting</h2>
    <div>
      <button id="setting-btn" @click="navigateToSetting">navigateToSetting</button>
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
      userAccounts: [],
      newName: null,
      newPassword: null,
      newOnlyPassword: null,
      newEmail: null,
      newAccountType: "PLAYER"
    };
  },
  // ...

  methods: {
    async updateUserAccountAll() {
      const updateUserAccount = {
        name: this.newName,
        password: this.newPassword,
        email: this.newEmail,
        accountType: this.newAccountType,
      };
      try {
        // id need to be changed
        const storedUserAccountID = localStorage.getItem("userAccountID");
        const response = await axiosClient.put("/UserAccount/"+storedUserAccountID, updateUserAccount);
        alert("You updated your account successfully");
      } catch (error) {
        alert(error.response.data.errors + "\nYou failed to update your account");
      }
      // need to delete the old one
      this.userAccounts.push(updateUserAccount);
      this.clearInputsAll();
    },
    clearInputsAll() {
      this.newAccountType = "Player";
      this.newName = null;
      this.newPassword = null;
      this.newEmail = null;
    },
    isUserAccountValid() {
      return this.newName
          && this.newPassword
          && this.newEmail
    },

    async updatePassword(){
      const newPassword = this.newOnlyPassword;
      try{
        const storedUserAccountID = localStorage.getItem("userAccountID");
        const response = await axiosClient.get("/UserAccount/"+storedUserAccountID);
        const u = response.data;
        const updateUserAccount = {
          name: u.name,
          password: newPassword,
          email: u.email,
          accountType: u.accountType,
        };
        const response2 = await axiosClient.put("/UserAccount/"+storedUserAccountID, updateUserAccount);
        alert("You updated your account successfully");
      }
      catch(e){
        alert(e.response.data.errors + "\nYou failed to update your account");
      }
      this.clearPassword();
    },
    clearPassword(){
      this.newOnlyPassword = null;
    },
    isPasswordValid(){
      return this.newOnlyPassword;
    },
    async navigateToSetting(){
      this.$router.push("/userSetting");
    }

  }
}
</script>