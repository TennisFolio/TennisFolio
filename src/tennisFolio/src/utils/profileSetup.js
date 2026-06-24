export function shouldShowProfileSetup(user) {
  if (!user) {
    return false;
  }

  return !user.nickName?.trim() || !user.gender;
}
