<template>
  <div class="home-container"> <div class="main-frame"> <div class="card recommended-games"> <h2>Game Catalog</h2>

        <div class="action-bar">
          <button class="menu-item" @click="navigateToAddGame"> Add New Game
          </button>
        </div>

        <div v-if="loading" class="loading">Loading games...</div>
        <div v-else-if="error" class="error">{{ error }}</div>
        <div v-else-if="games.length === 0" class="no-games">No games available.</div>

        <div v-else class="games-grid"> <div v-for="game in games" :key="game.id" class="game-card"> <img :src="game.imageUrl || require('@/assets/game-placeholder.jpg')" alt="Game image" class="game-image"> <div class="game-info"> <h4 class="game-title">{{ game.name }}</h4> <p><strong>Category:</strong> {{ game.category }}</p>
              <p><strong>Player Count:</strong> {{ game.minPlayers }}-{{ game.maxPlayers }}</p>
              <p><strong>Duration:</strong> {{ game.gameDuration }} min</p>
            </div>
            <div class="game-actions">
              <button @click="createBorrowRequest(game.id)" class="menu-item">Borrow</button> <button @click="viewReviews(game.id)" class="menu-item">Reviews</button> </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import GameCopyService from '@/services/GameCopyService';

export default {
  name: 'GameCatalogView',
  data() {
    return {
      loading: false,
      error: null,
      games: [],
    };
  },
  mounted() {
    this.fetchGames();
  },
  methods: {
    async fetchGames() {
      this.loading = true;
      this.error = null;
      try {
        const response = await GameCopyService.getGameCatalog();
        this.games = response;
      } catch (err) {
        this.error = err.message || 'Failed to load games.'; 
        console.error(err);
      } finally {
        this.loading = false;
      }
    },
    navigateToAddGame() {
      const userId = localStorage.getItem('userAccountID');
      if (userId) {
        this.$router.push(`/add-game/${userId}`); 
      } else {
        console.error('User ID not found in local storage.');
      }
    },
    createBorrowRequest(gameId) {
      this.$router.push(`/create-borrow-request?gameId=${gameId}`);
    },
    viewReviews(gameId) {
      this.$router.push(`/reviews?gameId=${gameId}`);
    },
  },
};
</script>

<style scoped>
@import '../HomePageView/HomePageView.css';

.action-bar {
  margin-bottom: 1rem;
}

.loading, .error, .no-games {
  padding: 1rem;
  text-align: center;
  color: #ffffff80;
}

.games-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(400px, 1fr)); /* Adjust minmax as needed */
  gap: 1rem;
  margin-top: 1rem;
}

.game-card {
  display: flex;
  flex-direction: row;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  overflow: hidden;
  padding: 1rem;
  gap: 1rem;
  transition: transform 0.2s, box-shadow 0.2s;
}

.game-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 6px 10px rgba(0, 0, 0, 0.2);
}

.game-image {
  width: 30%; /* Adjust as needed */
  height: auto;
  object-fit: cover;
  border-radius: 8px;
}

.game-info {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  flex: 1;
  gap: 0.5rem;
}

.game-title {
  font-size: 1.5rem;
  font-weight: bold;
  color: #ffffff;
  margin-bottom: 0.5rem;
}

.game-actions {
  display: flex;
  flex-direction: column;
  gap: 0.5rem;
}
</style>
