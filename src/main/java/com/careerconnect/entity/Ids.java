package com.careerconnect.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/** Composite primary keys for Likes, Share and UserSkill. */
public class Ids {

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class UserPost implements Serializable {
        private Long userId;
        private Long postId;
    }

    @Data @NoArgsConstructor @AllArgsConstructor
    public static class UserSkillKey implements Serializable {
        private Long userId;
        private Long skillId;
    }
}
