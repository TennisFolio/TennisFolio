package com.tennisfolio.Tennisfolio.security.oauth.repository;

import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import com.tennisfolio.Tennisfolio.common.OAuthProvider;
import com.tennisfolio.Tennisfolio.common.OAuthStatus;
import com.tennisfolio.Tennisfolio.security.oauth.domain.OAuthAccount;
import com.tennisfolio.Tennisfolio.user.repository.UserEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_oauth_account",
uniqueConstraints = @UniqueConstraint(columnNames = {"PROVIDER", "PROVIDER_ID", "STATUS"}))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuthAccountEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="OAUTH_ID")
    private Long oAuthId;
    @ManyToOne(fetch= FetchType.LAZY)
    @JoinColumn(name = "USER_ID")
    private UserEntity user;
    @Column(name="PROVIDER")
    @Enumerated(EnumType.STRING)
    private OAuthProvider provider;
    @Column(name="PROVIDER_ID")
    private String providerId;
    @Column(name="EMAIL")
    private String email;
    @Enumerated(EnumType.STRING)
    @Column(name="STATUS")
    private OAuthStatus status;

    public static OAuthAccountEntity fromModel(OAuthAccount oAuthAccount) {
        return OAuthAccountEntity
                .builder()
                .oAuthId(oAuthAccount.getOAuthId())
                .user(new UserEntity().fromModel(oAuthAccount.getUser()))
                .provider(oAuthAccount.getProvider())
                .providerId(oAuthAccount.getProviderId())
                .email(oAuthAccount.getEmail())
                .status(oAuthAccount.getStatus())
                .build();
    }

    public OAuthAccount toModel(){
        return OAuthAccount.builder()
                .oAuthId(this.oAuthId)
                .user(this.user == null ? null : this.user.toModel())
                .provider(this.provider)
                .providerId(this.providerId)
                .email(this.email)
                .status(this.status)
                .build();
    }


}
