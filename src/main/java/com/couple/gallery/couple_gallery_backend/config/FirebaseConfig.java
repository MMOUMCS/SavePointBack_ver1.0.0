package com.couple.gallery.couple_gallery_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "fcm")
@Data
public class FirebaseConfig {

    // yml에 적어둔 설정값들 1:1로 꽂히는 곳
    private String type;
    private String projectId;
    private String privateKeyId;
    private String privateKey;
    private String clientEmail;
    private String clientId;
    private String authUri;
    private String tokenUri;
    private String authProviderX509CertUrl;
    private String clientX509CertUrl;
    private String universeDomain;

    @PostConstruct
    public void init() {
        try {
            Map<String, Object> configMap = new HashMap<>();
            configMap.put("type", type);
            configMap.put("project_id", projectId);
            configMap.put("private_key_id", privateKeyId);

            // yml에서 읽어올 때 줄바꿈 깨지는 거 방지

	    configMap.put("private_key", privateKey
                .replace("\\\\n", "\n")  // \\n → 실제 줄바꿈
   	    	.replace("\\n", "\n")    // \n → 실제 줄바꿈 (혹시 모를 경우 대비)
  	    	.trim()
	    );

            configMap.put("client_email", clientEmail);
            configMap.put("client_id", clientId);
            configMap.put("auth_uri", authUri);
            configMap.put("token_uri", tokenUri);
            configMap.put("auth_provider_x509_cert_url", authProviderX509CertUrl);
            configMap.put("client_x509_cert_url", clientX509CertUrl);
            configMap.put("universe_domain", universeDomain);

            // 구글 SDK가 알아먹게 스트림으로 변환
            byte[] jsonBytes = new ObjectMapper().writeValueAsBytes(configMap);

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(new ByteArrayInputStream(jsonBytes)))
                    .build();

            // 앱 여러 번 띄워도 중복 초기화 안 되게 방어 로직
            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase 세팅 끝! ᕕ( ᐛ )ᕗ");
            }
        } catch (Exception e) {
            System.err.println("Firebase 초기화 하다가 터짐: " + e.getMessage());
        }
    }
}
