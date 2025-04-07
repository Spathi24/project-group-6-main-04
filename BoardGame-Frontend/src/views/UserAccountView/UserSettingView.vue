<template>
  <div class="user-account-container">
    <div class="account-card">
      <h1 class="title">Account Settings</h1>
      
      <div class="form-section">
        <h2>User Profile</h2>
        <button class="btn btn-primary" @click="navigateToUpdateUserAccount">
          <Settings class="icon" />
          Update Profile
        </button>
      </div>

      <div class="form-section">
        <h2>Session</h2>
        <button class="btn btn-secondary" @click="logout">
          <LogOut class="icon" />
          Sign Out
        </button>
      </div>

      <div class="form-section">
        <h2>Account Management</h2>
        <button class="btn btn-danger" @click="deleteUserAccount">
          <UserX class="icon" />
          Delete Account
        </button>
      </div>

      <div class="actions">
        <button class="btn btn-primary" @click="navigateToHomePage">
          <Home class="icon" />
          Return to Home
        </button>
      </div>
    </div>
  </div>
</template>

<style src="./UserAccountStyles.css"></style>

<script>
import axios from "axios";
import Settings from "@/components/icons/Settings.vue";
import LogOut from "@/components/icons/LogOut.vue";
import UserX from "@/components/icons/UserX.vue";
import Home from "@/components/icons/HomeIcon.vue";
import AuthService from "@/services/AuthService"; // Import the AuthService

const axiosClient = axios.create({
  baseURL: "http://localhost:8080"
});

export default {
  components: {
    Settings,
    LogOut,
    UserX,
    Home
  },
  methods: {
    async navigateToUpdateUserAccount() {
      this.$router.push("/update");
    },
    async deleteUserAccount(){
      try{
        const storedUserAccountID = AuthService.getUserID(); // Use AuthService
        AuthService.removeUserID(); // Use AuthService
        await axiosClient.delete("/UserAccount/"+storedUserAccountID);
        alert("Your account has been successfully deleted");
        this.$router.push("/login");
      }
      catch(e){
        alert(e.response.data.errors);
      }
    },
    async logout(){
      try{
        AuthService.removeUserID(); // Use AuthService
        alert("You have successfully logged out")
        this.$router.push("/login");
      }
      catch (e){
        alert(e.response.data.errors);
      }
    },
    async navigateToHomePage(){
      this.$router.push("/");
    }
  }
}
</script>