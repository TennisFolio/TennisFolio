import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import ClubMemberEditorView from '../../components/club/ClubMemberEditorView';
import {
  deleteClubMember,
  getClub,
  getClubMembers,
  updateClubMember,
} from '../../utils/clubApi';
import ClubAuthRequired from './ClubAuthRequired';
import {
  emptyMemberForm,
  errorMessage,
  memberPayload,
  normalizeClub,
  unwrapData,
} from './clubPageUtils';
import '../Club.css';

function ClubMemberEditPage({ currentUser }) {
  const navigate = useNavigate();
  const { publicId, memberId } = useParams();
  const [selectedClub, setSelectedClub] = useState(null);
  const [members, setMembers] = useState([]);
  const [memberForm, setMemberForm] = useState(emptyMemberForm);
  const [memberFormSourceId, setMemberFormSourceId] = useState(null);
  const [notice, setNotice] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isMembersLoading, setIsMembersLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  const isAdmin = selectedClub?.admin === true || selectedClub?.role === 'ADMIN';
  const editingMember = useMemo(
    () => members.find((member) => String(member.id) === String(memberId)) ?? null,
    [memberId, members],
  );

  const showNotice = (message) => {
    setNotice(message);
    window.setTimeout(() => setNotice(''), 2200);
  };

  useEffect(() => {
    if (!currentUser || !publicId) {
      return;
    }

    let cancelled = false;
    setIsLoading(true);
    setError('');

    getClub(publicId)
      .then((response) => {
        if (!cancelled) {
          setSelectedClub(normalizeClub(unwrapData(response, null)));
        }
      })
      .catch((requestError) => {
        if (!cancelled) {
          setError(errorMessage(requestError, '클럽 정보를 불러오지 못했습니다.'));
        }
      })
      .finally(() => {
        if (!cancelled) {
          setIsLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [currentUser, publicId]);

  useEffect(() => {
    if (!currentUser || !publicId) {
      return;
    }

    let cancelled = false;
    setIsMembersLoading(true);

    getClubMembers(publicId, '')
      .then((response) => {
        if (!cancelled) {
          setMembers(unwrapData(response, []));
        }
      })
      .catch((requestError) => {
        if (!cancelled) {
          setError(errorMessage(requestError, '클럽원 목록을 불러오지 못했습니다.'));
        }
      })
      .finally(() => {
        if (!cancelled) {
          setIsMembersLoading(false);
        }
      });

    return () => {
      cancelled = true;
    };
  }, [currentUser, publicId]);

  useEffect(() => {
    if (!editingMember || memberFormSourceId === editingMember.id) {
      return;
    }

    setMemberForm({
      name: editingMember.name ?? '',
      gender: editingMember.gender ?? 'MALE',
      role: editingMember.role ?? 'MEMBER',
      skillNote: editingMember.skillNote ?? '',
      contactMemo: editingMember.contactMemo ?? '',
      memo: editingMember.memo ?? '',
    });
    setMemberFormSourceId(editingMember.id);
  }, [editingMember, memberFormSourceId]);

  const backToMembers = () => {
    setMemberForm(emptyMemberForm);
    setMemberFormSourceId(null);
    navigate(selectedClub ? `/clubs/${selectedClub.publicId}` : '/clubs', {
      state: { activeTab: 'members' },
    });
  };

  const handleUpdateMember = async (event) => {
    event.preventDefault();

    if (!selectedClub || !memberId) {
      return;
    }

    const name = memberForm.name.trim();
    if (!name) {
      showNotice('클럽원 이름을 입력해 주세요.');
      return;
    }

    setIsSaving(true);
    setError('');

    try {
      await updateClubMember(
        selectedClub.publicId,
        memberId,
        memberPayload(memberForm, name),
      );
      backToMembers();
    } catch (requestError) {
      setError(errorMessage(requestError, '클럽원 정보를 수정하지 못했습니다.'));
    } finally {
      setIsSaving(false);
    }
  };

  const handleRemoveMember = async () => {
    if (!selectedClub || !editingMember || !window.confirm(`${editingMember.name} 님을 삭제할까요?`)) {
      return;
    }

    setIsSaving(true);
    setError('');

    try {
      await deleteClubMember(selectedClub.publicId, editingMember.id);
      backToMembers();
    } catch (requestError) {
      setError(errorMessage(requestError, '클럽원을 삭제하지 못했습니다.'));
    } finally {
      setIsSaving(false);
    }
  };

  if (!currentUser) {
    return <ClubAuthRequired />;
  }

  return (
    <main className="club-mobile-page">
      <ClubMemberEditorView
        club={selectedClub}
        form={memberForm}
        notice={notice}
        error={error}
        editingMember={editingMember}
        isAdmin={isAdmin}
        isEdit
        isLoading={isLoading}
        isMembersLoading={isMembersLoading}
        isSaving={isSaving}
        onSubmit={handleUpdateMember}
        onChange={setMemberForm}
        onBack={backToMembers}
        onDelete={handleRemoveMember}
        onBackToClubs={() => navigate('/clubs')}
      />
    </main>
  );
}

export default ClubMemberEditPage;
