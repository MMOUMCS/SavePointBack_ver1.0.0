package com.couple.gallery.couple_gallery_backend.service;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.file.Files;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.couple.gallery.couple_gallery_backend.domain.User;
import com.couple.gallery.couple_gallery_backend.dto.ProfileRequest;
import com.couple.gallery.couple_gallery_backend.repository.UserRepository;

import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.couple.gallery.couple_gallery_backend.dto.UserLoginRequest;
import com.couple.gallery.couple_gallery_backend.config.security.JwtTokenProvider;
import com.couple.gallery.couple_gallery_backend.dto.JwtResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final AmazonS3 amazonS3;

    // 생성자 주입
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, AuthenticationManager authenticationManager, JwtTokenProvider jwtTokenProvider, AmazonS3 amazonS3) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtTokenProvider = jwtTokenProvider;
        this.amazonS3 = amazonS3;
    }

    // ▼ 설정 파일의 cloud.aws.s3.bucket.name 값을 가져옴
    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    private static final String DEFAULT_PROFILE_IMAGE = "https://cdn.pixabay.com/photo/2015/10/05/22/37/blank-profile-picture-973460_1280.png";


    // 이메일을 사용하여 사용자 정보를 조회하는 메서드
    public User findUserByEmail(String email) {
        return userRepository.findByEmail(email)
                // 조회 결과가 없으면 예외를 발생
                .orElseThrow(() -> new IllegalArgumentException("해당 이메일의 사용자를 찾을 수 없습니다."));
    }

    // 회원가입 로직 (Email 중복 검사 및 비밀번호 암호화)
    // UserService.java

    @Transactional
    public User registerNewUser(User user) {
        // 1. 중복 검사
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalStateException("이미 존재하는 이메일입니다.");
        }

        // 2. 암호화
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setConnectionCode(passwordEncoder.encode(user.getConnectionCode()));

        // 3. 기본 이미지 설정 (일단 이거 넣어두고 가입 완료시킴)
        user.setProfileImageUrl(DEFAULT_PROFILE_IMAGE);

        // 4. 저장 (여기서 ID 생성됨)
        return userRepository.save(user);
    }

    @Transactional
    public User updateProfile(Long userId, MultipartFile file, String bio) {
        // 1. 유저 찾기 (ID로 조회)
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UsernameNotFoundException("유저를 찾을 수 없습니다."));

        // 3. 프로필 이미지 업로드 (파일이 들어왔을 때만)
        if (file != null && !file.isEmpty()) {
            try {
                String oldImageUrl = user.getProfileImageUrl();
                // 폴더 구조: photo/{id}/profile/파일명
                String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
                String s3Path = "photos/" + user.getId() + "/profile/" + fileName;

                // 썸네일 생성 및 업로드 (이전에 만든 로직)
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                Thumbnails.of(file.getInputStream())
                        .size(150, 150)       //  150x150으로 압축
                        .outputFormat("jpg")
                        .outputQuality(0.7)   //  품질 70%
                        .toOutputStream(outputStream);

                byte[] imageBytes = outputStream.toByteArray();
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(imageBytes.length);
                metadata.setContentType("image/jpeg");

                // S3 업로드
                amazonS3.putObject(new PutObjectRequest(bucket, s3Path, new ByteArrayInputStream(imageBytes), metadata));

                // 기존 이미지가 기본 이미지가 아니면 S3에서 삭제하는 로직 추가 가능 (선택)

                // DB 업데이트
//                user.setProfileImageUrl(amazonS3.getUrl(bucket, s3Path).toString());
                user.setProfileImageUrl(publicUrl + "/" + s3Path);
                deleteS3Image(oldImageUrl);

            } catch (IOException e) {
                throw new IllegalStateException("이미지 업로드 실패", e);
            }
        }

        return user; // 수정된 유저 정보 반환
    }
    // ========================================================
    // 프로필 이미지 수정 메서드 (컨트롤러에서 이걸 찾고 있었음!)
    // =======================================================
    @Value("${cloud.aws.s3.public-url}")
    private String publicUrl;

    @Transactional
    public User updateProfileImage(String email, MultipartFile file) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("이미지 파일이 비어있습니다.");
        }

        try {
            String oldImageUrl = user.getProfileImageUrl();

            String fileName = UUID.randomUUID() + "_profile_" + user.getId() + "_" + System.currentTimeMillis() + ".jpg";
            String s3Path = "photos/" + user.getId() + "/profile/" + fileName;

            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            Thumbnails.of(file.getInputStream())
                    .size(150, 150)        //  프사는 어차피 작게 보이니 150x150으로 더 압축
                    .outputFormat("jpg")
                    .outputQuality(0.7)    //  품질 70%로 낮춰서 용량 확 줄이기
                    .toOutputStream(outputStream);
            byte[] imageBytes = outputStream.toByteArray();

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageBytes.length);
            metadata.setContentType("image/jpeg");

            amazonS3.putObject(new PutObjectRequest(bucket, s3Path, new ByteArrayInputStream(imageBytes), metadata));

            //  프라이빗 URL 대신 퍼블릭 URL로 저장
            String fileUrl = publicUrl + "/" + s3Path;
            user.setProfileImageUrl(fileUrl);

            deleteS3Image(oldImageUrl);

        } catch (IOException e) {
            throw new IllegalStateException("이미지 업로드에 실패했습니다.", e);
        }

        return user;
    }
