<template>
  <div class="user-account-container">
    <div class="account-card">
      <h1 class="title">Sign In</h1>
      
      <div class="form-group">
        <input type="text" placeholder="User ID" v-model="userAccountID"/>
      </div>
      
      <div class="form-group">
        <input type="password" placeholder="Password" v-model="password"/>
      </div>
      
      <div class="actions">
        <button class="btn btn-primary" @click="login" :disabled="!isInputValid()">
          <LogIn class="icon" />
          Sign In
        </button>
        <button class="btn btn-secondary" @click="clearInputs">
          <RefreshCw class="icon" />
          Clear
        </button>
      </div>
      
      <div class="form-section">
        <h2>New to our platform?</h2>
        <button class="btn btn-success" @click="navigateToCreateAccount">
          <UserPlus class="icon" />
          Create Account
        </button>
      </div>
    </div>
  </div>
</template>

<style src="./UserAccountStyles.css"></style>

<script>
import axios from "axios";
import {ref} from "vue";
import LogIn from "@/components/icons/LogIn.vue";
import RefreshCw from "@/components/icons/RefreshCw.vue";
import UserPlus from "@/components/icons/UserPlus.vue";
import AuthService from "@/services/AuthService"; // Import the AuthService

const axiosClient = axios.create({
  baseURL: "http://localhost:8080"
});

export default {
  name: 'Login',
  components: {
    LogIn,
    RefreshCw,
    UserPlus
  },
  data() {
    return {
      userAccountID: null,
      password: null
    };
  },
  methods: {
    async login() {
      const newUserAccount = {
        userAccountID: this.userAccountID,
        password: this.password
      };
      try {
        const response = await axiosClient.post("/UserAccount/login/"+newUserAccount.userAccountID+"/"+newUserAccount.password);
        AuthService.setUserID(newUserAccount.userAccountID); // Use AuthService
        alert("You have successfully logged in!")
        this.$router.push("/");
      } catch (error) {
        alert("The ID or password you provided is incorrect. Please try again.");
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