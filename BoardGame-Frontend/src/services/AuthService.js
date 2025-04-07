/**
 * AuthService: Handles authentication-related operations
 * Using sessionStorage instead of localStorage ensures auth data is cleared when browser is closed
 */
class AuthService {
  // Store user ID in session
  setUserID(userAccountID) {
    sessionStorage.setItem('userAccountID', userAccountID);
  }

  // Get stored user ID
  getUserID() {
    return sessionStorage.getItem('userAccountID');
  }

  // Remove user ID (logout)
  removeUserID() {
    sessionStorage.removeItem('userAccountID');
  }

  // Check if user is logged in
  isLoggedIn() {
    return !!this.getUserID();
  }
}

export default new AuthService();
