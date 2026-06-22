import { oauth_authorization_urls } from '@/constants';
import { apiRequestSilent } from './apiClient';

export const loginWithProvider = (provider) => {
  const url = oauth_authorization_urls[provider];
  if (!url) {
    throw new Error(`Unsupported OAuth provider: ${provider}`);
  }
  window.location.assign(url);
};

export const getCurrentUser = () => apiRequestSilent.get('/api/auth/me');

export const updateProfile = (profile) =>
  apiRequestSilent.patch('/api/auth/profile', profile);

export const logout = () => apiRequestSilent.post('/api/auth/logout');
