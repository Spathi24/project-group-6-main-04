import Cell from '@/components/Cell.vue'
import Size16 from '@/components/Size16.vue'
import Trash2 from '@/components/icons/Trash2.vue'
import Edit from '@/components/icons/Edit.vue'
import StatusCircle from '@/components/icons/StatusCircle.vue'
import HomeButton from '@/components/HomeButton.vue'
import GameService from '@/services/GameCopyService'
import { ref, onMounted, computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'

export default {
  name: 'GameCopyView',
  components: {
    Cell,
    Size16,
    Trash2,
    Edit,
    StatusCircle,
    HomeButton
  },
  props: {
    userId: {
      type: [Number, String],
      required: true
    }
  },
  setup(props) {
    const router = useRouter();
    const route = useRoute();
    const games = ref([]);
    const loading = ref(true);
    const error = ref(null);
    const searchQuery = ref('');
    const selectedGames = ref([]);
    const showDeleteConfirm = ref(false);
    const deleteType = ref('single'); // 'single' or 'multiple'
    const gameToDelete = ref(null);
    
    // Get userId from props or route params
    const userId = computed(() => props.userId || route.params.userId);

    // Fetch games from the backend
    const fetchGames = async () => {
      loading.value = true;
      error.value = null;
      
      try {
        const data = await GameService.getUserGames(userId.value);
        games.value = data;
      } catch (err) {
        error.value = err.message || "Failed to load your games. Please try again.";
        console.error("Error fetching games:", err);
      } finally {
        loading.value = false;
      }
    };

    onMounted(() => {
      fetchGames();
    });

    // Filter games based on search query
    const filteredGames = computed(() => {
      if (!searchQuery.value) return games.value;
      
      const query = searchQuery.value.toLowerCase();
      return games.value.filter(game => {
        // Handle both title/name property possibilities
        const gameTitle = game.title ? game.title.toLowerCase() : '';
        return gameTitle.includes(query)

      });
    });

    // Reset search query
    const filterGames = () => {
      // This function can be enhanced for more advanced filtering logic
    };

    // Game selection logic
    const toggleSelect = (title) => {
      const index = selectedGames.value.indexOf(title);
      if (index === -1) {
        selectedGames.value.push(title);
      } else {
        selectedGames.value.splice(index, 1);
      }
    };

    const isSelected = (title) => {
      return selectedGames.value.includes(title);
    };

    const toggleSelectAll = () => {
      if (selectedGames.value.length === games.value.length) {
        selectedGames.value = [];
      } else {
        selectedGames.value = games.value.map(game => game.title);
      }
    };

    // Navigation
    const editGame = (title) => {
      router.push({ 
        name: 'EditGame', 
        params: { 
          userId: userId.value,
          title: title 
        } 
      });
    };
    
    const addGame = () => {
      router.push({ 
        name: 'AddGame',
        params: {
          userId: userId.value
        }
      });
    };

    // Delete functionality
    const confirmDeleteGame = (title) => {
      gameToDelete.value = title;
      deleteType.value = 'single';
      showDeleteConfirm.value = true;
    };

    const deleteSelected = () => {
      if (selectedGames.value.length > 0) {
        deleteType.value = 'multiple';
        showDeleteConfirm.value = true;
      }
    };

    const cancelDelete = () => {
      showDeleteConfirm.value = false;
      gameToDelete.value = null;
    };

    const proceedWithDelete = async () => {
      try {
        if (deleteType.value === 'single') {
          await GameService.deleteGame(userId.value, gameToDelete.value);
          games.value = games.value.filter(game => game.title !== gameToDelete.value);
        } else {
          await GameService.deleteManyGames(userId.value, selectedGames.value);
          games.value = games.value.filter(game => !selectedGames.value.includes(game.title));
          selectedGames.value = [];
        }
        showDeleteConfirm.value = false;
      } catch (err) {
        error.value = err.message || "Failed to delete the game(s). Please try again.";
        console.error("Error deleting games:", err);
      }
    };

    const capitalizedStatus = (status) => {
      if (!status) return ''
      return status.charAt(0).toUpperCase() + status.slice(1).toLowerCase()
    }

    // Function to check if a game status is "Borrowed"
    const isBorrowed = (status) => {
      return status && status.toLowerCase() === 'borrowed';
    };

    return {
      userId,
      games, 
      loading, 
      error, 
      searchQuery,
      filteredGames,
      selectedGames,
      showDeleteConfirm,
      deleteType,
      editGame,
      addGame,
      fetchGames,
      filterGames,
      toggleSelect,
      isSelected,
      toggleSelectAll,
      confirmDeleteGame,
      deleteSelected,
      cancelDelete,
      proceedWithDelete,
      capitalizedStatus,
      isBorrowed
    };
  }
}