//    @Transactional
//    public User updateProfileImage(String email, MultipartFile file) {
//        // 1. 토큰에 있는 이메일로 유저를 찾습니다.
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));
//
//        // 2. 파일이 비어있는지 체크
//        if (file == null || file.isEmpty()) {
//            throw new IllegalArgumentException("이미지 파일이 비어있습니다.");
//        }
//
//        // 3. S3 업로드 로직 (ID 기반 경로)
//        try {
//            // 1. 임시로 기존 이미지 URL을 변수에 보관해둡니다.
//            String oldImageUrl = user.getProfileImageUrl();
//
//            // 2. 새 파일명 생성 및 S3 업로드 진행
//            String fileName = UUID.randomUUID() + "_" + file.getOriginalFilename();
//            String s3Path = "photos/" + user.getId() + "/profile/" + fileName;
//
//            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
//            Thumbnails.of(file.getInputStream()).size(200, 200).outputFormat("jpg").toOutputStream(outputStream);
//            byte[] imageBytes = outputStream.toByteArray();
//
//            ObjectMetadata metadata = new ObjectMetadata();
//            metadata.setContentLength(imageBytes.length);
//            metadata.setContentType("image/jpeg");
//
//            // 새 파일 업로드 완료! (여기서 터지면 기존 이미지는 안전함)
//            amazonS3.putObject(new PutObjectRequest(bucket, s3Path, new ByteArrayInputStream(imageBytes), metadata));
//
//            // 3. DB 정보 갱신
//            String fileUrl = amazonS3.getUrl(bucket, s3Path).toString();
//            user.setProfileImageUrl(fileUrl);
//
//            // 4. 새 파일이 안전하게 올라갔으니, 이제 안심하고 예전 이미지를 지웁니다.
//            deleteS3Image(oldImageUrl);
//
//        } catch (IOException e) {
//            throw new IllegalStateException("이미지 업로드에 실패했습니다.", e);
//        }
//
//        return user; // 수정된 유저 정보 반환
//    }

    // S3에서 파일 삭제하는 헬퍼 메서드
    private void deleteS3Image(String fileUrl) {
        if (fileUrl == null || fileUrl.equals(DEFAULT_PROFILE_IMAGE)) {
            return;
        }
        try {
            //  .com/ 또는 .dev/ 둘 다 대응
            String key = null;
            for (String delimiter : new String[]{".com/", ".dev/"}) {
                int index = fileUrl.indexOf(delimiter);
                if (index != -1) {
                    key = fileUrl.substring(index + delimiter.length());
                    break;
                }
            }
            if (key != null) {
                String decodedKey = java.net.URLDecoder.decode(key, "UTF-8");
                amazonS3.deleteObject(bucket, decodedKey);
                System.out.println("기존 이미지 삭제 완료: " + decodedKey);
            }
        } catch (Exception e) {
            System.err.println("S3 이미지 삭제 실패: " + e.getMessage());
        }
    }
//    private void deleteS3Image(String fileUrl) {
//        if (fileUrl == null || fileUrl.equals(DEFAULT_PROFILE_IMAGE)) {
//            return; // 기본 이미지는 절대 지우면 안 됨!
//        }
//
//        try {
//            // URL에서 "파일명(Key)"만 추출하는 로직
//            // 예: https://...com/photos/1/profile/abc.jpg -> photos/1/profile/abc.jpg
//            String splitStr = ".com/";
//            int index = fileUrl.indexOf(splitStr);
//
//            if (index != -1) {
//                String fileName = fileUrl.substring(index + splitStr.length());
//                // 한글 파일명 등은 URL 인코딩 되어있을 수 있으니 디코딩 필요
//                String decodedFileName = java.net.URLDecoder.decode(fileName, "UTF-8");
//
//                // S3에서 진짜 삭제
//                amazonS3.deleteObject(bucket, decodedFileName);
//                System.out.println("기존 이미지 삭제 완료: " + decodedFileName);
//            }
//        } catch (Exception e) {
//            // 삭제 에러가 나도 로직이 멈추면 안 됨 (로그만 남김)
//            System.err.println("S3 이미지 삭제 실패: " + e.getMessage());
//        }
//    }


    // 로그인 로직
    public JwtResponse authenticateUser(UserLoginRequest loginRequest) {
        // 1. ID와 Password를 통해 Authentication 객체 생성
        UsernamePasswordAuthenticationToken authenticationToken =
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());

        // 2. 실제 인증 (CustomUserDetailsService의 loadUserByUsername 호출)
        Authentication authentication = authenticationManager.authenticate(authenticationToken);

        // 3. 인증 성공 시, JWT 토큰 생성
        String accessToken = jwtTokenProvider.generateToken(authentication);

        // 4. 로그인한 사용자 정보 조회
        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new UsernameNotFoundException("사용자를 찾을 수 없습니다."));

        // 5. JwtResponse 객체로 묶어 반환
        return JwtResponse.builder()
                .accessToken(accessToken)
                .grantType("Bearer")
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .profileImageUrl(user.getProfileImageUrl())
                .coupleId(user.getCoupleId())
                .build();
    }


    // ========================================================
    // 프로필 텍스트 수정 메서드 (이름, 소개글 변경)
    // ========================================================
    @Transactional // 값을 수정해야 하므로 readOnly=true를 깨고 쓰기 권한 부여
    public User updateProfileText(String email, ProfileRequest request) {
        // 1. findUserByEmail 메서드를 재활용해서 유저를 찾습니다.
        User user = findUserByEmail(email);

        // 2. 엔티티의 필드 값을 프론트에서 보낸 새 데이터로 세팅합니다.
        // (더티 체킹 덕분에 메소드가 끝날 때 알아서 DB에 Update 쿼리가 날아갑니다!)
        user.setUsername(request.getUsername());

        return user; // 수정된 유저 정보 반환
    }

    // ========================================================
    // 비밀번호 변경
    // ========================================================
    @Transactional
    public void updatePassword(String email, String currentPassword, String newPassword) {
        User user = findUserByEmail(email);
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("현재 비밀번호가 일치하지 않습니다.");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
    }
}