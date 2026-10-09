package com.tennisfolio.Tennisfolio.club.repository;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccountInvitation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClubAccountInvitationRepository extends JpaRepository<ClubAccountInvitation, Long> {
    Optional<ClubAccountInvitation> findByClub(Club club);

    Optional<ClubAccountInvitation> findByTokenId(String tokenId);
}
