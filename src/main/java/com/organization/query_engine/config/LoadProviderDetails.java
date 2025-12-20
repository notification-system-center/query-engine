package com.organization.query_engine.config;

import com.organization.query_engine.entity.UserEntity;
import com.organization.query_engine.service.FileService;
import com.organization.query_engine.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;

import java.util.List;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class LoadProviderDetails {

    private final UserService userService;
    private final FileService fileService;

    @Value("${elasticsearch.indexName}")
    private String indexName;

    @Value("${elasticsearch.pathName}")
    private String pathName;

    @EventListener(ContextRefreshedEvent.class)
    public void loadInitialDataIfIndexIsEmpty() {
        if (!userService.isIndexEmpty(indexName)) {
            return;
        }

        List<UserEntity> users = fileService.readUsersFromFile(pathName);

        if (users.isEmpty()) {
            log.info("No users loaded. Index remains empty.");
            return;
        }

        userService.saveAll(users);
        log.info("Elasticsearch index populated successfully");
    }
}
