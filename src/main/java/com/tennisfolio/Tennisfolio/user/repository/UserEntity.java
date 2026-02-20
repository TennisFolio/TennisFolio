package com.tennisfolio.Tennisfolio.user.repository;

import com.tennisfolio.Tennisfolio.Tournament.domain.Tournament;
import com.tennisfolio.Tennisfolio.Tournament.repository.TournamentEntity;
import com.tennisfolio.Tennisfolio.common.Entity.BaseTimeEntity;
import com.tennisfolio.Tennisfolio.common.RankingCategory;
import com.tennisfolio.Tennisfolio.common.UserStatus;
import com.tennisfolio.Tennisfolio.user.domain.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "tb_user",
        uniqueConstraints = @UniqueConstraint(
                columnNames = {"email", "status"}
        ))
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserEntity extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="USER_ID")
    private Long userId;
    @Column(name="EMAIL")
    private String email;
    @Column(name="NICKNAME")
    private String nickName;
    @Enumerated(EnumType.STRING)
    @Column(name="STATUS")
    private UserStatus status;

    public static UserEntity fromModel(User user) {
        return UserEntity
                .builder()
                .userId(user.getUserId())
                .email(user.getEmail())
                .nickName(user.getNickName())
                .status(user.getStatus())
                .build();
    }

    public User toModel(){
        return User.builder()
                .userId(this.userId)
                .email(this.email)
                .nickName(this.nickName)
                .status(this.status)
                .build();
    }
}
