package com.couple.gallery.couple_gallery_backend.repository;

import com.couple.gallery.couple_gallery_backend.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.Optional;

// interface UserRepository : jpa가 제공하는 쿼리 메소드 기능 덕분. 프록시를 알아서 만들고 주입하여 JPQL(자바 객체 기준의 쿼리)를 DB 전용 SQL로 실시간 번역 실행.
// jpa : 자바 개발자가 자바 코드만으로도 DB를 다룰 수 있게 해줌. (생산성 향상, 유지보수 편함, DB독립성)
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    // Optional을 사용함으로써 DB에 조회 당시 값이 존재하지 않을 수 있고(NULL) 그걸 서비스측에서 JPA 메서드를 통해 예외 처리가 가능하다.
    // findByEmail : JPA가 제공하는 쿼리 메소드 기능으로 find, Email을 통해 JPQL을 생성, SQL로 변환하는 프록시 객체 구현.
    Optional<User> findByEmail(String email);
    // username을 key로 둠.
    Optional<User> findByUsername(String username);
    // AndiIdNot : 나와 커플 유저 두 명이 id가 연결되어 있을 때, 단순히 커플 id를 가진 유저를 찾으면 나 자신까지 조회가 되어버림.
    // 그래서 내 커플 id(CouldId)로 조회를하되, 나 자신은 제외(IdNot)한 상대방 데이터만 필터링해서 꺼내오기 위함.
    Optional<User> findByCoupleIdAndIdNot(Long coupleId, Long id);
}
