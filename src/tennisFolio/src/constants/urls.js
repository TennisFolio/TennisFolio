export const base_url = 'https://tennisfolio.net';
export const base_server_url =
  import.meta.env?.VITE_BASE_SERVER_URL || 'http://localhost:8080';
export const base_image_url = 'https://tennisfolio.net/img';

export const default_oauth_provider = 'kakao';

export const oauth_authorization_urls = {
  kakao: `${base_server_url}/oauth2/authorization/kakao`,
};
