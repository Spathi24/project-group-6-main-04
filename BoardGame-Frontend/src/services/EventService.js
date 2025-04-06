import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

class EventService {
  getAllEvents() {
    return axios.get(`${API_BASE_URL}/events`);
  }
  
  // Additional methods can be added as needed
}

export default new EventService();
