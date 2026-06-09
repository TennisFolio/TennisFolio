import assert from 'node:assert/strict';
import test from 'node:test';

import {
  COMPETITION_FIELD_LIMITS,
  COMPETITION_FIELDS,
  createCompetitionPayload,
  INITIAL_COMPETITION_FORM,
} from './competitionCreateFormConfig.js';

test('fixed schedule uses totalGames field with default of eight games', () => {
  assert.equal(INITIAL_COMPETITION_FORM.totalGames, 8);
  assert.equal(COMPETITION_FIELD_LIMITS.totalGames.min, 1);
  assert.equal(COMPETITION_FIELD_LIMITS.totalGames.max, 200);

  const totalGamesField = COMPETITION_FIELDS.find(
    (field) => field.name === 'totalGames'
  );

  assert.deepEqual(totalGamesField, {
    name: 'totalGames',
    label: '총 경기 수',
    unit: '게임',
  });
  assert.equal(
    COMPETITION_FIELDS.some((field) => field.name === 'hours'),
    false
  );
});

test('fixed schedule payload sends totalGames and omits hours', () => {
  const payload = createCompetitionPayload({
    form: {
      ...INITIAL_COMPETITION_FORM,
      mode: 'FIXED_SCHEDULE',
      totalGames: 10,
    },
    isClubSession: false,
  });

  assert.equal(payload.totalGames, 10);
  assert.equal(Object.hasOwn(payload, 'hours'), false);
});
