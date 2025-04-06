<template>
  <div class="game-overlay" @click.self="closePopup">
    <div class="game-popup">
      <h1>Edit Game</h1>
      
      <div v-if="loading" class="loading-spinner">
        <div class="spinner"></div>
        <p>Loading game details...</p>
      </div>

      <div v-else-if="error" class="error-message">
        <p>{{ error }}</p>
        <div class="retry-button-container">
          <button @click="fetchGameDetails" class="btn-retry">Try Again</button>
        </div>
      </div>
      
      <template v-else>
        <div class="status-indicator">
          <div class="status-dot" :class="getStatusClass"></div>
          <span class="status-text">Current Status: {{ game.status }}</span>
        </div>
        
        <form @submit.prevent="saveChanges" class="form-container">
          <div class="form-group">
            <label for="game-name">Game Name</label>
            <input id="game-name" type="text" v-model="game.title" disabled />
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
          
          <div class="form-group" v-if="game.status !== 'Borrowed'">
            <label for="status">Status</label>
            <select id="status" v-model="game.status" required>
              <option value="Available">Available</option>
              <option value="Damaged">Damaged</option>
              <option value="Lost">Lost</option>
            </select>
          </div>
          
          <div class="button-container">
            <button type="submit" class="btn btn-success" :disabled="submitting">
              <span v-if="submitting">Saving...</span>
              <span v-else>Save Changes</span>
            </button>
            <button type="button" class="btn btn-danger" @click="closePopup" :disabled="submitting">Cancel</button>
          </div>
        </form>

        <!-- Success Message -->
        <div v-if="showSuccess" class="success-message">
          <p>Game updated successfully!</p>
          <p>Redirecting to your game list...</p>
        </div>
      </template>
    </div>
    <HomeButton :showText="false" />
  </div>
</template>

<script>
import { ref, onMounted, computed, watch } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import HomeButton from '@/components/HomeButton.vue';
import GameService from '@/services/GameCopyService';

export default {
  name: 'EditGameView',
  components: {
    HomeButton
  },
  props: {
    userId: {
      type: [Number, String],
      required: true
    },
    title: {
      type: String,
      required: true
    }
  },
  setup(props) {
    const route = useRoute();
    const router = useRouter();
    const game = ref({ name: '', description: '', status: '' });
    const loading = ref(true);
    const error = ref(null);
    const submitting = ref(false);
    const showSuccess = ref(false);

    const userId = computed(() => props.userId || route.params.userId);
    const gameTitle = computed(() => props.title || route.params.title);

    const getStatusClass = computed(() => {
      return {
        'status-available': game.value.status === 'Available',
        'status-borrowed': game.value.status === 'Borrowed',
        'status-damaged': game.value.status === 'Damaged',
        'status-lost': game.value.status === 'Lost'
      };
    });

    const fetchGameDetails = async () => {
      loading.value = true;
      error.value = null;
      
      try {
        const gameData = await GameService.getGameById(userId.value, gameTitle.value, true);
        Object.assign(game.value, gameData);
      } catch (err) {
        error.value = err.message || "Failed to load game details. Please try again.";
        console.error("Error fetching game details:", err);
      } finally {
        loading.value = false;
      }
    };

    onMounted(fetchGameDetails);

    watch(() => route.params.title, (newTitle, oldTitle) => {
      if (newTitle !== oldTitle) {
        fetchGameDetails();
      }
    });

    const saveChanges = async () => {
      submitting.value = true;
      
      try {
        await GameService.updateGame(userId.value, gameTitle.value, game.value);
        
        showSuccess.value = true;
        setTimeout(() => {
          router.push({ name: 'game-copy-view', params: { userId: userId.value } });
        }, 1500);
      } catch (err) {
        error.value = err.message || "Failed to update the game. Please try again.";
        console.error("Error updating game:", err);
        submitting.value = false;
      }
    };

    const closePopup = () => {
      router.push({ name: 'game-copy-view', params: { userId: userId.value } });
    };

    return { 
      userId,
      gameTitle,
      game, 
      loading, 
      error, 
      submitting, 
      showSuccess,
      getStatusClass, 
      fetchGameDetails,
      saveChanges, 
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
