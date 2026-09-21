package com.tennisfolio.Tennisfolio.club.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

@Getter
@RequiredArgsConstructor
public class ClubDashboardMemberPage {

    private final List<ClubDashboardMemberActivity> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
}
