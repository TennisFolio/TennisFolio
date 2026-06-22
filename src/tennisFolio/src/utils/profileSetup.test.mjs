import assert from 'node:assert/strict';
import test from 'node:test';

import { shouldShowProfileSetup } from './profileSetup.js';

test('shows profile setup when nickname or gender is missing', () => {
  assert.equal(shouldShowProfileSetup({ nickName: null }), true);
  assert.equal(shouldShowProfileSetup({ nickName: '' }), true);
  assert.equal(shouldShowProfileSetup({ nickName: 'tester' }), true);
  assert.equal(shouldShowProfileSetup({ nickName: null, gender: 'MALE' }), true);
  assert.equal(shouldShowProfileSetup({ nickName: 'tester', gender: null }), true);
  assert.equal(shouldShowProfileSetup({ nickName: 'tester', gender: 'MALE' }), false);
  assert.equal(shouldShowProfileSetup(null), false);
});
