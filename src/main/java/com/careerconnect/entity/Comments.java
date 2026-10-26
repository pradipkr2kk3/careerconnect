package com.careerconnect.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter @Setter @NoArgsConstructor
public class Comments {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;
    private Long userId;
    private Long postId;
    private String commentContent;
    private LocalDateTime commentTime;
    private Integer commentCount;
}
