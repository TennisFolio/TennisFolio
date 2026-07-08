import ClubForm from './ClubForm';
import ClubMessage from './ClubMessage';
import ClubState from './ClubState';

function ClubEditView({
  club,
  form,
  notice,
  error,
  isAdmin,
  isLoading,
  isSaving,
  onSubmit,
  onChange,
  onBack,
  onDelete,
  onBackToClubs,
}) {
  return (
    <>
      {isLoading && <p className="club-notice static">클럽 정보를 불러오는 중입니다.</p>}
      {!isLoading && !club && (
        <ClubState eyebrow="CLUB" title="클럽을 찾을 수 없습니다">
          <ClubMessage notice={notice} error={error} />
          <button className="club-button full" type="button" onClick={onBackToClubs}>
            클럽 목록
          </button>
        </ClubState>
      )}
      {club && !isAdmin && (
        <ClubState
          eyebrow={club.name}
          title="관리자만 사용할 수 있습니다"
          description="클럽 정보 수정은 클럽 관리자에게만 열려 있습니다."
          actionLabel="클럽 상세"
          onAction={onBack}
        />
      )}
      {club && isAdmin && (
        <>
          <header className="club-page-head">
            <span className="club-eyebrow">클럽 정보 수정</span>
            <h1>{club.name}</h1>
            <p>클럽명과 소개를 수정합니다.</p>
          </header>
          <ClubMessage notice={notice} error={error} />
          <ClubForm
            form={form}
            submitLabel="저장"
            isSaving={isSaving}
            onSubmit={onSubmit}
            onChange={onChange}
            onBack={onBack}
            onDelete={onDelete}
          />
        </>
      )}
    </>
  );
}

export default ClubEditView;
