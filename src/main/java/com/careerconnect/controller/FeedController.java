package com.careerconnect.controller;

import com.careerconnect.dao.QueryDao;
import com.careerconnect.entity.*;
import com.careerconnect.repository.*;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import static com.careerconnect.security.AuthHelper.*;

/** Posts, likes, comments and shares. */
@RestController
@RequestMapping("/api/feed")
@RequiredArgsConstructor
public class FeedController {

    private final QueryDao dao;
    private final PostRepository posts;
    private final LikesRepository likes;
    private final CommentsRepository comments;
    private final ShareRepository shares;

    @GetMapping
    public List<Map<String, Object>> feed(HttpSession s) {
        return dao.feed(require(s, SEEKER));
    }

    @PostMapping
    public Map<String, Object> createPost(@RequestBody Post body, HttpSession s) {
        Long uid = require(s, SEEKER);
        need(body.getPostContent(), "Post content");
        body.setPostId(null);
        body.setUserId(uid);
        body.setPostDate(LocalDateTime.now());
        posts.save(body);
        return Map.of("message", "Post published");
    }

    @PostMapping("/{postId}/like")
    public Map<String, Object> toggleLike(@PathVariable Long postId, HttpSession s) {
        Long uid = require(s, SEEKER);
        if (!posts.existsById(postId)) throw notFound("Post not found");
        Likes like = likes.findById(new Ids.UserPost(uid, postId)).orElse(null);
        if (like == null) {
            like = new Likes();
            like.setUserId(uid);
            like.setPostId(postId);
            like.setLikeStatus(true);
            like.setLikeCount(1);
        } else {
            like.setLikeStatus(!Boolean.TRUE.equals(like.getLikeStatus()));
        }
        like.setLikedTime(LocalDateTime.now());
        likes.save(like);
        return Map.of("likes", dao.likeCount(postId), "liked", like.getLikeStatus());
    }

    @GetMapping("/{postId}/comments")
    public List<Map<String, Object>> listComments(@PathVariable Long postId, HttpSession s) {
        require(s, SEEKER);
        return dao.comments(postId);
    }

    @PostMapping("/{postId}/comments")
    public Map<String, Object> addComment(@PathVariable Long postId, @RequestBody Map<String, String> body, HttpSession s) {
        Long uid = require(s, SEEKER);
        if (!posts.existsById(postId)) throw notFound("Post not found");
        need(body.get("commentContent"), "Comment");
        Comments c = new Comments();
        c.setUserId(uid);
        c.setPostId(postId);
        c.setCommentContent(body.get("commentContent").trim());
        c.setCommentTime(LocalDateTime.now());
        c.setCommentCount(1);
        comments.save(c);
        return Map.of("message", "Comment added");
    }

    @PostMapping("/{postId}/share")
    public Map<String, Object> share(@PathVariable Long postId,
                                     @RequestBody(required = false) Map<String, String> body, HttpSession s) {
        Long uid = require(s, SEEKER);
        if (!posts.existsById(postId)) throw notFound("Post not found");
        Share sh = shares.findById(new Ids.UserPost(uid, postId)).orElse(new Share());
        sh.setUserId(uid);
        sh.setPostId(postId);
        sh.setShareContent(body == null ? null : body.get("shareContent"));
        sh.setShareTime(LocalDateTime.now());
        sh.setShareCount(1);
        shares.save(sh);
        return Map.of("message", "Post shared");
    }
}
