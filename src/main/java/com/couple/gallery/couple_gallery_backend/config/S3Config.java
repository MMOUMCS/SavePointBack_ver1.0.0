package com.couple.gallery.couple_gallery_backend.config;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder; // 👈 이 임포트가 꼭 필요합니다!
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class S3Config {

    // 하드코딩 대신 yml 파일에 적어둔 보안 키들을 안전하게 매핑해서 가져옵니다.
    @Value("${cloud.aws.credentials.access-key}")
    private String accessKey;

    @Value("${cloud.aws.credentials.secret-key}")
    private String secretKey;

    @Value("${cloud.aws.region.static}")
    private String region;

    //  Cloudflare R2 호환을 위해 주소(Endpoint) 설정을 yml에서 가져옵니다.
    @Value("${cloud.aws.endpoint.uri}")
    private String endpointUri;

    @Bean
    public AmazonS3 amazonS3Client() { // 리턴 타입을 부모 인터페이스인 AmazonS3로 잡는 것이 정석입니다.
        BasicAWSCredentials credentials = new BasicAWSCredentials(accessKey, secretKey);

        //  [핵심] AWS 주소가 아닌 yml에 적힌 Cloudflare R2 주소로 강제 이정표를 세워줍니다.
        AwsClientBuilder.EndpointConfiguration endpointConfiguration =
                new AwsClientBuilder.EndpointConfiguration(endpointUri, region);

        return AmazonS3ClientBuilder.standard()
                .withEndpointConfiguration(endpointConfiguration) // 👈 기존 withRegion(region) 대신 이걸 넣어야 R2로 날아갑니다!
                .withCredentials(new AWSStaticCredentialsProvider(credentials))
                .build();
    }
}