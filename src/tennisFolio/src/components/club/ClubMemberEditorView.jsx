import ClubMemberForm from './ClubMemberForm';
import ClubMessage from './ClubMessage';
import ClubState from './ClubState';

function ClubMemberEditorView({
  club,
  form,
  notice,
  error,
  editingMember,
  isAdmin,
  isEdit,
  isLoading,
  isMembersLoading,
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
        <ClubState eyebrow="CLUB MEMBER" title="클럽을 찾을 수 없습니다">
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
          description="클럽원 추가와 수정은 클럽 관리자에게만 열려 있습니다."
          actionLabel="클럽원 목록"
          onAction={onBack}
        />
      )}
      {club && isAdmin && (
        <>
          <header className="club-page-head">
            <span className="club-eyebrow">{club.name}</span>
            <h1>{isEdit ? '클럽원 수정' : '클럽원 추가'}</h1>
            <p>
              {isEdit
                ? '클럽원 이름, 성별, 역할과 운영 메모를 수정합니다.'
                : '관리자가 직접 운영 명단을 추가합니다. userId는 없어도 됩니다.'}
            </p>
          </header>
          <ClubMessage notice={notice} error={error} />
          {isEdit && isMembersLoading && (
            <p className="club-notice static">클럽원 정보를 불러오는 중입니다.</p>
          )}
          {isEdit && !isMembersLoading && !editingMember && (
            <ClubState
              eyebrow="MEMBER"
              title="클럽원을 찾을 수 없습니다"
              actionLabel="클럽원 목록"
              onAction={onBack}
            />
          )}
          {(!isEdit || editingMember) && (
            <>
              <ClubMemberForm
                form={form}
                isEdit={isEdit}
                isSaving={isSaving}
                showDelete={isEdit && Boolean(editingMember)}
                onSubmit={onSubmit}
                onChange={onChange}
                onBack={onBack}
                onDelete={onDelete}
              />
              <div className="club-notice static">
                같은 클럽의 활성 클럽원 이름은 중복 저장하지 않습니다.
              </div>
            </>
          )}
        </>
      )}
    </>
  );
}

export default ClubMemberEditorView;
