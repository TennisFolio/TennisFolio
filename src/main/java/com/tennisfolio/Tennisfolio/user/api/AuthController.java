package com.tennisfolio.Tennisfolio.user.api;

import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.security.oauth.service.OAuthUnlinkService;
import com.tennisfolio.Tennisfolio.security.oauth.service.ReIssueService;
import com.tennisfolio.Tennisfolio.security.oauth.service.RefreshTokenService;
import io.lettuce.core.dynamic.annotation.Param;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.tennisfolio.Tennisfolio.util.CookieUtils.deleteCookie;
import static com.tennisfolio.Tennisfolio.util.CookieUtils.getCookie;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final OAuthUnlinkService oAuthUnlinkService;
    private final RefreshTokenService refreshTokenService;
    private final ReIssueService reIssueService;

    public AuthController(OAuthUnlinkService oAuthUnlinkService, RefreshTokenService refreshTokenService, ReIssueService reIssueService) {
        this.oAuthUnlinkService = oAuthUnlinkService;
        this.refreshTokenService = refreshTokenService;
        this.reIssueService = reIssueService;
    }

    @PostMapping("/unlink/kakao/{userId}")
    public ResponseEntity<ResponseDTO> unlinkKakao(@PathVariable("userId") Long userId){
        oAuthUnlinkService.unlinkKakao(userId);
        return new ResponseEntity<>(ResponseDTO.success(), HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ){
        Long userId = (Long)authentication.getPrincipal();
        System.out.println("Logging out user with ID: " + userId);

        String sessionId = getCookie(request, "session_id");
        System.out.println("Session ID from cookie: " + sessionId);

        refreshTokenService.delete(userId, sessionId);

        deleteCookie(response, "refresh_token");
        deleteCookie(response, "session_id");

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/reIssue")
    public ResponseEntity<Void> reIssue(
            HttpServletRequest request,
            HttpServletResponse response
    ) {

        String sessionId = getCookie(request, "session_id");
        String refreshToken = getCookie(request, "refresh_token");
        String newAccessToken = reIssueService.reIssue(refreshToken, sessionId);

        response.setHeader("Authorization", "Bearer " + newAccessToken);

        return ResponseEntity.noContent().build();
    }




}
