import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import ClubEditView from '../../components/club/ClubEditView';
import { deleteClub, getClub, updateClub } from '../../utils/clubApi';
import ClubAuthRequired from './ClubAuthRequired';
import {
  emptyClubForm,
  errorMessage,
  normalizeClub,
  unwrapData,
} from './clubPageUtils';
import '../Club.css';

function ClubEditPage({ currentUser }) {
  const navigate = useNavigate();
  const { publicId } = useParams();
  const [selectedClub, setSelectedClub] = useState(null);
  const [clubForm, setClubForm] = useState(emptyClubForm);
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
    setSelectedClub(null);

    getClub(publicId)
      .then((response) => {
        if (!cancelled) {
          const club = normalizeClub(unwrapData(response, null));
          setSelectedClub(club);
          setClubForm({
            name: club?.name ?? '',
            description: club?.description ?? '',
          });
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

  const handleUpdateClub = async (event) => {
    event.preventDefault();

    if (!selectedClub) {
      return;
    }

    const name = clubForm.name.trim();
    if (!name) {
      showNotice('클럽명을 입력해 주세요.');
      return;
    }

    setIsSaving(true);
    setError('');

    try {
      await updateClub(selectedClub.publicId, {
        name,
        description: clubForm.description.trim(),
      });
      navigate(`/clubs/${selectedClub.publicId}`);
    } catch (requestError) {
      setError(errorMessage(requestError, '클럽 정보를 수정하지 못했습니다.'));
    } finally {
      setIsSaving(false);
    }
  };

  const handleDeleteClub = async () => {
    if (!selectedClub || !window.confirm('클럽을 삭제할까요?')) {
      return;
    }

    setIsSaving(true);
    setError('');

    try {
      await deleteClub(selectedClub.publicId);
      navigate('/clubs');
    } catch (requestError) {
      setError(errorMessage(requestError, '클럽을 삭제하지 못했습니다.'));
    } finally {
      setIsSaving(false);
    }
  };

  if (!currentUser) {
    return <ClubAuthRequired />;
  }

  return (
    <main className="club-mobile-page">
      <ClubEditView
        club={selectedClub}
        form={clubForm}
        notice={notice}
        error={error}
        isAdmin={isAdmin}
        isLoading={isLoading}
        isSaving={isSaving}
        onSubmit={handleUpdateClub}
        onChange={setClubForm}
        onBack={() => navigate(selectedClub ? `/clubs/${selectedClub.publicId}` : '/clubs')}
        onDelete={handleDeleteClub}
        onBackToClubs={() => navigate('/clubs')}
      />
    </main>
  );
}

export default ClubEditPage;
