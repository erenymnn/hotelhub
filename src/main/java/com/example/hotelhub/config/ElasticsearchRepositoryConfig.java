package com.example.hotelhub.config;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;

@Configuration
@ConditionalOnProperty(prefix = "app.elasticsearch", name = "enabled", havingValue = "true", matchIfMissing = true) //applicationda enabled varsa ve true ise yoksada otomatik true kabul eder.
@EnableElasticsearchRepositories(basePackages = "com.example.hotelhub.elasticsearch")
// Diyelim ki yerel bilgisayarında Elasticsearch yok ama test yapman gerekiyor. application.yml dosyana app.elasticsearch.enabled: false yazarsın, sistem Elasticsearch'ü hiç ayağa kaldırmaz ve uygulama hata vermeden açılır.
public class ElasticsearchRepositoryConfig {
}
