package com.example.ebazaarobackend.service;

import com.example.ebazaarobackend.model.Like;
import com.example.ebazaarobackend.model.User;
import com.example.ebazaarobackend.repository.LikeRepository;
import com.example.ebazaarobackend.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class LikeService {
    @Autowired
    private LikeRepository likeRepository;
    @Autowired
    private PostRepository postRepository;

    @Transactional(readOnly = true)
    public boolean isLiked(User user, Long postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return likeRepository.existsByUserIdAndPostId(user.getId(), postId);
    }

    @Transactional
    public void addLike(User user, Long postId) {
        var post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var exists = likeRepository.existsByUserIdAndPostId(user.getId(), postId);
        if (exists)
            throw new ResponseStatusException(HttpStatus.CONFLICT);

        Like like = new Like();
        like.setUser(user);
        like.setPost(post);
        likeRepository.save(like);
    }

    @Transactional
    public void deleteLike(User user, Long postId) {
        postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        var like = likeRepository.findByUserIdAndPostId(user.getId(), postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        likeRepository.delete(like);
    }
}
