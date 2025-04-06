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
      path: '/add-game/:userId',
      name: 'AddGame',
      component: AddGameView,
      props: true,
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
    }
  ]
})

router.beforeEach((to, from, next) => {
  const auth = to.meta?.needsAuth;
  const storedAccountID = localStorage.getItem('userAccountID');
  if (auth && !(storedAccountID)) {
    next({ name: 'secure' });
  }
  else {
    next();
  }
});

export default router
