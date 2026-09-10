package com.jhcs.newgram.infrastructure.aws;

import java.net.URI;
import java.time.Duration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3Configuration;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * Clientes S3 (SDK v2) com timeouts e pool configurados.
 *
 * <p>Em EC2/ECS com IAM role, omita {@code AWS_ACCESS_KEY_ID} e
 * {@code AWS_SECRET_ACCESS_KEY} para usar a credential chain padrao
 * (rotacao automatica). {@code AWS_S3_ENDPOINT} permite apontar para
 * MinIO/LocalStack em desenvolvimento.
 */
@Configuration
public class AwsConfig {

    @Value("${aws.accessKeyId:}")
    private String accessKey;

    @Value("${aws.secretKey:}")
    private String secretKey;

    @Value("${aws.region:us-east-1}")
    private String region;

    @Value("${aws.s3.endpoint:}")
    private String endpoint;

    @Value("${aws.s3.connection-timeout-seconds:10}")
    private int connectionTimeoutSeconds;

    @Value("${aws.s3.socket-timeout-seconds:30}")
    private int socketTimeoutSeconds;

    @Value("${aws.s3.max-connections:50}")
    private int maxConnections;

    @Bean
    public AwsCredentialsProvider awsCredentialsProvider() {
        if (accessKey != null && !accessKey.isBlank() && secretKey != null && !secretKey.isBlank()) {
            return StaticCredentialsProvider.create(AwsBasicCredentials.create(accessKey.trim(), secretKey.trim()));
        }
        return DefaultCredentialsProvider.create();
    }

    private NettyNioAsyncHttpClient.Builder httpClient() {
        return NettyNioAsyncHttpClient.builder()
                .connectionTimeout(Duration.ofSeconds(connectionTimeoutSeconds))
                .readTimeout(Duration.ofSeconds(socketTimeoutSeconds))
                .writeTimeout(Duration.ofSeconds(socketTimeoutSeconds))
                .maxConcurrency(maxConnections);
    }

    @Bean
    public S3Client s3Client(AwsCredentialsProvider credentials) {
        var builder = S3Client.builder()
                .region(Region.of(region))
                .credentialsProvider(credentials)
                .httpClientBuilder(httpClient())
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(endpoint != null && !endpoint.isBlank())
                        .build());
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint.trim()));
        }
        return builder.build();
    }

    @Bean
    public S3Presigner s3Presigner(AwsCredentialsProvider credentials) {
        var builder = S3Presigner.builder()
                .region(Region.of(region))
                .credentialsProvider(credentials);
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint.trim()));
        }
        return builder.build();
    }

    @Bean(destroyMethod = "close")
    public S3AsyncClient s3AsyncClient(AwsCredentialsProvider credentials) {
        var builder = S3AsyncClient.builder()
                .region(Region.of(region))
                .credentialsProvider(credentials)
                .httpClientBuilder(httpClient())
                .serviceConfiguration(S3Configuration.builder()
                        .pathStyleAccessEnabled(endpoint != null && !endpoint.isBlank())
                        .build());
        if (endpoint != null && !endpoint.isBlank()) {
            builder.endpointOverride(URI.create(endpoint.trim()));
        }
        return builder.build();
    }
}
