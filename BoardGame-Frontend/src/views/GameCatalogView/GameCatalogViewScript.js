import GameService from '@/services/GameService';

export default {
  name: 'GameCatalogView',
  data() {
    return {
      games: [],
      loading: true,
      error: null
    };
  },
  created() {
    this.fetchGames();
  },
  methods: {
    async fetchGames() {
      this.loading = true;
      try {
        const response = await GameService.getAllGames();
        this.games = response.data;
        this.error = null;
      } catch (err) {
        console.error('Error fetching games:', err);
        this.error = 'Failed to load games. Please try again later.';
      } finally {
        this.loading = false;
      }
    },
    navigateToAddGame() {
      this.$router.push('/games/add');
    },
    createBorrowRequest(gameId) {
      this.$router.push(`/borrow-request/create/${gameId}`);
    },
    viewReviews(gameId) {
      this.$router.push(`/games/${gameId}/reviews`);
    }
  }
};
