import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

class BorrowRequestService {
  getUserRequests(userId) {
    return axios.get(`${API_BASE_URL}/borrowrequests/user/${userId}`);
  }
  
  // Additional methods can be added as needed
}

export default new BorrowRequestService();
