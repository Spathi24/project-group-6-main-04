import axios from 'axios';
const API_URL = 'http://localhost:8080/api';

// Create axios instance with common configuration
const api = axios.create({
  baseURL: API_URL,
  headers: {
    'Content-Type': 'application/json',
    'Cache-Control': 'no-cache, no-store, must-revalidate',
    'Pragma': 'no-cache',
    'Expires': '0'
  }
});

// Helper function to extract error message from different response formats
const extractErrorMessage = (error) => {
  if (error.response) {
    const data = error.response.data;

    if (data.message) {
      return data.message;
    } else if (data.errors && Array.isArray(data.errors)) {
      return data.errors.join(', ');
    } else if (typeof data === 'string') {
      return data;
    } else if (typeof data === 'object') {
      return JSON.stringify(data);
    }
  }

  return error.message || 'An unexpected error occurred';
};

class GameCopyService {
  getAllGameCopies() {
    return axios.get(`${API_URL}/gamecopies`);
  }

  getUserGames = async (userId) => {
    try {
      const response = await api.get(`/gamecopies/${userId}`);
      console.log('User games:', response.data);
      return response.data;
    } catch (error) {
      console.error('Error fetching user games:', error);
      throw new Error(extractErrorMessage(error));
    }
  }

  getGameById = async (userId, title, noCache = false) => {
    try {
      const cacheParam = noCache ? `?_=${Date.now()}` : '';
      const response = await api.get(`/gamecopies/${userId}${cacheParam}`);
      const game = response.data.find(g => g.title == title);

      if (!game) {
        throw new Error(`Game with title ${title} not found`);
      }

      return game;
    } catch (error) {
      console.error(`Error fetching game with title ${title}:`, error);
      throw new Error(extractErrorMessage(error));
    }
  }

  addGame = async (userId, gameData) => {
    try {
      const gameCopyCreationDto = {
        title: gameData.title,
        description: gameData.description,
        owner: userId
      };
      console.log('Adding new game:', gameCopyCreationDto);
      const response = await api.post(`/gamecopies/${userId}`, gameCopyCreationDto);
      return response.data;
    } catch (error) {
      console.error('Error adding new game:', error);
      throw new Error(extractErrorMessage(error));
    }
  }

  updateGameStatus = async (userId, gameTitle, status) => {
    try {
      await api.put(`/gamecopies/${userId}/status/${gameTitle}`, null, {
        params: { status }
      });
      return true;
    } catch (error) {
      console.error(`Error updating status for game '${gameTitle}':`, error);
      throw new Error(extractErrorMessage(error));
    }
  }

  updateGameDescription = async (userId, gameTitle, newDescription) => {
    try {
      await api.put(`/gamecopies/${userId}/description/${gameTitle}`, null, {
        params: { newDescription }
      });
      return true;
    } catch (error) {
      console.error(`Error updating description for game '${gameTitle}':`, error);
      throw new Error(extractErrorMessage(error));
    }
  }

  updateGame = async (userId, title, gameData) => {
    try {
      const currentGame = await this.getGameById(userId, title);

      if (!currentGame) {
        throw new Error(`Game with title ${title} not found`);
      }

      const gameTitle = currentGame.title;

      if (gameData.description !== undefined && gameData.description !== currentGame.description) {
        await this.updateGameDescription(userId, gameTitle, gameData.description);
      }

      if (gameData.status !== undefined && gameData.status !== currentGame.status) {
        await this.updateGameStatus(userId, gameTitle, gameData.status);
      }

      return true;
    } catch (error) {
      console.error(`Error updating game with title ${title}:`, error);
      throw new Error(extractErrorMessage(error));
    }
  }

  deleteGame = async (userId, title) => {
    try {
      const currentGame = await this.getGameById(userId, title);

      if (!currentGame) {
        throw new Error(`Game with title ${title} not found`);
      }

      const gameTitle = currentGame.title;

      await api.delete(`/gamecopies/${userId}/${gameTitle}`);
      return true;
    } catch (error) {
      console.error(`Error deleting game with title ${title}:`, error);
      throw new Error(extractErrorMessage(error));
    }
  }

  deleteManyGames = async (userId, gameIds) => {
    try {
      const deletePromises = gameIds.map(id => this.deleteGame(userId, id));
      await Promise.all(deletePromises);
      return true;
    } catch (error) {
      console.error('Error deleting multiple games:', error);
      throw new Error(extractErrorMessage(error));
    }
  }

  getGameCatalog = async () => {
    try {
      const response = await api.get('/games');
      console.log('Game catalog:', response.data);
      return response.data;
    } catch (error) {
      console.error('Error fetching game catalog:', error);
      throw new Error(extractErrorMessage(error));
    }
  }

  getGameByTitle = async (title) => {
    try {
      const response = await api.get(`/games/${title}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching game with title ${title}:`, error);
      throw new Error(extractErrorMessage(error));
    }
  }
}

export default new GameCopyService();
