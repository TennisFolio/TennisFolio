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
  assert.match(pageSource, /참석자로 대진표 생성/);
  assert.match(pageSource, /공유 링크 복사/);
});

test('meeting manage page removes redundant dashboard and empty helper copy', () => {
  assert.doesNotMatch(pageSource, /관리자 대시보드/);
  assert.doesNotMatch(pageSource, /아직 표시할 참석자가 없습니다/);
  assert.doesNotMatch(pageSource, /참석 명단 확인 영역 아래에 분리해 둡니다/);
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
  assert.match(pageCss, /\.meeting-confirm-backdrop\s*\{/);
  assert.match(pageCss, /\.meeting-confirm-panel\s*\{/);
  assert.match(pageCss, /\.meeting-attendee-remove\s*\{/);
  assert.match(pageCss, /\.meeting-toast\s*\{/);
  assert.match(pageCss, /position:\s*fixed;/);
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

test('meeting manage actions use a natural title and one auto-dismiss toast', () => {
  assert.doesNotMatch(pageSource, /운영 액션/);
  assert.match(pageSource, /운영 관리/);
  assert.match(pageSource, /MeetingToast/);
  assert.match(pageSource, /const \[notice, setNotice\]/);
  assert.match(pageSource, /showNotice\('success', '공유 링크를 복사했습니다\.'\)/);
  assert.match(pageSource, /showNotice\('success', status === 'OPEN'/);
  assert.match(pageSource, /<MeetingToast notice=\{notice\} onClose=\{\(\) => setNotice\(null\)\} \/>/);
  assert.doesNotMatch(pageSource, /meeting-feedback-stack/);
});

test('meeting manage operations separate primary schedule actions from dangerous meeting deletion', () => {
  assert.match(pageSource, /meeting-operation-primary/);
  assert.match(pageSource, /대진표 생성/);
  assert.match(pageSource, /참석자로 대진표 생성/);
  assert.match(pageSource, /대진표 보기/);
  assert.match(pageSource, /meeting-operation-tools/);
  assert.match(pageSource, /meeting-operation-status/);
  assert.match(pageSource, /meeting-danger-zone/);
  assert.doesNotMatch(pageSource, /className="meeting-button danger small"[\s\S]*삭제/);
  assert.match(
    pageSource,
    /title="불참"[\s\S]*<section className="meeting-panel meeting-danger-zone"/,
  );

  assert.match(pageCss, /\.meeting-operation-primary\s*\{/);
  assert.match(pageCss, /\.meeting-operation-tools\s*\{/);
  assert.match(pageCss, /\.meeting-operation-status\s*\{/);
  assert.match(pageCss, /\.meeting-danger-zone\s*\{/);
});

test('meeting manage operation panels use the mobile layout on desktop too', () => {
  assert.match(
    pageCss,
    /\.meeting-operation-head,\s*\.meeting-operation-status\s*\{[^}]*align-items:\s*stretch;[^}]*flex-direction:\s*column;/s,
  );
  assert.doesNotMatch(
    pageCss,
    /@media[\s\S]*\.meeting-operation-head,\s*\.meeting-operation-status,[\s\S]*flex-direction:\s*column;/,
  );
});

test('meeting manage operation attendance summary uses a compact chip', () => {
  assert.match(pageSource, /meeting-chip ok compact/);
  assert.match(pageCss, /\.meeting-chip\.compact\s*\{/);
  assert.match(
    pageCss,
    /\.meeting-chip\.compact\s*\{[^}]*width:\s*34px;[^}]*height:\s*34px;[^}]*border-radius:\s*999px;/s,
  );
});

test('meeting manage delete meeting panel keeps a white surface with only the button dangerous', () => {
  assert.match(pageSource, /className="meeting-panel meeting-danger-zone"/);
  assert.doesNotMatch(
    pageCss,
    /\.meeting-danger-zone\s*\{[^}]*background:\s*var\(--competition-danger-bg\);/s,
  );
  assert.match(
    pageCss,
    /\.meeting-danger-zone\s*\{[^}]*background:\s*var\(--competition-surface\);/s,
  );
});

test('meeting manage share and edit actions stay as half-width buttons on mobile too', () => {
  assert.match(
    pageCss,
    /\.meeting-operation-tools\s*\{[^}]*grid-template-columns:\s*repeat\(2,\s*minmax\(0,\s*1fr\)\);/s,
  );
  assert.doesNotMatch(
    pageCss,
    /@media[\s\S]*\.meeting-operation-tools\s*\{[^}]*grid-template-columns:\s*1fr;/,
  );
});

test('meeting manage links to generated competition when available', () => {
  assert.match(pageSource, /meeting\.competitionPublicId/);
  assert.match(pageSource, /navigate\(`\/competitions\/\$\{meeting\.competitionPublicId\}`\)/);
  assert.match(pageSource, /대진표 보기/);
});

test('meeting manage destructive actions use a centered modal confirmation', () => {
  assert.match(pageSource, /competitionDeleteRequested/);
  assert.match(pageSource, /setCompetitionDeleteRequested\(true\)/);
  assert.match(pageSource, /meeting-confirm-backdrop/);
  assert.match(pageSource, /role="alertdialog"/);
  assert.match(pageSource, /aria-modal="true"/);
  assert.match(pageSource, /선수를 삭제하시겠습니까\?/);
  assert.match(pageSource, /생성된 대진표를 삭제하시겠습니까\?/);
  assert.match(pageSource, /onCancel=\{\(\) => setCompetitionDeleteRequested\(false\)\}/);
  assert.match(pageSource, /onConfirm=\{handleDeleteCompetition\}/);

  assert.match(
    pageCss,
    /\.meeting-confirm-backdrop\s*\{[^}]*position:\s*fixed;[^}]*inset:\s*0;/s,
  );
  assert.match(
    pageCss,
    /\.meeting-confirm-backdrop\s*\{[^}]*background:\s*rgba\(15,\s*23,\s*42,\s*0\.48\);/s,
  );
  assert.match(
    pageCss,
    /\.meeting-confirm-panel\s*\{[^}]*width:\s*min\(420px,\s*calc\(100% - 32px\)\);/s,
  );
});
