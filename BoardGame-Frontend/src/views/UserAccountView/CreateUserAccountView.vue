<template>
  <div class="user-account-container">
    <div class="account-card">
      <h1 class="title">Create Account</h1>
      <p class="warning">*Password must be at least eight characters long</p>
      
      <div class="form-group">
        <select v-model="newAccountType">
          <option value="PLAYER">Player</option>
          <option value="GAMEOWNER">Game Owner</option>
        </select>
      </div>
      
      <div class="form-group">
        <input type="text" placeholder="Name" v-model="newName"/>
      </div>
      
      <div class="form-group">
        <input type="text" placeholder="Password" v-model="newPassword"/>
      </div>
      
      <div class="form-group">
        <input type="text" placeholder="Email" v-model="newEmail"/>
      </div>
      
      <div class="actions">
        <button class="btn btn-primary" @click="createUserAccount" :disabled="!isUserAccountValid()">
          <UserPlus class="icon" />
          Create Account
        </button>
        <button class="btn btn-secondary" @click="clearInputs">
          <RefreshCw class="icon" />
          Clear
        </button>
      </div>
      
      <div class="form-section">
        <h2>Already have an account?</h2>
        <button class="btn btn-success" @click="navigateToLogin">
          <LogIn class="icon" />
          Go to Login
        </button>
      </div>
    </div>
  </div>
</template>

<style src="./UserAccountStyles.css"></style>

<script>
import axios from "axios";
import UserPlus from "@/components/icons/UserPlus.vue";
import RefreshCw from "@/components/icons/RefreshCw.vue";
import LogIn from "@/components/icons/LogIn.vue";

const axiosClient = axios.create({
  baseURL: "http://localhost:8080"
});

export default {
  components: {
    UserPlus,
    RefreshCw,
    LogIn
  },
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
        alert("Your account has been successfully created");
        alert("*** Important! ***\nYour account ID is: " + response.data + "\nPlease save this ID for login");
        
        // Add redirection to login page after successful creation
        setTimeout(() => {
          this.$router.push("/login");
        }, 1500);
      } catch (error) {
        alert(error.response.data.errors + "\nAccount creation failed");
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
          && this.newEmail;
    },
    async navigateToLogin(){
      this.$router.push("/login");
    }
  }
}
</script>