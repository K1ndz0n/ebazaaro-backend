package com.example.ebazaarobackend.repository;

import com.example.ebazaarobackend.model.Chat;
import com.example.ebazaarobackend.model.Post;
import com.example.ebazaarobackend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChatRepository extends JpaRepository<Chat, Long> {
    boolean existsByPostAndBuyerAndSeller(Post post, User buyer, User seller);
    List<Chat> findAllBySellerOrBuyer(User seller, User buyer);
    List<Chat> findAllByPost(Post post);
}
