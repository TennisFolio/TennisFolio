import assert from 'node:assert/strict';
import test from 'node:test';

import {
  COMPETITION_CREATE_MODES,
  COMPETITION_FIELD_LIMITS,
  COMPETITION_FIELDS,
  canAllocateSameGenderDoublesOnlyGames,
  createCompetitionPayload,
  INITIAL_COMPETITION_FORM,
  getSameGenderDoublesOnlyUnavailableReason,
  isSameGenderDoublesOnlyUnavailable,
} from './competitionCreateFormConfig.js';

test('competition create form defaults to fixed schedule mode', () => {
  assert.equal(
    INITIAL_COMPETITION_FORM.mode,
    COMPETITION_CREATE_MODES.FIXED_SCHEDULE
  );
});

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

test('fixed schedule payload sends sameGenderDoublesOnly when enabled', () => {
  const payload = createCompetitionPayload({
    form: {
      ...INITIAL_COMPETITION_FORM,
      mode: 'FIXED_SCHEDULE',
      sameGenderDoublesOnly: true,
    },
    isClubSession: false,
  });

  assert.equal(payload.sameGenderDoublesOnly, true);
});

test('club session payload does not send sameGenderDoublesOnly', () => {
  const payload = createCompetitionPayload({
    form: {
      ...INITIAL_COMPETITION_FORM,
      sameGenderDoublesOnly: true,
    },
    isClubSession: true,
  });

  assert.equal(Object.hasOwn(payload, 'sameGenderDoublesOnly'), false);
});

test('same gender doubles only is unavailable for included gender below four players', () => {
  assert.equal(isSameGenderDoublesOnlyUnavailable(8, 0), false);
  assert.equal(isSameGenderDoublesOnlyUnavailable(0, 8), false);
  assert.equal(isSameGenderDoublesOnlyUnavailable(8, 3), true);
  assert.equal(isSameGenderDoublesOnlyUnavailable(3, 8), true);
  assert.equal(isSameGenderDoublesOnlyUnavailable(4, 4), false);
});

test('same gender doubles only detects impossible game count allocation', () => {
  assert.equal(canAllocateSameGenderDoublesOnlyGames(5, 5, 5), false);
  assert.equal(
    getSameGenderDoublesOnlyUnavailableReason({
      ...INITIAL_COMPETITION_FORM,
      maleCount: 5,
      femaleCount: 5,
      totalGames: 5,
    }),
    '남복/여복만으로는 현재 인원과 총 경기 수를 공정하게 배분할 수 없어요.'
  );
});

test('same gender doubles only allows odd player count when allocation is possible', () => {
  assert.equal(canAllocateSameGenderDoublesOnlyGames(6, 5, 11), true);
  assert.equal(
    getSameGenderDoublesOnlyUnavailableReason({
      ...INITIAL_COMPETITION_FORM,
      maleCount: 6,
      femaleCount: 5,
      totalGames: 11,
    }),
    ''
  );
});
