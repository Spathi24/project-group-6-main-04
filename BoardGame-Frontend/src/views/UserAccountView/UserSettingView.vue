<template>
  <main>
    <h1>Update User Account</h1>
    <div>
      <button id="update-btn" @click="navigateToUpdateUserAccount">Update User Account</button>
    </div>
    <h1>Log Out</h1>
    <div>
      <button id="logout-btn" @click="logout">Logout</button>
    </div>
    <h1>Delete User Account</h1>
    <div>
      <button class="danger-btn" @click="deleteUserAccount">Delete User Account</button>
    </div>
    <h1>Go to Main Page</h1>
    <div>
      <button id="homePage-btn" @click="navigateToHomePage">Go to Home Page</button>
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

  methods: {
    async navigateToUpdateUserAccount() {
      this.$router.push("/update");
    },
    async deleteUserAccount(){
      try{
        const storedUserAccountID = localStorage.getItem("userAccountID");
        localStorage.removeItem("userAccountID")
        await axiosClient.delete("/UserAccount/"+storedUserAccountID);
        alert("You delete your account successfully");
        this.$router.push("/login");
      }
      catch(e){
        alert(e.response.data.errors);
      }
    },
    async logout(){
      try{
        localStorage.removeItem("userAccountID");
        alert("You log out successfully")
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