import { createRouter, createWebHistory } from 'vue-router'
import GameCopyView from '../views/GameCopyView/GameCopyView.vue'
import HomePage from '../views/HomePageView/HomePageView.vue'
import EditGameView from '@/views/GameCopyView/EditGameView.vue';
import AddGameView from '@/views/GameCopyView/AddGameView.vue';
import CreateEventView from '@/views/CreateEventView/CreateEventView.vue';
import createUserAccountView from "@/views/UserAccountView/CreateUserAccountView.vue";
import LoginView from "@/views/UserAccountView/LoginView.vue";
import UpdateUserAccount from "@/views/UserAccountView/UpdateUserAccount.vue";
import UserSettingView from "@/views/UserAccountView/UserSettingView.vue";
import Secure from "@/views/UserAccountView/Secure.vue";
import AuthService from "@/services/AuthService";
import RegisterEventView from '@/views/RegisterEventView/RegisterEventView.vue';
import GameCatalogView from '@/views/GameCatalogView/GameCatalogView.vue'; // Import GameCatalogView
import CreateBorrowRequestView from '@/views/BorrowRequestView/CreateBorrowRequestView.vue'; // Import CreateBorrowRequestView
import ReviewsView from '@/views/ReviewView/ReviewsView.vue'; // Import ReviewsView

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomePage,
      meta:{
        needsAuth: true
      }
    },
    {
      path: '/your-games/:userId',
      name: 'game-copy-view',
      component: GameCopyView,
      props: true,
      meta:{
        needsAuth: true
      }
    },
    {
      path: '/edit-game/:userId/:title',
      name: 'EditGame',
      component: EditGameView,
      props: true,
      meta:{
        needsAuth: true
      }
    },
    {
      path: '/add-game/:userId', // Modified to include userId as a parameter
      name: 'AddGame',
      component: AddGameView,
      props: true, // Ensure props are passed if the component expects them
      meta:{
        needsAuth: true
      }
    },
    {
      path: '/create-event/:userId',
      name: 'CreateEventView',
      component: CreateEventView,
      meta:{
        needsAuth: true
      }
    },
    {
      path: '/create',
      name: 'create',
      component: createUserAccountView
    },
    {
      path: '/login',
      name: 'Login',
      component: LoginView,
    },
    {
      path: '/update',
      name: 'update',
      component: UpdateUserAccount,
      meta:{
        needsAuth: true
      }
    },
    {
      path: '/userSetting',
      name: 'user setting',
      component: UserSettingView,
      meta:{
        needsAuth: true
      }
    },
    {
      path: '/secure',
      name: 'secure',
      component: Secure
    },
    {
      path: '/join-events',
      name: 'join-events',
      component: RegisterEventView,
      meta: {
        needsAuth: true
      }
    },
    {
      path: '/game-catalog', // Added route for GameCatalogView
      name: 'game-catalog',
      component: GameCatalogView,
      meta: {
        needsAuth: true
      }
    },
    {
      path: '/create-borrow-request', // Added route for CreateBorrowRequestView
      name: 'create-borrow-request',
      component: CreateBorrowRequestView, // Make sure this component exists
      meta: {
        needsAuth: true
      },
      props: route => ({ gameId: route.query.gameId })
    },
    {
      path: '/reviews', // Added route for ReviewsView
      name: 'reviews',
      component: ReviewsView, // Make sure this component exists
      meta: {
        needsAuth: true
      },
      props: route => ({ gameId: route.query.gameId })
    }
  ]
})

router.beforeEach((to, from, next) => {
  const auth = to.meta?.needsAuth;
  const isLoggedIn = AuthService.isLoggedIn();
  if (auth && !isLoggedIn) {
    next({ name: 'secure' });
  }
  else {
    next();
  }
});

export default router
