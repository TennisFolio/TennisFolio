const editTokenKey = (publicId) => `competition:${publicId}:editToken`;
const adminLinkPromptKey = (publicId) => `competition:${publicId}:adminLinkPrompt`;
const adminLinkPromptDismissedKey = (publicId) =>
  `competition:${publicId}:adminLinkPromptDismissed`;

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

export function markCompetitionAdminLinkPrompt(publicId) {
  if (!publicId) {
    return;
  }
  localStorage.setItem(adminLinkPromptKey(publicId), '1');
}

export function shouldShowCompetitionAdminLinkPrompt(publicId) {
  if (!publicId) {
    return false;
  }

  return (
    localStorage.getItem(adminLinkPromptKey(publicId)) === '1' &&
    localStorage.getItem(adminLinkPromptDismissedKey(publicId)) !== '1'
  );
}

export function dismissCompetitionAdminLinkPrompt(publicId) {
  if (!publicId) {
    return;
  }

  localStorage.setItem(adminLinkPromptDismissedKey(publicId), '1');
  localStorage.removeItem(adminLinkPromptKey(publicId));
}

export function createEditTokenHeaders(editToken) {
  return editToken
    ? {
        'X-Competition-Edit-Token': editToken,
      }
    : {};
}
