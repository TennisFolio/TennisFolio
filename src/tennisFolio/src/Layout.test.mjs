import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const layoutSource = readFileSync(new URL('./Layout.jsx', import.meta.url), 'utf8');
const layoutCss = readFileSync(new URL('./Layout.css', import.meta.url), 'utf8');

test('logged-in header exposes a direct my competitions link', () => {
  assert.match(layoutSource, /className="[^"]*header-my-competitions-button/);
  assert.match(layoutSource, /navigate\('\/me\/competitions'\)/);
});

test('my competitions header button uses a high-contrast surface style', () => {
  assert.match(
    layoutCss,
    /\.header-my-competitions-button\s*\{[^}]*background:\s*#ffffff;[^}]*color:\s*#101828;/s,
  );
});

test('logged-in account button uses a compact profile avatar', () => {
  assert.match(layoutSource, /aria-label="\uD504\uB85C\uD544"/);
  assert.match(layoutSource, /className="header-profile-avatar"/);
  assert.doesNotMatch(layoutSource, /className="auth-button-label"/);
});

test('mobile account button keeps the avatar centered in a fixed circle', () => {
  assert.match(
    layoutCss,
    /@media\s*\(max-width:\s*560px\)\s*\{[\s\S]*\.auth-button-account\s*\{[^}]*width:\s*32px;[^}]*height:\s*32px;[^}]*padding:\s*0;[^}]*\}/,
  );
});

test('layout consumes post-login redirect after authentication', () => {
  assert.match(layoutSource, /tennisfolio:postLoginRedirect/);
  assert.match(layoutSource, /if \(!currentUser\) \{/);
  assert.match(layoutSource, /sessionStorage\.removeItem\('tennisfolio:postLoginRedirect'\)/);
  assert.match(layoutSource, /localStorage\.getItem\('tennisfolio:postLoginRedirect'\)/);
  assert.match(layoutSource, /localStorage\.removeItem\('tennisfolio:postLoginRedirect'\)/);
  assert.match(layoutSource, /redirectAfterLogin\.startsWith\('\/'\)/);
  assert.match(layoutSource, /redirectAfterLogin\.startsWith\('\/\/'\)/);
});
