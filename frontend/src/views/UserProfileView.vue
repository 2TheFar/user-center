<template>
  <a-layout class="app-layout">
    <AppHeader />

    <a-layout-content class="content-wrap profile-content">
      <section class="page-title-row">
        <div>
          <h1>我的资料</h1>
          <p>维护当前登录账号的基础资料，账号、角色和状态由系统统一管理。</p>
        </div>
        <a-space wrap>
          <a-button :loading="userStore.currentUserLoading" @click="refreshCurrentUser">
            <template #icon>
              <ReloadOutlined />
            </template>
            刷新
          </a-button>
        </a-space>
      </section>

      <section class="current-user-panel profile-summary">
        <div class="current-user-main">
          <a-avatar :src="avatarPreview || undefined" :size="56">
            {{ userInitial }}
          </a-avatar>
          <div>
            <h2>{{ displayName }}</h2>
            <p>{{ accountText }}</p>
          </div>
        </div>
        <a-descriptions size="small" :column="{ xs: 1, sm: 2, md: 3 }">
          <a-descriptions-item label="用户 ID">
            {{ userStore.currentUser?.id || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="账号">
            {{ userStore.currentUser?.userAccount || '-' }}
          </a-descriptions-item>
          <a-descriptions-item label="角色">
            <a-tag :color="userStore.isAdmin ? 'green' : 'blue'">
              {{ userStore.isAdmin ? '管理员' : '普通用户' }}
            </a-tag>
          </a-descriptions-item>
        </a-descriptions>
      </section>

      <section class="workspace-panel profile-form-panel">
        <div class="panel-heading">
          <div>
            <h2>修改信息</h2>
            <p>保存后会同步更新页面顶部展示的昵称和头像。</p>
          </div>
        </div>

        <a-form
          class="profile-form"
          :model="formState"
          :rules="rules"
          :label-col="{ xs: { span: 24 }, sm: { span: 4 } }"
          :wrapper-col="{ xs: { span: 24 }, sm: { span: 18 } }"
          @finish="handleSubmit"
        >
          <a-form-item label="昵称" name="username">
            <a-input v-model:value="formState.username" placeholder="请输入昵称" allow-clear>
              <template #prefix>
                <UserOutlined />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item label="头像" name="avatarUrl">
            <div class="avatar-upload-row">
              <a-avatar :src="avatarPreview || undefined" :size="72">
                {{ userInitial }}
              </a-avatar>
              <div class="avatar-upload-actions">
                <a-space wrap>
                  <a-button @click="openAvatarFilePicker">
                    <template #icon>
                      <UploadOutlined />
                    </template>
                    选择图片
                  </a-button>
                  <a-button v-if="formState.avatarUrl || pendingAvatarPreview" @click="clearAvatar">
                    <template #icon>
                      <DeleteOutlined />
                    </template>
                    移除头像
                  </a-button>
                </a-space>
              </div>
              <input
                ref="avatarInputRef"
                class="avatar-file-input"
                type="file"
                accept="image/jpeg,image/png,image/webp,image/gif"
                aria-hidden="true"
                hidden
                style="display: none"
                tabindex="-1"
                @change="handleAvatarFileChange"
              />
            </div>
          </a-form-item>

          <a-form-item label="性别" name="gender">
            <a-radio-group v-model:value="formState.gender">
              <a-radio :value="0">女</a-radio>
              <a-radio :value="1">男</a-radio>
              <a-radio :value="null">保密</a-radio>
            </a-radio-group>
          </a-form-item>

          <a-form-item label="手机" name="phone">
            <a-input v-model:value="formState.phone" placeholder="请输入手机号" allow-clear>
              <template #prefix>
                <PhoneOutlined />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item label="邮箱" name="email">
            <a-input v-model:value="formState.email" placeholder="请输入邮箱" allow-clear>
              <template #prefix>
                <MailOutlined />
              </template>
            </a-input>
          </a-form-item>

          <a-form-item :wrapper-col="{ xs: { span: 24 }, sm: { offset: 4, span: 18 } }">
            <a-space wrap>
              <a-button type="primary" html-type="submit" :loading="saving">
                <template #icon>
                  <SaveOutlined />
                </template>
                保存资料
              </a-button>
              <a-button @click="resetForm">恢复当前资料</a-button>
            </a-space>
          </a-form-item>
        </a-form>
      </section>
    </a-layout-content>

    <a-modal
      v-model:open="cropModalOpen"
      title="裁剪头像"
      ok-text="使用头像"
      cancel-text="取消"
      :ok-button-props="{ disabled: !cropSourceUrl }"
      @ok="confirmAvatarCrop"
      @cancel="closeCropModal"
    >
      <div v-if="cropSourceUrl" class="avatar-cropper">
        <div
          class="avatar-crop-stage"
          :class="{ 'is-dragging': isCropDragging }"
          @pointerdown="startCropDrag"
          @pointermove="handleCropDrag"
          @pointerup="stopCropDrag"
          @pointercancel="stopCropDrag"
          @wheel.prevent="handleCropWheel"
        >
          <img
            ref="cropImageRef"
            class="avatar-crop-image"
            :src="cropSourceUrl"
            :style="cropImageStyle"
            alt=""
            @load="handleCropImageLoad"
          />
        </div>
        <p class="avatar-crop-hint">拖动图片调整位置，滚轮或滑杆缩放。</p>
        <div class="avatar-crop-controls">
          <label>
            缩放
            <a-slider v-model:value="cropZoom" :min="1" :max="3" :step="0.01" />
          </label>
          <a-button size="small" @click="resetCropPosition">居中</a-button>
        </div>
      </div>
    </a-modal>
  </a-layout>
</template>

<script setup lang="ts">
import { computed, onMounted, onUnmounted, reactive, ref, watch } from 'vue';
import { useRouter } from 'vue-router';
import type { FormProps } from 'ant-design-vue';
import { message } from 'ant-design-vue';
import {
  DeleteOutlined,
  MailOutlined,
  PhoneOutlined,
  ReloadOutlined,
  SaveOutlined,
  UploadOutlined,
  UserOutlined,
} from '@ant-design/icons-vue';

import { updateCurrentUserProfile, uploadCurrentUserAvatar } from '@/api/user';
import AppHeader from '@/components/AppHeader.vue';
import { useUserStore } from '@/stores/user';
import type { UpdateProfilePayload, User } from '@/types/user';
import { resolveAvatarSrc } from '@/utils/avatar';

const router = useRouter();
const userStore = useUserStore();
const saving = ref(false);
const avatarInputRef = ref<HTMLInputElement | null>(null);
const cropImageRef = ref<HTMLImageElement | null>(null);
const pendingAvatarBlob = ref<Blob | null>(null);
const pendingAvatarPreview = ref('');
const cropModalOpen = ref(false);
const cropSourceUrl = ref('');
const cropZoom = ref(1);
const cropOffsetX = ref(0);
const cropOffsetY = ref(0);
const isCropDragging = ref(false);
const cropImageSize = reactive({
  width: 0,
  height: 0,
});
const cropDragStart = reactive({
  pointerId: 0,
  clientX: 0,
  clientY: 0,
  offsetX: 0,
  offsetY: 0,
});

const CROP_VIEWPORT_SIZE = 280;
const AVATAR_OUTPUT_SIZE = 512;
const MAX_AVATAR_SIZE = 5 * 1024 * 1024;
const ALLOWED_AVATAR_TYPES = ['image/jpeg', 'image/png', 'image/webp', 'image/gif'];

const formState = reactive<UpdateProfilePayload>({
  username: '',
  avatarUrl: '',
  phone: '',
  email: '',
  gender: null,
});

const rules: FormProps['rules'] = {
  username: [{ max: 256, message: '昵称不能超过 256 个字符', trigger: 'blur' }],
  phone: [
    {
      pattern: /^$|^1[3-9]\d{9}$/,
      message: '请输入正确的手机号',
      trigger: 'blur',
    },
  ],
  email: [
    { max: 256, message: '邮箱不能超过 256 个字符', trigger: 'blur' },
    {
      type: 'email',
      message: '请输入正确的邮箱',
      trigger: 'blur',
    },
  ],
};

const displayName = computed(
  () => formState.username?.trim() || userStore.currentUser?.userAccount || '未命名用户',
);
const accountText = computed(() => `账号：${userStore.currentUser?.userAccount || '-'}`);
const avatarPreview = computed(() => pendingAvatarPreview.value || resolveAvatarSrc(formState.avatarUrl));
const userInitial = computed(() => displayName.value.slice(0, 1).toUpperCase());
const cropDisplaySize = computed(() => {
  if (!cropImageSize.width || !cropImageSize.height) {
    return { width: CROP_VIEWPORT_SIZE, height: CROP_VIEWPORT_SIZE };
  }

  const coverScale =
    Math.max(CROP_VIEWPORT_SIZE / cropImageSize.width, CROP_VIEWPORT_SIZE / cropImageSize.height) *
    cropZoom.value;
  return {
    width: cropImageSize.width * coverScale,
    height: cropImageSize.height * coverScale,
  };
});
const cropMaxOffsetX = computed(() => Math.max(0, Math.round((cropDisplaySize.value.width - CROP_VIEWPORT_SIZE) / 2)));
const cropMaxOffsetY = computed(() =>
  Math.max(0, Math.round((cropDisplaySize.value.height - CROP_VIEWPORT_SIZE) / 2)),
);
const cropImageStyle = computed(() => ({
  width: `${cropDisplaySize.value.width}px`,
  height: `${cropDisplaySize.value.height}px`,
  transform: `translate(calc(-50% + ${cropOffsetX.value}px), calc(-50% + ${cropOffsetY.value}px))`,
}));

onMounted(async () => {
  if (!userStore.currentUser) {
    const user = await userStore.fetchCurrentUser();
    if (!user) {
      await router.replace({
        path: '/login',
        query: { redirect: '/profile' },
      });
      return;
    }
  }
  fillForm(userStore.currentUser);
});

onUnmounted(() => {
  revokePreviewUrl();
  revokeCropSourceUrl();
});

watch(
  () => userStore.currentUser,
  (user) => {
    fillForm(user);
  },
);

watch([cropZoom, cropMaxOffsetX, cropMaxOffsetY], clampCropOffsets);

async function refreshCurrentUser() {
  const user = await userStore.fetchCurrentUser();
  if (!user) {
    await router.replace({
      path: '/login',
      query: { redirect: '/profile' },
    });
  }
}

async function handleSubmit() {
  saving.value = true;
  try {
    let nextAvatarUrl = normalizeValue(formState.avatarUrl);
    if (pendingAvatarBlob.value) {
      const avatarFile = new File([pendingAvatarBlob.value], 'avatar.png', { type: 'image/png' });
      nextAvatarUrl = await uploadCurrentUserAvatar(avatarFile);
      formState.avatarUrl = nextAvatarUrl || '';
    }

    const updatedUser = await updateCurrentUserProfile({
      ...buildPayload(),
      avatarUrl: nextAvatarUrl,
    });
    if (!updatedUser?.id) {
      message.error('保存失败，请稍后重试');
      return;
    }

    pendingAvatarBlob.value = null;
    revokePreviewUrl();
    userStore.setCurrentUser(updatedUser);
    message.success('资料保存成功');
  } catch {
    // 统一错误提示由 request 拦截器处理。
  } finally {
    saving.value = false;
  }
}

function resetForm() {
  pendingAvatarBlob.value = null;
  revokePreviewUrl();
  fillForm(userStore.currentUser);
}

function fillForm(user: User | null) {
  formState.username = user?.username || '';
  formState.avatarUrl = user?.avatarUrl || '';
  formState.phone = user?.phone || '';
  formState.email = user?.email || '';
  formState.gender = user?.gender ?? null;
}

function openAvatarFilePicker() {
  avatarInputRef.value?.click();
}

function handleAvatarFileChange(event: Event) {
  const input = event.target as HTMLInputElement;
  const file = input.files?.[0];
  input.value = '';
  if (!file) {
    return;
  }
  if (!ALLOWED_AVATAR_TYPES.includes(file.type)) {
    message.warning('头像只支持 JPG、PNG、WEBP 或 GIF 图片');
    return;
  }
  if (file.size > MAX_AVATAR_SIZE) {
    message.warning('头像图片不能超过 5MB');
    return;
  }

  revokeCropSourceUrl();
  cropSourceUrl.value = URL.createObjectURL(file);
  cropZoom.value = 1;
  cropOffsetX.value = 0;
  cropOffsetY.value = 0;
  cropImageSize.width = 0;
  cropImageSize.height = 0;
  cropModalOpen.value = true;
}

function handleCropImageLoad() {
  const image = cropImageRef.value;
  if (!image) {
    return;
  }
  cropImageSize.width = image.naturalWidth;
  cropImageSize.height = image.naturalHeight;
  clampCropOffsets();
}

function startCropDrag(event: PointerEvent) {
  if (!cropSourceUrl.value) {
    return;
  }

  isCropDragging.value = true;
  cropDragStart.pointerId = event.pointerId;
  cropDragStart.clientX = event.clientX;
  cropDragStart.clientY = event.clientY;
  cropDragStart.offsetX = cropOffsetX.value;
  cropDragStart.offsetY = cropOffsetY.value;
  (event.currentTarget as HTMLElement).setPointerCapture(event.pointerId);
}

function handleCropDrag(event: PointerEvent) {
  if (!isCropDragging.value || event.pointerId !== cropDragStart.pointerId) {
    return;
  }

  const nextOffsetX = cropDragStart.offsetX + event.clientX - cropDragStart.clientX;
  const nextOffsetY = cropDragStart.offsetY + event.clientY - cropDragStart.clientY;
  cropOffsetX.value = clamp(nextOffsetX, -cropMaxOffsetX.value, cropMaxOffsetX.value);
  cropOffsetY.value = clamp(nextOffsetY, -cropMaxOffsetY.value, cropMaxOffsetY.value);
}

function stopCropDrag(event: PointerEvent) {
  if (!isCropDragging.value || event.pointerId !== cropDragStart.pointerId) {
    return;
  }

  isCropDragging.value = false;
  const target = event.currentTarget as HTMLElement;
  if (target.hasPointerCapture(event.pointerId)) {
    target.releasePointerCapture(event.pointerId);
  }
}

function handleCropWheel(event: WheelEvent) {
  const direction = event.deltaY > 0 ? -1 : 1;
  cropZoom.value = Number(clamp(cropZoom.value + direction * 0.08, 1, 3).toFixed(2));
}

function resetCropPosition() {
  cropOffsetX.value = 0;
  cropOffsetY.value = 0;
}

async function confirmAvatarCrop() {
  const image = cropImageRef.value;
  if (!image) {
    return;
  }

  const canvas = document.createElement('canvas');
  canvas.width = AVATAR_OUTPUT_SIZE;
  canvas.height = AVATAR_OUTPUT_SIZE;
  const context = canvas.getContext('2d');
  if (!context) {
    message.error('头像裁剪失败，请重试');
    return;
  }

  const canvasScale = AVATAR_OUTPUT_SIZE / CROP_VIEWPORT_SIZE;
  const drawWidth = cropDisplaySize.value.width * canvasScale;
  const drawHeight = cropDisplaySize.value.height * canvasScale;
  const drawX = ((CROP_VIEWPORT_SIZE - cropDisplaySize.value.width) / 2 + cropOffsetX.value) * canvasScale;
  const drawY = ((CROP_VIEWPORT_SIZE - cropDisplaySize.value.height) / 2 + cropOffsetY.value) * canvasScale;

  context.clearRect(0, 0, AVATAR_OUTPUT_SIZE, AVATAR_OUTPUT_SIZE);
  context.drawImage(image, drawX, drawY, drawWidth, drawHeight);

  try {
    const blob = await getCanvasBlob(canvas);
    pendingAvatarBlob.value = blob;
    revokePreviewUrl();
    pendingAvatarPreview.value = URL.createObjectURL(blob);
    closeCropModal();
  } catch {
    message.error('头像裁剪失败，请重试');
  }
}

function closeCropModal() {
  isCropDragging.value = false;
  cropModalOpen.value = false;
  revokeCropSourceUrl();
}

function clearAvatar() {
  pendingAvatarBlob.value = null;
  revokePreviewUrl();
  formState.avatarUrl = '';
}

function buildPayload(): UpdateProfilePayload {
  return {
    username: normalizeValue(formState.username),
    avatarUrl: normalizeValue(formState.avatarUrl),
    phone: normalizeValue(formState.phone),
    email: normalizeValue(formState.email),
    gender: formState.gender ?? null,
  };
}

function normalizeValue(value?: string | null) {
  const normalizedValue = value?.trim();
  return normalizedValue || null;
}

function getCanvasBlob(canvas: HTMLCanvasElement) {
  return new Promise<Blob>((resolve, reject) => {
    canvas.toBlob((blob) => {
      if (blob) {
        resolve(blob);
        return;
      }
      reject(new Error('Canvas blob is empty'));
    }, 'image/png');
  });
}

function clampCropOffsets() {
  cropOffsetX.value = clamp(cropOffsetX.value, -cropMaxOffsetX.value, cropMaxOffsetX.value);
  cropOffsetY.value = clamp(cropOffsetY.value, -cropMaxOffsetY.value, cropMaxOffsetY.value);
}

function clamp(value: number, min: number, max: number) {
  return Math.min(max, Math.max(min, value));
}

function revokePreviewUrl() {
  if (pendingAvatarPreview.value) {
    URL.revokeObjectURL(pendingAvatarPreview.value);
    pendingAvatarPreview.value = '';
  }
}

function revokeCropSourceUrl() {
  if (cropSourceUrl.value) {
    URL.revokeObjectURL(cropSourceUrl.value);
    cropSourceUrl.value = '';
  }
}
</script>
