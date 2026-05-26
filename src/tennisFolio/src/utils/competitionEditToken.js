const adminTokenKey = (publicId) => `competition:${publicId}:adminToken`;

export function getCompetitionAdminToken(publicId) {
  if (!publicId) {
    return '';
  }
  return localStorage.getItem(adminTokenKey(publicId)) ?? '';
}

export function saveCompetitionAdminToken(publicId, adminToken) {
  if (!publicId || !adminToken) {
    return;
  }
  localStorage.setItem(adminTokenKey(publicId), adminToken);
}

export function clearCompetitionAdminToken(publicId) {
  if (!publicId) {
    return;
  }
  localStorage.removeItem(adminTokenKey(publicId));
}

export function createAdminTokenHeaders(adminToken) {
  return adminToken
    ? {
        'X-Competition-Admin-Token': adminToken,
      }
    : {};
}
