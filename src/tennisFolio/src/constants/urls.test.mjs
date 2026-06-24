import assert from 'node:assert/strict';
import test from 'node:test';

import {
  default_oauth_provider,
  oauth_authorization_urls,
} from './urls.js';

test('uses Kakao as the only public OAuth login provider', () => {
  assert.equal(default_oauth_provider, 'kakao');
  assert.deepEqual(Object.keys(oauth_authorization_urls), ['kakao']);
  assert.match(oauth_authorization_urls.kakao, /\/oauth2\/authorization\/kakao$/);
});
