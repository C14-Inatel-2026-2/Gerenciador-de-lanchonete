package com.lanchonete.gerenciadorlanchonetebackend.user.repository;

import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import org.springframework.data.repository.Repository;

public interface UserProfileRepository extends Repository<UserProfile, Long> {

    boolean existsByEmail(String email);

    UserProfile save(UserProfile userProfile);
}