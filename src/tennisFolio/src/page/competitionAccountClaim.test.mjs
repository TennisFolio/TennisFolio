import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const authApiSource = readFileSync(
  new URL('../utils/authApi.js', import.meta.url),
  'utf8',
);

const detailSource = readFileSync(
  new URL('./CompetitionDetail.jsx', import.meta.url),
  'utf8',
);

const detailCss = readFileSync(
  new URL('./CompetitionDetail.css', import.meta.url),
  'utf8',
);

test('auth API exposes competition claim request', () => {
  assert.match(authApiSource, /export const claimMyCompetition/);
  assert.match(authApiSource, /\/api\/auth\/me\/competitions\/\$\{publicId\}\/claim/);
  assert.match(authApiSource, /createAdminTokenHeaders\(adminToken\)/);
});

test('competition detail renders account save action from claim flow', () => {
  assert.match(detailSource, /claimAfterLogin/);
  assert.match(detailSource, /claimMyCompetition/);
  assert.match(detailSource, /sessionStorage\.setItem\('tennisfolio:postLoginRedirect', redirectPath\)/);
  assert.match(detailSource, /localStorage\.setItem\('tennisfolio:postLoginRedirect', redirectPath\)/);
  assert.match(detailSource, /내 계정에 저장/);
  assert.match(detailSource, /로그인 후 저장/);
  assert.doesNotMatch(detailSource, /내 계정에 저장된 경기입니다\./);
  assert.match(detailSource, /setTimeout\(\(\) =>/);
  assert.match(detailSource, /setAccountClaimMessage\(''\)/);
  assert.match(detailSource, /setAccountClaimError\(''\)/);
  assert.match(detailSource, /\[accountClaimMessage,\s*accountClaimError\]/);
  assert.match(detailSource, /이미 다른 계정에 저장된 경기입니다\./);
});

test('competition account save banner uses competition theme variables', () => {
  assert.match(detailCss, /\.competition-account-claim/);
  assert.match(detailCss, /var\(--competition-surface-accent\)/);
  assert.match(detailCss, /var\(--competition-border\)/);
});
