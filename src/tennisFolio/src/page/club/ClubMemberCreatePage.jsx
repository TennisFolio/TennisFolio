import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import ClubMemberEditorView from '../../components/club/ClubMemberEditorView';
import { addClubMember, getClub } from '../../utils/clubApi';
import ClubAuthRequired from './ClubAuthRequired';
import {
  emptyMemberForm,
  errorMessage,
  memberPayload,
  normalizeClub,
  unwrapData,
} from './clubPageUtils';
import '../Club.css';

function ClubMemberCreatePage({ currentUser }) {
  const navigate = useNavigate();
  const { publicId } = useParams();
  const [selectedClub, setSelectedClub] = useState(null);
  const [memberForm, setMemberForm] = useState(emptyMemberForm);
  const [notice, setNotice] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [isSaving, setIsSaving] = useState(false);

  const isAdmin = selectedClub?.admin === true || selectedClub?.role === 'ADMIN';

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

  const handleAddMember = async (event) => {
    event.preventDefault();

    if (!selectedClub) {
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
      await addClubMember(selectedClub.publicId, memberPayload(memberForm, name));
      setMemberForm(emptyMemberForm);
      navigate(`/clubs/${selectedClub.publicId}`, { state: { activeTab: 'members' } });
    } catch (requestError) {
      setError(errorMessage(requestError, '클럽원을 추가하지 못했습니다.'));
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
        editingMember={null}
        isAdmin={isAdmin}
        isEdit={false}
        isLoading={isLoading}
        isMembersLoading={false}
        isSaving={isSaving}
        onSubmit={handleAddMember}
        onChange={setMemberForm}
        onBack={() =>
          navigate(selectedClub ? `/clubs/${selectedClub.publicId}` : '/clubs', {
            state: { activeTab: 'members' },
          })
        }
        onDelete={() => {}}
        onBackToClubs={() => navigate('/clubs')}
      />
    </main>
  );
}

export default ClubMemberCreatePage;
