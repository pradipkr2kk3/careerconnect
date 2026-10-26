package com.careerconnect.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "likes")
@IdClass(Ids.UserPost.class)
@Getter @Setter @NoArgsConstructor
public class Likes {
    @Id private Long userId;
    @Id private Long postId;
    private Boolean likeStatus;
    private LocalDateTime likedTime;
    private Integer likeCount;
}
