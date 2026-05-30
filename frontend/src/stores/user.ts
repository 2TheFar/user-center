import { defineStore } from 'pinia';
import { computed, ref } from 'vue';
import { getCurrentUser } from '@/api/user';
import type { User } from '@/types/user';

export const useUserStore = defineStore('user', () => {
  const currentUser = ref<User | null>(null);
  const currentUserLoading = ref(false);

  const isLoggedIn = computed(() => Boolean(currentUser.value?.id));
  const isAdmin = computed(() => currentUser.value?.userRole === 1);

  function setCurrentUser(user: User | null) {
    currentUser.value = user;
  }

  function clearCurrentUser() {
    currentUser.value = null;
  }

  async function fetchCurrentUser() {
    currentUserLoading.value = true;
    try {
      const user = await getCurrentUser();
      currentUser.value = user?.id ? user : null;
      return currentUser.value;
    } catch {
      currentUser.value = null;
      return null;
    } finally {
      currentUserLoading.value = false;
    }
  }

  return {
    currentUser,
    currentUserLoading,
    isLoggedIn,
    isAdmin,
    setCurrentUser,
    clearCurrentUser,
    fetchCurrentUser,
  };
});
