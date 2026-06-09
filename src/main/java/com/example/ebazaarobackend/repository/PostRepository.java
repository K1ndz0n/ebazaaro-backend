package com.example.ebazaarobackend.repository;

import com.example.ebazaarobackend.model.Post;
import com.example.ebazaarobackend.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {
    Page<Post> findAll(Specification<Post> spec, Pageable sortedPageable);
    Page<Post> findAllByUser(User user, Specification<Post> spec, Pageable pageable);

    Page<Post> findAllByIdIn(List<Long> ids, Specification<Post> spec, Pageable pageable);

    List<Long> id(Long id);
}
