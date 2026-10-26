package com.careerconnect.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "share")
@IdClass(Ids.UserPost.class)
@Getter @Setter @NoArgsConstructor
public class Share {
    @Id private Long userId;
    @Id private Long postId;
    private String shareContent;
    private LocalDateTime shareTime;
    private Integer shareCount;
}
