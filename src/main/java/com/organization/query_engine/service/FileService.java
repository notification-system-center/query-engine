package com.organization.query_engine.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.organization.query_engine.entity.UserEntity;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class FileService {

  private final ObjectMapper objectMapper;

  public FileService(ObjectMapper objectMapper) {
    this.objectMapper = objectMapper;
  }

  public List<UserEntity> readUsersFromFile(String pathName) {
    try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(pathName)) {

      if (inputStream == null) {
        log.warn("File not found: {}", pathName);
        return Collections.emptyList();
      }

      return List.of(objectMapper.readValue(inputStream, UserEntity[].class));

    } catch (Exception ex) {
      log.warn("Failed to read or parse file: {}", pathName, ex);
      return Collections.emptyList();
    }
  }
}
