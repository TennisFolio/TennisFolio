import assert from 'node:assert/strict';
import test from 'node:test';

import { formatRoundSummary } from './competitionDetailSummaryLabels.js';

test('formats fixed schedule rounds without estimated hours', () => {
  assert.equal(formatRoundSummary(8), '8R');
  assert.equal(formatRoundSummary(0), '0R');
});
