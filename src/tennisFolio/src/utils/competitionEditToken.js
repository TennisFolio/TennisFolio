const editTokenKey = (publicId) => `competition:${publicId}:editToken`;

export function getCompetitionEditToken(publicId) {
  if (!publicId) {
    return '';
  }
  return localStorage.getItem(editTokenKey(publicId)) ?? '';
}

export function saveCompetitionEditToken(publicId, editToken) {
  if (!publicId || !editToken) {
    return;
  }
  localStorage.setItem(editTokenKey(publicId), editToken);
}

export function createEditTokenHeaders(editToken) {
  return editToken
    ? {
        'X-Competition-Edit-Token': editToken,
      }
    : {};
}
