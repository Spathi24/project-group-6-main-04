<template>
  <div class="game-overlay" @click.self="closePopup">
    <div class="game-popup">
      <h1>Add New Game</h1>
      
      <div v-if="loading" class="loading-spinner">
        <div class="spinner"></div>
        <p>Loading game catalog...</p>
      </div>

      <div v-else-if="error" class="error-message">
        <p>{{ error }}</p>
        <div class="retry-button-container">
          <button @click="fetchGameCatalog" class="btn-retry">Try Again</button>
        </div>
      </div>
      
      <form v-else @submit.prevent="saveGame" class="form-container">
        <div class="form-group">
          <label for="game-name">Game Name</label>
          <select id="game-name" v-model="selectedGameId" required>
            <option value="" disabled>Select a game</option>
            <option v-for="game in gameOptions" :key="game.title" :value="game.title">
              {{ game.title }}
            </option>
          </select>
        </div>
        
        <div class="form-group">
          <label for="description">Physical Description</label>
          <textarea 
            id="description" 
            v-model="game.description" 
            placeholder="Describe the condition of your game" 
            required
          ></textarea>
        </div>
        
        <div class="button-container">
          <button type="submit" class="btn btn-primary" :disabled="submitting">
            <span v-if="submitting">Adding...</span>
            <span v-else>Add Game</span>
          </button>
          <button type="button" class="btn btn-secondary" @click="closePopup" :disabled="submitting">Cancel</button>
        </div>
      </form>

      <!-- Success Message -->
      <div v-if="showSuccess" class="success-message">
        <p>Game added successfully!</p>
        <p>Redirecting to your game list...</p>
      </div>
    </div>
    <HomeButton :showText="false" />
  </div>
</template>

<script>
import { ref, onMounted, computed } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import HomeButton from '@/components/HomeButton.vue';
import GameService from '@/services/GameCopyService';

export default {
  name: 'AddGameView',
  components: {
    HomeButton
  },
  props: {
    userId: {
      type: [Number, String],
      required: true
    }
  },
  setup(props) {
    const route = useRoute();
    const router = useRouter();
    const game = ref({ 
      name: '', 
      description: '',
      status: 'Available' // Default status is Available
    });
    
    // Get userId from props or route params
    const userId = computed(() => props.userId || route.params.userId);
    const gameOptions = ref([]);
    const selectedGameId = ref('');
    const loading = ref(true);
    const error = ref(null);
    const submitting = ref(false);
    const showSuccess = ref(false);

    // Fetch game catalog from backend for dropdown options
    const fetchGameCatalog = async () => {
      loading.value = true;
      error.value = null;
      
      try {
        const catalog = await GameService.getGameCatalog();
        gameOptions.value = catalog;
      } catch (err) {
        error.value = err.message || "Failed to load game catalog. Please try again.";
        console.error("Error fetching game catalog:", err);
      } finally {
        loading.value = false;
      }
    };

    onMounted(fetchGameCatalog);

    const saveGame = async () => {
      submitting.value = true;

      try {
        // Find the selected game from the catalog
        const selectedGame = gameOptions.value.find(g => g.title === selectedGameId.value);

        if (!selectedGame) {
          throw new Error('Please select a valid game');
        }

        // Create the game copy with the correct structure
        const gameCopyData = {
          title: selectedGame.title, // Use title instead of ID
          description: game.value.description
        };

        await GameService.addGame(userId.value, gameCopyData);

        // Show success message and redirect after delay
        showSuccess.value = true;
        setTimeout(() => {
          router.push({ name: 'game-copy-view', params: { userId: userId.value } });
        }, 1500);
      } catch (err) {
        error.value = err.message || "Failed to add the game. Please try again.";
        console.error("Error adding game:", err);
        submitting.value = false;
      }
    };

    const closePopup = () => {
      router.push({ name: 'game-copy-view', params: { userId: userId.value } });
    };

    return { 
      userId,
      game,
      gameOptions,
      selectedGameId,
      loading, 
      error, 
      submitting, 
      showSuccess,
      fetchGameCatalog,
      saveGame, 
      closePopup 
    };
  }
};
</script>

<style src="./GameCopyViewStyles.css"></style>

<style scoped>
.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 2rem;
}

.spinner {
  border: 4px solid rgba(255, 255, 255, 0.3);
  border-radius: 50%;
  border-top: 4px solid #ff6f61;
  width: 30px;
  height: 30px;
  animation: spin 1s linear infinite;
  margin-bottom: 1rem;
}

@keyframes spin {
  0% { transform: rotate(0deg); }
  100% { transform: rotate(360deg); }
}

.error-message {
  color: #ff6f61;
  padding: 1rem 0;
  text-align: center;
  font-family: 'Inter', sans-serif;
}

.retry-button-container {
  display: flex;
  justify-content: center;
  margin-top: 1rem;
}

.btn-retry {
  background-color: #ff6f61;
  color: white;
  border: none;
  padding: 0.5rem 1rem;
  border-radius: 4px;
  cursor: pointer;
  font-family: 'Inter', sans-serif;
  font-weight: 500;
  transition: background-color 0.3s, transform 0.2s;
}

.btn-retry:hover {
  background-color: #e65a50;
  transform: translateY(-2px);
}

.success-message {
  color: #4CAF50;
  text-align: center;
  padding: 2rem;
}
</style>
