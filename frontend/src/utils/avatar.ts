import { API_BASE_URL } from '@/api/request';

export function resolveAvatarSrc(avatarUrl?: string | null) {
  const normalizedUrl = avatarUrl?.trim();
  if (!normalizedUrl) {
    return '';
  }
  if (/^(https?:|data:|blob:)/i.test(normalizedUrl)) {
    return normalizedUrl;
  }
  if (normalizedUrl.startsWith('/api/')) {
    return normalizedUrl;
  }

  const normalizedBaseUrl = API_BASE_URL.replace(/\/$/, '');
  const normalizedPath = normalizedUrl.startsWith('/') ? normalizedUrl : `/${normalizedUrl}`;
  return `${normalizedBaseUrl}${normalizedPath}`;
}
