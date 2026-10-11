import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import ExcelJS from 'exceljs';
import ClubMemberBulkAddView from '../../components/club/bulk/ClubMemberBulkAddView';
import { isClubOperator } from '../../components/club/clubUtils';
import { bulkMemberPayload } from '../../components/club/bulk/bulkMemberUtils';
import ClubDashboardLayout from '../../components/club/dashboard/ClubDashboardLayout';
import ClubAuthRequired from './ClubAuthRequired';
import { addClubMembers, getClub, getClubManagementMembers } from '../../utils/clubApi';
import { errorMessage, normalizeClub, unwrapData } from './clubPageUtils';
import '../Club.css';

function ClubMemberBulkAddPage({ currentUser }) {
  const navigate = useNavigate(); const { clubPublicId, publicId } = useParams(); const selectedPublicId = clubPublicId ?? publicId;
  const [club, setClub] = useState(null); const [existingMembers, setExistingMembers] = useState([]); const [error, setError] = useState(''); const [isLoading, setIsLoading] = useState(false); const [isSaving, setIsSaving] = useState(false);
  const isAdmin = isClubOperator(club); const backToMembers = () => navigate(`/clubs/${selectedPublicId}/dashboard/members`);
  useEffect(() => { if (!currentUser || !selectedPublicId) return undefined; let cancelled = false; setIsLoading(true); setError(''); Promise.all([getClub(selectedPublicId), getClubManagementMembers(selectedPublicId)]).then(([clubResponse, memberResponse]) => { if (!cancelled) { setClub(normalizeClub(unwrapData(clubResponse, null))); setExistingMembers(unwrapData(memberResponse, [])); } }).catch((requestError) => !cancelled && setError(errorMessage(requestError, '회원 정보를 불러오지 못했습니다.'))).finally(() => !cancelled && setIsLoading(false)); return () => { cancelled = true; }; }, [currentUser, selectedPublicId]);
  const downloadTemplate = async () => {
    const workbook = new ExcelJS.Workbook();
    const worksheet = workbook.addWorksheet('회원 등록');
    const tierNames = (club?.skillTiers ?? []).map((tier) => tier.name).filter(Boolean);

    worksheet.columns = [
      { header: '이름', key: 'name', width: 16 },
      { header: '성별', key: 'gender', width: 10 },
      { header: '티어', key: 'tier', width: 16 },
      { header: '가입일', key: 'joinedAt', width: 14 },
    ];
    worksheet.getRow(1).font = { bold: true };
    worksheet.addRow(['예시 회원', '남', tierNames[0] ?? '클럽 티어명', new Date(2025, 0, 15)]);

    for (let row = 2; row <= 101; row += 1) {
      worksheet.getCell(`B${row}`).dataValidation = {
        type: 'list',
        allowBlank: false,
        formulae: ['"남,여"'],
        showErrorMessage: true,
        errorTitle: '성별 선택',
        error: '성별은 남 또는 여만 선택할 수 있습니다.',
      };
      worksheet.getCell(`D${row}`).numFmt = 'yyyy-mm-dd';
    }

    const guideWorksheet = workbook.addWorksheet('작성 안내');
    guideWorksheet.columns = [{ width: 14 }, { width: 56 }];
    guideWorksheet.addRows([
      ['항목', '입력 방법'],
      ['성별', '드롭다운에서 남 또는 여를 선택'],
      ['티어', tierNames.length ? `현재 사용 가능한 티어: ${tierNames.join(', ')}` : '현재 설정된 티어가 없습니다. 빈칸으로 두세요.'],
      ['가입일', 'YYYY-MM-DD 형식, 예: 2026-09-01 (비워도 됨)'],
      ['예시 행', '두 번째 행은 예시이므로 삭제하거나 실제 회원 정보로 바꾼 뒤 사용'],
    ]);
    guideWorksheet.getRow(1).font = { bold: true };

    const fileData = await workbook.xlsx.writeBuffer();
    const fileUrl = URL.createObjectURL(new Blob([fileData], {
      type: 'application/vnd.openxmlformats-officedocument.spreadsheetml.sheet',
    }));
    const link = document.createElement('a');
    link.href = fileUrl;
    link.download = '클럽회원-일괄등록-양식.xlsx';
    link.click();
    URL.revokeObjectURL(fileUrl);
  };
  const handleSubmit = async (members) => { if (!club || !members.length) return; setIsSaving(true); setError(''); try { await addClubMembers(club.publicId, bulkMemberPayload(members)); backToMembers(); } catch (requestError) { const status = requestError?.response?.status; if (status === 409) { try { const response = await getClubManagementMembers(selectedPublicId); setExistingMembers(unwrapData(response, [])); setError('등록 중 같은 이름의 회원이 추가되었습니다. 최신 명단으로 충돌 행을 표시했으니 수정해 주세요.'); } catch { setError('동시 등록 충돌이 발생했습니다. 잠시 후 다시 시도해 주세요.'); } } else if (status === 400) { setError(errorMessage(requestError, '입력 형식을 다시 확인해 주세요. 등록된 회원은 없습니다.')); } else if (status === 401) { setError('로그인이 만료되었습니다. 다시 로그인해 주세요.'); } else if (status === 403) { setError('관리자만 회원을 일괄 등록할 수 있습니다.'); } else { setError('회원 일괄 등록에 실패했습니다. 등록된 회원은 없습니다.'); } } finally { setIsSaving(false); } };
  if (!currentUser) return <ClubAuthRequired />;
  return <ClubDashboardLayout clubPublicId={selectedPublicId} currentUser={currentUser} activeMenu="members">{isLoading && <p className="club-notice static">회원 정보를 불러오는 중입니다.</p>}{!isLoading && error && <p className="club-notice danger">{error}</p>}{!isLoading && !club && <button className="club-button" type="button" onClick={() => navigate('/clubs')}>클럽 목록</button>}{!isLoading && club && !isAdmin && <p className="club-notice danger">관리자만 사용할 수 있습니다.</p>}{!isLoading && club && isAdmin && <ClubMemberBulkAddView skillTiers={club.skillTiers ?? []} existingMembers={existingMembers} isSaving={isSaving} onDownloadTemplate={downloadTemplate} onSubmit={handleSubmit} onBack={backToMembers} />}</ClubDashboardLayout>;
}
export default ClubMemberBulkAddPage;
