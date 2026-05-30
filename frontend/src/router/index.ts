import { createRouter, createWebHistory } from 'vue-router';
import { useUserStore } from '@/stores/user';

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/',
      name: 'home',
      component: () => import('@/views/UserProfileView.vue'),
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/LoginView.vue'),
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/RegisterView.vue'),
    },
    {
      path: '/users',
      name: 'users',
      component: () => import('@/views/UserManagementView.vue'),
      meta: {
        requiresAuth: true,
        requiresAdmin: true,
      },
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/UserProfileView.vue'),
      meta: {
        requiresAuth: true,
      },
    },
    {
      path: '/:pathMatch(.*)*',
      redirect: '/',
    },
  ],
});

router.beforeEach(async (to) => {
  const userStore = useUserStore();
  const requiresAuth = Boolean(to.meta.requiresAuth);
  const requiresAdmin = Boolean(to.meta.requiresAdmin);

  if (to.path === '/') {
    if (!userStore.isLoggedIn) {
      await userStore.fetchCurrentUser();
    }
    if (!userStore.isLoggedIn) {
      return '/login';
    }
    return userStore.isAdmin ? '/users' : '/profile';
  }

  if (requiresAuth && !userStore.isLoggedIn) {
    await userStore.fetchCurrentUser();
  }

  if (requiresAuth && !userStore.isLoggedIn) {
    return {
      path: '/login',
      query: {
        redirect: to.fullPath,
      },
    };
  }

  if (requiresAdmin && !userStore.isAdmin) {
    return '/profile';
  }

  return true;
});

export default router;
