package com.couple.gallery.couple_gallery_backend.repository;

import com.couple.gallery.couple_gallery_backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // email을 키로 사용
    Optional<User> findByEmail(String email);
    // username을 key로 일시적으로 둠
    Optional<User> findByUsername(String username);
    Optional<User> findByCoupleIdAndIdNot(Long coupleId, Long id);
}