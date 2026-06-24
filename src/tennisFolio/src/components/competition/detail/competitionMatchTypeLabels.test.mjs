import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const gameCardSource = readFileSync(
  new URL('./CompetitionGameCard.jsx', import.meta.url),
  'utf8',
);

const summarySource = readFileSync(
  new URL('./CompetitionDetailSummary.jsx', import.meta.url),
  'utf8',
);

test('M2F2 split match type is labeled as male two female two', () => {
  assert.match(gameCardSource, /M2F2_SPLIT:\s*'남2:여2'/);
  assert.match(summarySource, />남2:여2</);
  assert.doesNotMatch(gameCardSource, /2:2 배정/);
  assert.doesNotMatch(summarySource, /2:2 배정/);
});
