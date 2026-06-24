import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const pageSource = readFileSync(
  new URL('./MyCompetitions.jsx', import.meta.url),
  'utf8',
);

const pageCss = readFileSync(
  new URL('./MyCompetitions.css', import.meta.url),
  'utf8',
);

function getCssRule(selector) {
  const match = pageCss.match(
    new RegExp(`${selector.replaceAll('.', '\\.')}\\s*\\{[^}]*\\}`)
  );

  return match?.[0] || '';
}

test('my competitions header presents title copy and action in one card', () => {
  const headerRule = getCssRule('.my-competitions-header');
  const createButtonRule = getCssRule('.my-competitions-create-button');

  assert.match(pageSource, /className="my-competitions-title-block"/);
  assert.match(pageSource, /className="my-competitions-title-row"/);
  assert.match(pageSource, /className="my-competitions-create-button"/);
  assert.match(
    headerRule,
    /padding:\s*16px;[\s\S]*?border:\s*1px solid var\(--competition-border\);[\s\S]*?border-radius:\s*8px;[\s\S]*?background:\s*var\(--competition-surface\);/,
  );
  assert.match(
    createButtonRule,
    /width:\s*100%;[\s\S]*?background:\s*var\(--competition-primary\);/,
  );
});

test('my competitions delete success message disappears automatically', () => {
  assert.match(pageSource, /setActionMessage\('/);
  assert.match(pageSource, /setTimeout\(\(\) =>/);
  assert.match(pageSource, /setActionMessage\(''\)/);
  assert.match(pageSource, /clearTimeout\(timerId\)/);
});

test('my competitions state text uses explicit page typography', () => {
  assert.match(
    pageCss,
    /\.my-competitions-state,[\s\S]*?font-size:\s*14px;[\s\S]*?font-weight:\s*800;[\s\S]*?line-height:\s*1\.5;/,
  );
  assert.match(pageCss, /\.my-competitions-state,[\s\S]*?margin:\s*0 0 10px;/);
});
