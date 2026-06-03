export const PLAYER_NAME_MAX_LENGTH = 9;

export function createDefaultPlayerNames(prefix, count) {
  return Array.from({ length: count }, (_, index) => `${prefix}${index + 1}`);
}

export function syncPlayerNames(currentNames = [], prefix, count) {
  return Array.from({ length: count }, (_, index) => {
    const currentName = currentNames[index];
    return currentName === undefined ? `${prefix}${index + 1}` : currentName;
  });
}

export function hasInvalidPlayerNameLength(playerNames = []) {
  return playerNames.some(
    (playerName) => playerName.trim().length > PLAYER_NAME_MAX_LENGTH
  );
}
