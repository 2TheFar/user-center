import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import type { User } from '@/types/user';

export const useUserStore = defineStore('user', () => {
  const currentUser = ref<User | null>(null);

  const isLoggedIn = computed(() => Boolean(currentUser.value?.id));
  const isAdmin = computed(() => currentUser.value?.userRole === 1);

  function setCurrentUser(user: User | null) {
    currentUser.value = user;
  }

  function clearCurrentUser() {
    currentUser.value = null;
  }

  return {
    currentUser,
    isLoggedIn,
    isAdmin,
    setCurrentUser,
    clearCurrentUser,
  };
});
