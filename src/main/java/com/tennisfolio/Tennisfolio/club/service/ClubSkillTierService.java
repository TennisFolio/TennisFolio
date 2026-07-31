package com.tennisfolio.Tennisfolio.club.service;

import com.tennisfolio.Tennisfolio.club.dto.ClubSkillTierRequest;
import com.tennisfolio.Tennisfolio.club.entity.Club;
import com.tennisfolio.Tennisfolio.club.entity.ClubSkillTier;
import com.tennisfolio.Tennisfolio.club.repository.ClubMemberRepository;
import com.tennisfolio.Tennisfolio.club.repository.ClubSkillTierRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

@Service
public class ClubSkillTierService {

    private static final int MAX_SKILL_TIER_COUNT = 10;
    private static final int MAX_SKILL_TIER_NAME_LENGTH = 10;

    private final ClubSkillTierRepository clubSkillTierRepository;
    private final ClubMemberRepository clubMemberRepository;

    public ClubSkillTierService(
            ClubSkillTierRepository clubSkillTierRepository,
            ClubMemberRepository clubMemberRepository
    ) {
        this.clubSkillTierRepository = clubSkillTierRepository;
        this.clubMemberRepository = clubMemberRepository;
    }

    public void replaceSkillTiers(Club club, List<ClubSkillTierRequest> requestedSkillTiers) {
        List<ClubSkillTierRequest> skillTiers = normalizeRequests(requestedSkillTiers);
        validateSkillTiers(skillTiers);

        Map<Long, ClubSkillTier> existingById = findExistingById(club);
        if (!existingById.isEmpty()) {
            moveExistingLevelsToTemporaryRange(existingById.values(), skillTiers.size());
            clubSkillTierRepository.flush();
        }

        Set<Long> retainedIds = saveOrUpdateSkillTiers(club, skillTiers, existingById);
        removeDeletedSkillTiers(existingById, retainedIds);
    }

    private List<ClubSkillTierRequest> normalizeRequests(List<ClubSkillTierRequest> requestedSkillTiers) {
        return requestedSkillTiers == null ? List.of() : requestedSkillTiers;
    }

    private Map<Long, ClubSkillTier> findExistingById(Club club) {
        Map<Long, ClubSkillTier> existingById = new HashMap<>();
        for (ClubSkillTier skillTier : clubSkillTierRepository.findByClubOrderByLevelDescIdAsc(club)) {
            existingById.put(skillTier.getId(), skillTier);
        }
        return existingById;
    }

    private void moveExistingLevelsToTemporaryRange(
            Collection<ClubSkillTier> existingSkillTiers,
            int targetCount
    ) {
        int currentMaxLevel = existingSkillTiers.stream()
                .mapToInt(ClubSkillTier::getLevel)
                .max()
                .orElse(0);
        int temporaryOffset = currentMaxLevel + targetCount;

        existingSkillTiers.forEach(skillTier ->
                skillTier.moveLevel(skillTier.getLevel() + temporaryOffset)
        );
    }

    private Set<Long> saveOrUpdateSkillTiers(
            Club club,
            List<ClubSkillTierRequest> skillTiers,
            Map<Long, ClubSkillTier> existingById
    ) {
        Set<Long> retainedIds = new HashSet<>();
        for (int index = 0; index < skillTiers.size(); index++) {
            ClubSkillTierRequest request = skillTiers.get(index);
            int level = skillTiers.size() - index;
            String name = normalizeName(request.getName());
            if (request.getId() == null) {
                clubSkillTierRepository.save(new ClubSkillTier(club, name, level));
                continue;
            }

            ClubSkillTier skillTier = existingById.get(request.getId());
            if (skillTier == null || !retainedIds.add(skillTier.getId())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "클럽에 속하지 않은 등급입니다.");
            }
            skillTier.update(name, level);
        }
        return retainedIds;
    }

    private void removeDeletedSkillTiers(Map<Long, ClubSkillTier> existingById, Set<Long> retainedIds) {
        List<ClubSkillTier> deletedSkillTiers = existingById.values().stream()
                .filter(skillTier -> !retainedIds.contains(skillTier.getId()))
                .toList();
        for (ClubSkillTier skillTier : deletedSkillTiers) {
            clubMemberRepository.findBySkillTier(skillTier).forEach(member -> member.clearSkillTier());
        }
        clubSkillTierRepository.deleteAll(deletedSkillTiers);
    }

    private void validateSkillTiers(List<ClubSkillTierRequest> skillTiers) {
        if (skillTiers.size() > MAX_SKILL_TIER_COUNT) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "등급은 최대 10개까지 추가할 수 있습니다.");
        }

        Set<String> names = new HashSet<>();
        for (ClubSkillTierRequest skillTier : skillTiers) {
            if (skillTier == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "등급 정보가 올바르지 않습니다.");
            }
            String name = normalizeName(skillTier.getName());
            if (!names.add(name.toLowerCase(Locale.ROOT))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "등급 이름은 중복될 수 없습니다.");
            }
        }
    }

    private String normalizeName(String name) {
        if (name == null || name.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "등급 이름을 입력해주세요.");
        }
        String normalizedName = name.trim();
        if (normalizedName.length() > MAX_SKILL_TIER_NAME_LENGTH) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "등급 이름은 최대 10자입니다.");
        }
        return normalizedName;
    }
}
