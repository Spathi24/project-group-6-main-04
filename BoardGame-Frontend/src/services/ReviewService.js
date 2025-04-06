import axios from "axios";

const API_URL = "http://localhost:8080/api";

const api = axios.create({
  baseURL: API_URL,
  headers: {
    "Content-Type": "application/json",
    "Cache-Control": "no-cache, no-store, must-revalidate",
    Pragma: "no-cache",
    Expires: "0",
  },
});

const extractErrorMessage = (error) => {
  if (error.response) {
    const data = error.response.data;
    if (data.message) return data.message;
    if (data.errors && Array.isArray(data.errors))
      return data.errors.join(", ");
    if (typeof data === "string") return data;
    if (typeof data === "object") return JSON.stringify(data);
  }
  return error.message || "An unexpected error occurred";
};

class ReviewService {
  getUserReviews = async (userId) => {
    try {
      const response = await api.get(`/reviews/user/${userId}`);
      return response.data;
    } catch (error) {
      console.error(`Error fetching reviews for user ${userId}:`, error);
      throw new Error(extractErrorMessage(error));
    }
  };

  getGameReviews = async (gameTitle) => {
    try {
      const response = await api.get(
        `/reviews/game/${encodeURIComponent(gameTitle)}`
      );
      return response.data;
    } catch (error) {
      console.error(`Error fetching reviews for game '${gameTitle}':`, error);
      throw new Error(extractErrorMessage(error));
    }
  };

  getReview = async (userId, gameTitle) => {
    try {
      const response = await api.get(
        `/reviews/${userId}/${encodeURIComponent(gameTitle)}`
      );
      return response.data;
    } catch (error) {
      console.error(
        `Error fetching review for user ${userId} and game '${gameTitle}':`,
        error
      );
      throw new Error(extractErrorMessage(error));
    }
  };

  addReview = async (userId, reviewData) => {
    try {
      const reviewCreationDto = {
        rating: reviewData.rating,
        comment: reviewData.comment,
        reviewKey: {
          gameTitle: reviewData.gameTitle,
          reviewerId: userId,
        },
      };
      const response = await api.post(`/reviews/${userId}`, reviewCreationDto);
      return response.data;
    } catch (error) {
      console.error(`Error creating review for user ${userId}:`, error);
      throw new Error(extractErrorMessage(error));
    }
  };

  deleteReview = async (userId, gameTitle) => {
    try {
      await api.delete(`/reviews/${userId}/${encodeURIComponent(gameTitle)}`);
      return true;
    } catch (error) {
      console.error(
        `Error deleting review for user ${userId} and game '${gameTitle}':`,
        error
      );
      throw new Error(extractErrorMessage(error));
    }
  };
}

export default new ReviewService();
