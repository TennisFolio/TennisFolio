import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const pageSource = readFileSync(new URL('./Meetings.jsx', import.meta.url), 'utf8');
const pageCss = readFileSync(new URL('./Meeting.css', import.meta.url), 'utf8');

test('meetings list card includes mockup-style meta and attendance summary', () => {
  assert.match(pageSource, /meeting-card-meta/);
  assert.match(pageSource, /courtCount/);
  assert.match(pageSource, /totalGames/);
  assert.match(pageSource, /attendingCount/);
  assert.match(pageSource, /maybeCount/);
  assert.match(pageSource, /notAttendingCount/);
});

test('meetings list card keeps delete in title row and manage/share actions together', () => {
  assert.match(pageSource, /meeting-card-title-row/);
  assert.match(pageSource, /meeting-card-actions/);
  assert.match(pageSource, /meeting-button danger small/);
});

test('meeting card styles support compact mockup buttons and meta chips', () => {
  assert.match(pageCss, /\.meeting-card-meta\s*\{/);
  assert.match(pageCss, /\.meeting-card-actions\s*\{/);
  assert.match(pageCss, /\.meeting-button\.small\s*\{/);
});

test('meetings list uses a single auto-dismiss toast for action feedback', () => {
  assert.match(pageSource, /MeetingToast/);
  assert.match(pageSource, /const \[notice, setNotice\]/);
  assert.match(pageSource, /showNotice\('success', '공유 링크를 복사했습니다\.'\)/);
  assert.match(pageSource, /showNotice\('success', '모임을 삭제했습니다\.'\)/);
  assert.match(pageSource, /<MeetingToast notice=\{notice\} onClose=\{\(\) => setNotice\(null\)\} \/>/);
  assert.doesNotMatch(pageSource, /meeting-success/);
});
