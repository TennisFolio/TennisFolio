package com.tennisfolio.Tennisfolio.user.api;

import com.tennisfolio.Tennisfolio.common.response.ResponseDTO;
import com.tennisfolio.Tennisfolio.security.oauth.dto.ReissuedToken;
import com.tennisfolio.Tennisfolio.security.oauth.service.OAuthUnlinkService;
import com.tennisfolio.Tennisfolio.security.oauth.service.ReIssueService;
import com.tennisfolio.Tennisfolio.security.oauth.service.RefreshTokenService;
import com.tennisfolio.Tennisfolio.user.dto.AuthMeResponse;
import com.tennisfolio.Tennisfolio.user.service.AuthQueryService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import static com.tennisfolio.Tennisfolio.util.CookieUtils.createHttpOnlyCookie;
import static com.tennisfolio.Tennisfolio.util.CookieUtils.deleteCookie;
import static com.tennisfolio.Tennisfolio.util.CookieUtils.getCookie;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private static final int ACCESS_TOKEN_MAX_AGE_SECONDS = 60 * 30;
    private static final int REFRESH_TOKEN_MAX_AGE_SECONDS = 60 * 60 * 24 * 14;

    private final OAuthUnlinkService oAuthUnlinkService;
    private final RefreshTokenService refreshTokenService;
    private final ReIssueService reIssueService;
    private final AuthQueryService authQueryService;

    public AuthController(
            OAuthUnlinkService oAuthUnlinkService,
            RefreshTokenService refreshTokenService,
            ReIssueService reIssueService,
            AuthQueryService authQueryService
    ) {
        this.oAuthUnlinkService = oAuthUnlinkService;
        this.refreshTokenService = refreshTokenService;
        this.reIssueService = reIssueService;
        this.authQueryService = authQueryService;
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseDTO<AuthMeResponse>> me(Authentication authentication) {
        Long userId = (Long) authentication.getPrincipal();
        return ResponseEntity.ok(ResponseDTO.success(authQueryService.getCurrentUser(userId)));
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
        Long userId = (Long) authentication.getPrincipal();
        String sessionId = getCookie(request, "session_id");

        refreshTokenService.delete(userId, sessionId);

        deleteCookie(response, "access_token");
        deleteCookie(response, "refresh_token");
        deleteCookie(response, "session_id");

        return ResponseEntity.noContent().build();
    }

    @PostMapping({"/reissue", "/reIssue"})
    public ResponseEntity<Void> reIssue(
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        String sessionId = getCookie(request, "session_id");
        String refreshToken = getCookie(request, "refresh_token");
        ReissuedToken token = reIssueService.reIssue(refreshToken, sessionId);

        response.addCookie(createHttpOnlyCookie(
                "access_token",
                token.getAccessToken(),
                ACCESS_TOKEN_MAX_AGE_SECONDS
        ));
        response.addCookie(createHttpOnlyCookie(
                "refresh_token",
                token.getRefreshToken(),
                REFRESH_TOKEN_MAX_AGE_SECONDS
        ));

        return ResponseEntity.noContent().build();
    }
}
