package com.example.ebazaarobackend.service;

import com.example.ebazaarobackend.dto.PhotoRequest;
import com.example.ebazaarobackend.dto.SetPhotosRequest;
import com.example.ebazaarobackend.model.Photo;
import com.example.ebazaarobackend.model.User;
import com.example.ebazaarobackend.repository.PhotoRepository;
import com.example.ebazaarobackend.repository.PostRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;

@Service
public class PhotoService {
    @Autowired
    private PhotoRepository photoRepository;
    @Autowired
    private StorageService storageService;
    @Autowired
    private PostRepository postRepository;

    @Transactional
    public void setPhotos(User user, Long postId, SetPhotosRequest request) {
        var post = postRepository.findById(postId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        if (!post.getUser().getId().equals(user.getId()))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN);

        List<Photo> photosToDelete = new ArrayList<>(post.getPhotos());

        if (request.getPhotos() == null || request.getPhotos().isEmpty()) {
            deletePhotos(photosToDelete);
            return;
        }

        for (PhotoRequest p : request.getPhotos()) {
            if (p.getId() == -1) {
                String path = storageService.store(p.getFile(), "posts");
                Photo photo = new Photo();
                photo.setPost(post);
                photo.setPath(path);
                photo.setDisplayOrder(p.getOrder());

                photoRepository.save(photo);
            } else {
                photoRepository.findById(p.getId()).ifPresent(photo -> {
                    photo.setDisplayOrder(p.getOrder());

                    photosToDelete.removeIf(pt -> pt.getId().equals(photo.getId()));
                });
            }
        }

        deletePhotos(photosToDelete);
    }

    private void deletePhotos(List<Photo> photos) {
        for (Photo photo : photos) {
            storageService.delete(photo.getPath());
            photo.getPost().getPhotos().remove(photo);
            photoRepository.delete(photo);
        }
    }
}