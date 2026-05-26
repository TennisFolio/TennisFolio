package com.tennisfolio.Tennisfolio.matching.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.server.ResponseStatusException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CompetitionAdminTokenServiceTest {

    private CompetitionAdminTokenService service;

    @BeforeEach
    void setUp() {
        service = new CompetitionAdminTokenService(
                "dGVubmlzZm9saW90ZW5uaXNmb2xpb3Rlbm5pc2ZvbGlvdGVubmlzZm9saW8="
        );
        service.init();
    }

    @Test
    void createAndValidate_returnsPublicIdForCompetitionAdminToken() {
        String token = service.createToken("public-id");

        assertEquals("public-id", service.validateAndGetPublicId(token));
    }

    @Test
    void validateAndGetPublicId_rejectsInvalidToken() {
        assertThrows(ResponseStatusException.class, () -> service.validateAndGetPublicId("bad-token"));
    }
}
