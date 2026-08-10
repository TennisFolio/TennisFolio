import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import ClubMemberBulkAddView from '../../components/club/bulk/ClubMemberBulkAddView';
import { bulkMemberPayload, createBulkMemberRow, createBulkMemberRows, parseBulkMemberRows, validateBulkMembers } from '../../components/club/bulk/bulkMemberUtils';
import ClubMessage from '../../components/club/ClubMessage';
import ClubState from '../../components/club/ClubState';
import { addClubMembers, getClub, getClubMembers } from '../../utils/clubApi';
import ClubAuthRequired from './ClubAuthRequired';
import { errorMessage, normalizeClub, unwrapData } from './clubPageUtils';
import '../Club.css';

function ClubMemberBulkAddPage({ currentUser }) {
  const navigate = useNavigate();
  const { publicId } = useParams();
  const [club, setClub] = useState(null);
  const [existingMembers, setExistingMembers] = useState([]);
  const [rows, setRows] = useState(() => createBulkMemberRows());
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);
  const isAdmin = club?.admin === true || club?.role === 'ADMIN';
  const validationRows = useMemo(() => validateBulkMembers(rows, existingMembers), [rows, existingMembers]);
  const backToMembers = () => navigate(club ? `/clubs/${club.publicId}` : '/clubs', { state: { activeTab: 'members' } });

  useEffect(() => {
    if (!currentUser || !publicId) return undefined;
    let cancelled = false;
    setIsLoading(true);
    setError('');
    Promise.all([getClub(publicId), getClubMembers(publicId)])
      .then(([clubResponse, memberResponse]) => {
        if (!cancelled) {
          setClub(normalizeClub(unwrapData(clubResponse, null)));
          setExistingMembers(unwrapData(memberResponse, []));
        }
      })
      .catch((requestError) => !cancelled && setError(errorMessage(requestError, '클럽 정보를 불러오지 못했습니다.')))
      .finally(() => !cancelled && setIsLoading(false));
    return () => { cancelled = true; };
  }, [currentUser, publicId]);

  const changeRow = (rowId, changes) => setRows((currentRows) => currentRows.map((row) => row.rowId === rowId ? { ...row, ...changes } : row));
  const handlePaste = (event) => {
    const text = event.clipboardData?.getData('text');
    if (!text) return;
    event.preventDefault();
    setRows((currentRows) => [...currentRows.filter((row) => row.name.trim()), ...parseBulkMemberRows(text, club?.skillTiers ?? [])]);
  };
  const handleSubmit = async () => {
    const members = validationRows.filter((row) => row.name);
    if (!club || members.some((row) => row.errors.length)) return;
    setIsSaving(true);
    setError('');
    try {
      await addClubMembers(club.publicId, bulkMemberPayload(members));
      backToMembers();
    } catch (requestError) {
      if (requestError?.response?.status === 409) {
        try {
          const response = await getClubMembers(club.publicId);
          setExistingMembers(unwrapData(response, []));
          setError('등록 중 같은 이름의 클럽원이 추가되었습니다. 표시된 행을 수정해 주세요.');
        } catch {
          setError(errorMessage(requestError, '클럽원을 일괄 등록하지 못했습니다.'));
        }
      } else {
        setError(errorMessage(requestError, '클럽원을 일괄 등록하지 못했습니다.'));
      }
    } finally {
      setIsSaving(false);
    }
  };

  if (!currentUser) return <ClubAuthRequired />;
  if (isLoading) return <main className="club-bulk-page"><p className="club-notice static">클럽 정보를 불러오는 중입니다.</p></main>;
  if (!club) return <main className="club-bulk-page"><ClubState eyebrow="CLUB MEMBER" title="클럽을 찾을 수 없습니다"><ClubMessage error={error} /><button className="club-button full" type="button" onClick={() => navigate('/clubs')}>클럽 목록</button></ClubState></main>;
  if (!isAdmin) return <main className="club-bulk-page"><ClubState eyebrow={club.name} title="관리자만 사용할 수 있습니다" description="클럽원 추가는 클럽 관리자에게만 열려 있습니다." actionLabel="클럽원 목록" onAction={backToMembers} /></main>;

  return (
    <main className="club-bulk-page">
      <ClubMessage error={error} />
      <ClubMemberBulkAddView
        rows={rows} skillTiers={club.skillTiers ?? []} validationRows={validationRows} isSaving={isSaving}
        onChangeRow={changeRow} onAddRow={() => setRows((currentRows) => [...currentRows, createBulkMemberRow()])}
        onRemoveRow={(rowId) => setRows((currentRows) => currentRows.filter((row) => row.rowId !== rowId))}
        onPaste={handlePaste}
        onSubmit={handleSubmit} onBack={backToMembers}
      />
    </main>
  );
}

export default ClubMemberBulkAddPage;
