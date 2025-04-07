<template>
  <div class="user-account-container">
    <div class="account-card">
      <h1 class="title">Update Account</h1>
      
      <!-- Navigation buttons moved to the top -->
      <div class="navigation-actions">
        <button class="btn btn-secondary" @click="navigateToSetting">
          <ArrowLeft class="icon" />
          Back to Settings
        </button>
        
        <button class="btn btn-primary" @click="navigateToHome">
          <Home class="icon" />
          Return to Home
        </button>
      </div>
      
      <div class="form-section">
        <h2>Update All Details</h2>
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
          <input type="password" placeholder="Password" v-model="newPassword"/>
        </div>
        
        <div class="form-group">
          <input type="email" placeholder="Email" v-model="newEmail"/>
        </div>
        
        <div class="actions">
          <button class="btn btn-primary" @click="updateUserAccountAll" :disabled="!isUserAccountValid()">
            <Save class="icon" />
            Update Account
          </button>
          <button class="btn btn-secondary" @click="clearInputsAll">
            <RefreshCw class="icon" />
            Clear
          </button>
        </div>
      </div>

      <div class="form-section">
        <h2>Update Password Only</h2>
        <p class="warning">*Password must be at least eight characters long</p>
        
        <div class="form-group">
          <input type="password" placeholder="New Password" v-model="newOnlyPassword"/>
        </div>
        
        <div class="actions">
          <button class="btn btn-primary" @click="updatePassword" :disabled="!isPasswordValid()">
            <Lock class="icon" />
            Update Password
          </button>
          <button class="btn btn-secondary" @click="clearPassword">
            <RefreshCw class="icon" />
            Clear
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<style src="./UserAccountStyles.css"></style>

<script>
import axios from "axios";
import Save from "@/components/icons/Save.vue";
import Lock from "@/components/icons/Lock.vue";
import RefreshCw from "@/components/icons/RefreshCw.vue";
import ArrowLeft from "@/components/icons/ArrowLeft.vue";
import Home from "@/components/icons/HomeIcon.vue";
import AuthService from "@/services/AuthService"; // Import the AuthService

const axiosClient = axios.create({
  baseURL: "http://localhost:8080"
});

export default {
  components: {
    Save,
    Lock,
    RefreshCw,
    ArrowLeft,
    Home
  },
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
  methods: {
    async updateUserAccountAll() {
      const updateUserAccount = {
        name: this.newName,
        password: this.newPassword,
        email: this.newEmail,
        accountType: this.newAccountType,
      };
      try {
        const storedUserAccountID = AuthService.getUserID(); // Use AuthService
        const response = await axiosClient.put("/UserAccount/"+storedUserAccountID, updateUserAccount);
        alert("Your account has been successfully updated");
      } catch (error) {
        alert(error.response.data.errors + "\nFailed to update your account");
      }
      this.userAccounts.push(updateUserAccount);
      this.clearInputsAll();
    },
    clearInputsAll() {
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

    async updatePassword(){
      const newPassword = this.newOnlyPassword;
      try{
        const storedUserAccountID = AuthService.getUserID(); // Use AuthService
        const response = await axiosClient.get("/UserAccount/"+storedUserAccountID);
        const u = response.data;
        const updateUserAccount = {
          name: u.name,
          password: newPassword,
          email: u.email,
          accountType: u.accountType,
        };
        const response2 = await axiosClient.put("/UserAccount/"+storedUserAccountID, updateUserAccount);
        alert("Your password has been successfully updated");
      }
      catch(e){
        alert(e.response.data.errors + "\nFailed to update your password");
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
    },
    async navigateToHome(){
      this.$router.push("/");
    }
  }
}
</script>

<style scoped>
/* Add styling for the new navigation container */
.navigation-actions {
  display: flex;
  justify-content: space-between;
  margin-bottom: 2rem;
  border-bottom: 1px solid rgba(255, 255, 255, 0.1);
  padding-bottom: 1rem;
}

/* Make buttons in the navigation section more compact */
.navigation-actions .btn {
  padding: 0.5rem 1rem;
  font-size: 0.9rem;
}
</style>