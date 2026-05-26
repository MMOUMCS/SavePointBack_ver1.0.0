// 로그인시 db에서 정보 비교하는 메서드
package com.couple.gallery.couple_gallery_backend.config.security;

import com.couple.gallery.couple_gallery_backend.domain.User;
import com.couple.gallery.couple_gallery_backend.repository.UserRepository;
import lombok.RequiredArgsConstructor; // Lombok 어노테이션
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor // final 필드(userRepository)에 대한 생성자를 자동 생성
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Spring Security가 로그인 시도 시 호출하는 메서드입니다.
     * @param email 사용자가 입력한 사용자 이름 (우리는 이메일을 사용자 이름으로 사용)
     * @return UserDetails 객체 (Spring Security가 관리하는 인증 정보)
     * @throws UsernameNotFoundException 사용자를 찾을 수 없을 때 발생하는 예외
     */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("해당 이메일의 사용자를 찾을 수 없습니다: " + email));

        return new UserDetailsImpl(user);
    }
}
