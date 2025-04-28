package com.school_project.smart_mirror.config.elasticsearch;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.json.jackson.JacksonJsonpMapper;
import co.elastic.clients.transport.ElasticsearchTransport;
import co.elastic.clients.transport.rest_client.RestClientTransport;
import lombok.Getter;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.ssl.SSLContexts;
import org.elasticsearch.client.RestClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;
import org.springframework.data.elasticsearch.config.EnableElasticsearchAuditing;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

import javax.net.ssl.SSLContext;
import java.net.URI;


@Configuration
@EnableElasticsearchAuditing
public class ElasticsearchConfig extends ElasticsearchConfiguration{

    @Value("${spring.elasticsearch.uris}")
    private String uri;

    public String getUri() {
        return uri;
    }

    @Value("${spring.elasticsearch.username}")
    private String username;

    @Value("${spring.elasticsearch.password}")
    private String password;

    @Bean
    public RestClient restClient() throws Exception {
        // URI 파싱
        URI uri = URI.create(getUri());
        String host = uri.getHost();
        int port = uri.getPort();

        // SSL 인증서 검증 우회 설정
        SSLContext sslContext = SSLContexts.custom()
                .loadTrustMaterial(null, (chain, authType) -> true) // 모든 인증서 신뢰 (개발용)
                .build();

        // 자격 증명 설정
        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(AuthScope.ANY,
                new UsernamePasswordCredentials(username, password));

        return RestClient.builder(new HttpHost(host, port, "https"))
                .setHttpClientConfigCallback(httpClientBuilder ->
                        httpClientBuilder
                                .setSSLContext(sslContext)
                                .setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE)
                                .setDefaultCredentialsProvider(credentialsProvider))
                .build();
    }

    @Override
    public ClientConfiguration clientConfiguration() {
        return ClientConfiguration.builder()
                .connectedTo(uri)
                .usingSsl()
                .withBasicAuth(username, password)
                .build();
    }







//    @Bean
//    public ElasticsearchClient elasticsearchClient() {
//        // 인증 정보 설정
//        final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
//        credentialsProvider.setCredentials(AuthScope.ANY,
//                new UsernamePasswordCredentials(username, password));
//
//        // RestClient 구성
//        RestClient restClient = RestClient.builder(
//                        new HttpHost("localhost", 9200, "https"))
//                .setHttpClientConfigCallback(httpClientBuilder -> {
//                    httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider);
//
//                    // SSL 설정 - 자체 서명 인증서 허용
//                    try {
//                        SSLContext sslContext = SSLContexts.custom()
//                                .loadTrustMaterial(null, (x509Certificates, s) -> true)
//                                .build();
//                        httpClientBuilder.setSSLContext(sslContext);
//                        httpClientBuilder.setSSLHostnameVerifier(NoopHostnameVerifier.INSTANCE);
//                    } catch (Exception e) {
//                        throw new RuntimeException(e);
//                    }
//
//                    return httpClientBuilder;
//                })
//                .setRequestConfigCallback(requestConfigBuilder ->
//                        requestConfigBuilder
//                                .setConnectTimeout(10000)
//                                .setSocketTimeout(60000))
//                .build();
//
//        // ElasticsearchTransport 생성
//        ElasticsearchTransport transport = new RestClientTransport(
//                restClient, new JacksonJsonpMapper());
//
//        // ElasticsearchClient 반환
//        return new ElasticsearchClient(transport);
//    }
//
//    @Bean
//    public ElasticsearchOperations elasticsearchOperations(ElasticsearchClient elasticsearchClient) {
//        return new org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate(elasticsearchClient);
//    }
//
//    @Bean(name = "elasticsearchTemplate")
//    public org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate elasticsearchTemplate(ElasticsearchClient elasticsearchClient) {
//        return new org.springframework.data.elasticsearch.client.elc.ElasticsearchTemplate(elasticsearchClient);
//    }
}
