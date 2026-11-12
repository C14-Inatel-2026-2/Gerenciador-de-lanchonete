package com.lanchonete.gerenciadorlanchonetebackend.user.repository;

import com.lanchonete.gerenciadorlanchonetebackend.user.model.UserProfile;
import org.springframework.data.repository.Repository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface UserProfileRepository extends Repository<UserProfile, Long> {

    boolean existsByEmail(String email);

    boolean existsBySupabaseUserId(String supabaseUserId);

    Optional<UserProfile> findBySupabaseUserId(String supabaseUserId);

    Optional<UserProfile> findById(Long id);

    List<UserProfile> findAllByOrderByNomeAsc();

    // Painel administrativo (US05): nomes dos clientes dos pedidos recentes e total de usuários
    List<UserProfile> findByIdIn(Collection<Long> ids);

    long count();

    UserProfile save(UserProfile userProfile);
}