<template>
  <button class="home-button" @click="goHome" title="Go to Home">
    <HomeIcon />
    <span v-if="showText">Home</span>
  </button>
</template>

<script>
import { useRouter } from 'vue-router';
import HomeIcon from './icons/HomeIcon.vue';

export default {
  name: 'HomeButton',
  components: {
    HomeIcon
  },
  props: {
    showText: {
      type: Boolean,
      default: true
    }
  },
  setup() {
    const router = useRouter();
    
    const goHome = () => {
      router.push({ name: 'home' });
    };
    
    return { goHome };
  }
};
</script>

<style scoped>
.home-button {
  position: fixed;
  bottom: 2rem;
  right: 2rem;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  background-color: #ff6f61;
  color: white;
  border: none;
  border-radius: 50px;
  padding: 0.75rem 1.25rem;
  cursor: pointer;
  box-shadow: 0 4px 8px rgba(0, 0, 0, 0.2);
  transition: all 0.2s ease;
  z-index: 100;
  font-family: 'Inter', sans-serif;
  font-weight: 500;
}

.home-button:hover {
  background-color: #e65a50;
  transform: translateY(-3px);
  box-shadow: 0 6px 12px rgba(0, 0, 0, 0.3);
}

@media (max-width: 768px) {
  .home-button {
    bottom: 1.5rem;
    right: 1.5rem;
    padding: 0.6rem;
  }
  
  .home-button span {
    display: none;
  }
}
</style>
