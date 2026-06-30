import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const pageSource = readFileSync(new URL('./MeetingPublic.jsx', import.meta.url), 'utf8');
const pageCss = readFileSync(new URL('./Meeting.css', import.meta.url), 'utf8');

test('meeting public page follows mockup detail layout', () => {
  assert.match(pageSource, /meeting-card-title-row/);
  assert.match(pageSource, /meeting-card-meta/);
  assert.match(pageSource, /meeting-note-box/);
  assert.match(pageSource, /meeting-state-note/);
  assert.match(pageSource, /handleCopyShareLink/);
});

test('meeting public page shows entry screen before a participant enters', () => {
  assert.match(pageSource, /hasEntered/);
  assert.match(pageSource, /renderEntryScreen/);
  assert.match(pageSource, /처음 입장/);
  assert.match(pageSource, /이미 응답했다면 아래 이름을 눌러 다시 입장하세요/);
  assert.match(pageSource, /참석 여부를 선택하면 참석 현황과 명단을 볼 수 있습니다/);
  assert.doesNotMatch(pageSource, /같은 공개 URL/);
});

test('meeting public page switches to detail after save or existing name selection', () => {
  assert.match(pageSource, /setHasEntered\(true\)/);
  assert.match(pageSource, /selectAttendance/);
  assert.match(pageSource, /saveAttendance/);
});

test('meeting public page keeps current attendance id after creating or entering detail', () => {
  assert.match(pageSource, /const savedAttendance = response\.data\.data/);
  assert.match(pageSource, /attendanceId: savedAttendance\?\.id \?\?/);
  assert.match(pageSource, /participantName: savedAttendance\?\.participantName \?\?/);
  assert.match(pageSource, /attendanceId: nextForm\.attendanceId \|\| null/);
});

test('meeting public page saves profile separately and status buttons update immediately', () => {
  assert.match(pageSource, /handleSaveProfile/);
  assert.match(pageSource, /handleStatusClick/);
  assert.match(pageSource, /statusLabels/);
  assert.match(pageSource, /onClick=\{\(\) => handleStatusClick\(status\)\}/);
});

test('meeting public page groups roster by attending gender and status', () => {
  assert.match(pageSource, /groupAttendances/);
  assert.match(pageSource, /attendingMale/);
  assert.match(pageSource, /attendingFemale/);
  assert.match(pageSource, /maybe/);
  assert.match(pageSource, /notAttending/);
  assert.match(pageSource, /RosterPanel/);
});

test('meeting public detail roster is read-only for non-owner visitors', () => {
  const rosterPanelStart = pageSource.indexOf('function RosterPanel');
  const meetingPublicStart = pageSource.indexOf('function MeetingPublic');
  const rosterPanelSource = pageSource.slice(rosterPanelStart, meetingPublicStart);

  assert.doesNotMatch(rosterPanelSource, /onSelect/);
  assert.doesNotMatch(rosterPanelSource, /<button/);
  assert.match(rosterPanelSource, /<span/);
});

test('meeting public styles support same compact roster panels as manage screen', () => {
  assert.match(pageCss, /\.meeting-roster-panel\s*\{/);
  assert.match(pageCss, /\.meeting-status-options\s*\{/);
  assert.match(pageCss, /\.meeting-page\s*\{[^}]*width:\s*min\(520px,\s*100%\);/s);
});
