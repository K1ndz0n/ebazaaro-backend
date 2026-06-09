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

    public User createUserObject(String username, String email) {
        return new User(username, email, "password");
    }

    public User createUser(String username, String email) {
        return userRepository.save(createUserObject(username, email));
    }

    public Post createPostObject(User user, String name, Category category, City city) {
        Post post = new Post();
        post.setName(name);
        post.setEmail(user.getEmail());
        post.setPrice(0f);
        post.setCondition("new");
        post.setUser(user);
        post.setCategory(category);
        post.setCity(city);
        return post;
    }

    public Post createPost(User user, String name, Category category, City city) {
        return postRepository.save(createPostObject(user, name, category, city));
    }

    public Photo createPhotoObject(Post post) {
        Photo photo = new Photo();
        photo.setDisplayOrder(1);
        photo.setPost(post);
        photo.setPath("path");
        return photo;
    }

    public Photo createPhoto(Post post) {
        return photoRepository.save(createPhotoObject(post));
    }

    public Message createTextMessageObject(String text, User user, Chat chat) {
        Message message = new Message();
        message.setMessage(text);
        message.setIsTextMessage(true);
        message.setUser(user);
        message.setChat(chat);
        return message;
    }

    public Message createTextMessage(String text, User user, Chat chat) {
        return messageRepository.save(createTextMessageObject(text, user, chat));
    }

    public Like createLikeObject(User user, Post post) {
        Like like = new Like();
        like.setPost(post);
        like.setUser(user);
        return like;
    }

    public Like createLike(User user, Post post) {
        return likeRepository.save(createLikeObject(user, post));
    }

    public City createCityObject(String name, String voivodeship, Float lat, Float lng) {
        City city = new City();
        city.setName(name);
        city.setVoivodeship(voivodeship);
        city.setLatitude(lat);
        city.setLongitude(lng);
        return city;
    }

    public City createCity(String name, String voivodeship, Float lat, Float lng) {
        return cityRepository.save(createCityObject(name, voivodeship, lat, lng));
    }

    public Chat createChatObject(Post post, User user) {
        Chat chat = new Chat();
        chat.setPost(post);
        chat.setSeller(post.getUser());
        chat.setBuyer(user);
        return chat;
    }

    public Chat createChat(Post post, User user) {
        return chatRepository.save(createChatObject(post, user));
    }

    public Category createCategoryObject(String name) {
        return new Category(name);
    }

    public Category createCategory(String name) {
        return categoryRepository.save(createCategoryObject(name));
    }
}