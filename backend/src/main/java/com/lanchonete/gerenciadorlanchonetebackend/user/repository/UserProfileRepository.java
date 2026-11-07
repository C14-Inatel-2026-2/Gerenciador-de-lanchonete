package com.lanchonete.gerenciadorlanchonetebackend.user.repository;

import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import org.springframework.data.repository.Repository;

import java.util.Optional;

public interface UserProfileRepository extends Repository<UserProfile, Long> {

    boolean existsByEmail(String email);

    Optional<UserProfile> findById(Long id);

    UserProfile save(UserProfile userProfile);
}