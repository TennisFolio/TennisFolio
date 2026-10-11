package com.tennisfolio.Tennisfolio.club.repository;

import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccount;
import com.tennisfolio.Tennisfolio.club.entity.ClubAccountRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClubAccountRepository extends JpaRepository<ClubAccount, Long> {

    Optional<ClubAccount> findByClubAndAccountId(Club club, Long accountId);

    Optional<ClubAccount> findByClubIdAndAccountId(Long clubId, Long accountId);

    Optional<ClubAccount> findByIdAndClub(Long id, Club club);

    List<ClubAccount> findByClubOrderByIdAsc(Club club);

    List<ClubAccount> findByAccountIdOrderByIdAsc(Long accountId);

    long countByClubAndRole(Club club, ClubAccountRole role);
}
