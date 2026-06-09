package com.example.ebazaarobackend;

import com.example.ebazaarobackend.model.*;
import com.example.ebazaarobackend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestDataFactory {
    @Autowired private UserRepository userRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private PhotoRepository photoRepository;
    @Autowired private MessageRepository messageRepository;
    @Autowired private LikeRepository likeRepository;
    @Autowired private CityRepository cityRepository;
    @Autowired private ChatRepository chatRepository;
    @Autowired private CategoryRepository categoryRepository;

    public User createUser(String username, String email) {
        return userRepository.save(new User(username, email, "password"));
    }

    public Post createPost(User user, String name, Category category, City city) {
        Post post = new Post();
        post.setName(name);
        post.setEmail(user.getEmail());
        post.setPrice(0f);
        post.setCondition("new");

        post.setUser(user);
        post.setCategory(category);
        post.setCity(city);

        return postRepository.save(post);
    }

    public Photo createPhoto(Post post) {
        Photo photo = new Photo();
        photo.setDisplayOrder(1);
        photo.setPost(post);
        photo.setPath("path");

        return photoRepository.save(photo);
    }

    public Message createTextMessage(String text, User user, Chat chat) {
        Message message = new Message();
        message.setMessage(text);
        message.setIsTextMessage(true);
        message.setUser(user);
        message.setChat(chat);

        return messageRepository.save(message);
    }

    public Like createLike(User user, Post post) {
        Like like = new Like();
        like.setPost(post);
        like.setUser(user);

        return likeRepository.save(like);
    }

    public City createCity(String name, String voivodeship, Float lat, Float lng) {
        City city = new City();
        city.setName(name);
        city.setVoivodeship(voivodeship);
        city.setLatitude(lat);
        city.setLongitude(lng);

        return cityRepository.save(city);
    }

    public Chat createChat(Post post, User user) {
        Chat chat = new Chat();
        chat.setPost(post);
        chat.setSeller(post.getUser());
        chat.setBuyer(user);

        return chatRepository.save(chat);
    }

    public Category createCategory(String name) {
        return categoryRepository.save(new Category(name));
    }
}
