import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

class UserAccountService {
  getUser(id) {
    return axios.get(`${API_BASE_URL}/UserAccount/${id}`);
  }
}

export default new UserAccountService();
