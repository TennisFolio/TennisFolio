import assert from 'node:assert/strict';
import { readFileSync } from 'node:fs';
import test from 'node:test';

const pageSource = readFileSync(new URL('./MeetingManage.jsx', import.meta.url), 'utf8');
const pageCss = readFileSync(new URL('./Meeting.css', import.meta.url), 'utf8');

test('meeting manage page follows mockup dashboard panels', () => {
  assert.match(pageSource, /meeting-manage-grid/);
  assert.match(pageSource, /meeting-note-box/);
  assert.match(pageSource, /내 정보/);
  assert.match(pageSource, /운영 관리/);
  assert.match(pageSource, /참석자로 경기 생성/);
  assert.match(pageSource, /공유 링크 복사/);
});

test('meeting manage page does not render extra note or operation memo copy', () => {
  assert.doesNotMatch(pageSource, /note: \{meeting\.note\}/);
  assert.doesNotMatch(pageSource, /수정은/);
  assert.doesNotMatch(pageSource, /운영 메모/);
  assert.doesNotMatch(pageSource, /정원이 부족하면/);
});

test('meeting manage page groups attendees by status and gender with owner remove controls', () => {
  assert.match(pageSource, /남자 참석자/);
  assert.match(pageSource, /여자 참석자/);
  assert.match(pageSource, /미정/);
  assert.match(pageSource, /불참/);
  assert.match(pageSource, /attendeeToDelete/);
  assert.match(pageSource, /meeting-attendee-remove/);
});

test('meeting manage owner info uses current user nickname and status clicks save immediately', () => {
  assert.match(pageSource, /getCurrentUser/);
  assert.match(pageSource, /currentUser\?\.nickName/);
  assert.match(pageSource, /readOnly/);
  assert.match(pageSource, /findOwnerAttendance/);
  assert.match(pageSource, /attendanceId: ownerAttendance\?\.id/);
  assert.match(pageSource, /onClick=\{\(\) => handleOwnerAttendance\(status\)\}/);
});

test('meeting manage styles support mockup roster and confirmation panels', () => {
  assert.match(pageCss, /\.meeting-manage-grid\s*\{/);
  assert.match(pageCss, /\.meeting-roster-panel\s*\{/);
  assert.match(pageCss, /\.meeting-confirm-panel\s*\{/);
  assert.match(pageCss, /\.meeting-attendee-remove\s*\{/);
});

test('meeting manage layout stays single column on desktop', () => {
  assert.match(
    pageCss,
    /\.meeting-manage-grid\s*\{[^}]*grid-template-columns:\s*1fr;/s,
  );
  assert.doesNotMatch(
    pageCss,
    /\.meeting-manage-grid\s*\{[^}]*grid-template-columns:\s*repeat\(2/s,
  );
});

test('meeting manage page keeps the same width as mobile meeting pages', () => {
  assert.match(pageCss, /\.meeting-page\s*\{[^}]*width:\s*min\(520px,\s*100%\);/s);
  assert.doesNotMatch(pageCss, /\.meeting-page\.manage\s*\{[^}]*width:/s);
});

test('meeting manage actions use a natural title and keep feedback inside that panel', () => {
  assert.doesNotMatch(pageSource, /운영 액션/);
  assert.match(pageSource, /운영 관리/);

  const actionTitleIndex = pageSource.indexOf('운영 관리');
  const nextRosterIndex = pageSource.indexOf('<RosterPanel', actionTitleIndex);
  const actionPanelSource = pageSource.slice(actionTitleIndex, nextRosterIndex);
  const afterActionPanelSource = pageSource.slice(nextRosterIndex);

  assert.match(actionPanelSource, /handleCopyShareLink/);
  assert.match(actionPanelSource, /meeting-feedback-stack/);
  assert.doesNotMatch(afterActionPanelSource, /meeting-feedback-stack/);
});
