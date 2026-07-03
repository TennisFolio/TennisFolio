import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const toastSource = readFileSync(new URL('./MeetingToast.jsx', import.meta.url), 'utf8');

test('meeting toast shows one fixed alert and dismisses after three seconds', () => {
  assert.match(toastSource, /function MeetingToast\(\{ notice, onClose \}\)/);
  assert.match(toastSource, /setTimeout\(onClose, 3000\)/);
  assert.match(toastSource, /clearTimeout/);
  assert.match(toastSource, /role="status"/);
  assert.match(toastSource, /aria-live="polite"/);
  assert.match(toastSource, /meeting-toast/);
  assert.match(toastSource, /알림 닫기/);
});
