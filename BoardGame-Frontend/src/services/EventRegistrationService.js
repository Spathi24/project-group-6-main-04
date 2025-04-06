import axios from 'axios';

const API_URL = 'http://localhost:8080/api';

const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
    'Cache-Control': 'no-cache, no-store, must-revalidate',
    'Pragma': 'no-cache',
    'Expires': '0'
  }
});

const extractErrorMessage = (error) => {
  if (error.response) {
    const data = error.response.data;
    if (data.message) return data.message;
    if (data.errors && Array.isArray(data.errors)) return data.errors.join(', ');
    if (typeof data === 'string') return data;
    if (typeof data === 'object') return JSON.stringify(data);
  }
  return error.message || 'An unexpected error occurred';
};

class EventRegistrationService {
  getAllEventRegistrations = async () => {
    try {
      const response = await api.get('/eventregistrations');
      return response.data;
    } catch (error) {
      console.error('Error fetching all event registrations:', error);
      throw new Error(extractErrorMessage(error));
    }
  };

  getAllRegistrationsByUser = async (userId) => {
    try {
      const response = await api.get(`/eventregistrations/user/${userId}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching registrations for user ${userId}:`, error);
      throw new Error(extractErrorMessage(error));
    }
  };

  getAllRegistrationsByEvent = async (eventId) => {
    try {
      const response = await api.get(`/eventregistrations/event/${eventId}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching registrations for event ${eventId}:`, error);
      throw new Error(extractErrorMessage(error));
    }
  };

  getEventRegistration = async (userId, eventId) => {
    try {
      const response = await api.get(`/eventregistrations/${userId}/${eventId}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching registration for user ${userId} and event ${eventId}:`, error);
      throw new Error(extractErrorMessage(error));
    }
  };

  createRegistration = async (registrationRequest) => {
    try {
      const response = await api.post('/eventregistrations', registrationRequest);
      return response.data;
    } catch (error) {
      console.error('Error creating event registration:', error);
      throw new Error(extractErrorMessage(error));
    }
  };

  cancelRegistration = async (userId, eventId) => {
    try {
      await api.delete(`/eventregistrations/${userId}/${eventId}`);
      return true;
    } catch (error) {
      console.error(`Error cancelling registration for user ${userId} and event ${eventId}:`, error);
      throw new Error(extractErrorMessage(error));
    }
  };
}

export default new EventRegistrationService();