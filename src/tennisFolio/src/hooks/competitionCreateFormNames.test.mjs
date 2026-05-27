import assert from 'node:assert/strict';
import test from 'node:test';

import {
  createDefaultPlayerNames,
  syncPlayerNames,
  hasInvalidPlayerNameLength,
} from './competitionCreateFormNames.js';

test('creates default player names with a gender prefix', () => {
  assert.deepEqual(createDefaultPlayerNames('M', 4), ['M1', 'M2', 'M3', 'M4']);
});

test('syncPlayerNames appends default names and preserves existing positions', () => {
  assert.deepEqual(syncPlayerNames(['민수', 'M2'], 'M', 4), [
    '민수',
    'M2',
    'M3',
    'M4',
  ]);
});

test('syncPlayerNames trims names from the end when count decreases', () => {
  assert.deepEqual(syncPlayerNames(['민수', '준호', 'M3'], 'M', 2), [
    '민수',
    '준호',
  ]);
});

test('hasInvalidPlayerNameLength ignores blanks and detects names over nine characters after trim', () => {
  assert.equal(hasInvalidPlayerNameLength([' ', '123456789']), false);
  assert.equal(hasInvalidPlayerNameLength([' 1234567890 ']), true);
});
