import assert from 'node:assert/strict';
import test from 'node:test';

import { PROFILE_SETUP_COPY } from './profileSetupCopy.js';

test('profile setup copy is shown in Korean', () => {
  assert.equal(PROFILE_SETUP_COPY.title, '프로필 설정');
  assert.equal(PROFILE_SETUP_COPY.nickNameLabel, '닉네임');
  assert.equal(PROFILE_SETUP_COPY.genderLabel, '성별');
  assert.equal(PROFILE_SETUP_COPY.maleLabel, '남성');
  assert.equal(PROFILE_SETUP_COPY.femaleLabel, '여성');
  assert.equal(PROFILE_SETUP_COPY.saveLabel, '저장');
  assert.equal(PROFILE_SETUP_COPY.savingLabel, '저장 중');
  assert.equal(PROFILE_SETUP_COPY.errorMessage, '저장에 실패했어요.');
});
