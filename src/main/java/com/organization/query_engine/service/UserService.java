package com.organization.query_engine.service;

import static com.organization.query_engine.enumeration.ExceptionMessageEnum.CLIENT_EXCEPTION;
import static com.organization.query_engine.enumeration.ExceptionMessageEnum.NOT_FOUND_EXCEPTION;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.ElasticsearchException;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.organization.query_engine.api.model.UserDetail;
import com.organization.query_engine.entity.UserEntity;
import com.organization.query_engine.exception.ClientException;
import com.organization.query_engine.exception.NotFoundException;
import com.organization.query_engine.mapper.UserMapper;
import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class UserService {
  private final ElasticsearchClient client;
  private final String indexName;

  public UserService(
      ElasticsearchClient client, @Value("${elasticsearch.indexName}") String indexName) {
    this.client = client;
    this.indexName = indexName;
  }

  public UserDetail findByUserId(String id) {
    try {
      GetResponse<UserEntity> response =
          client.get(requestGet -> requestGet.index(indexName).id(id), UserEntity.class);

      if (response.found()) {
        UserEntity user = response.source();
        return UserMapper.toDto(user);
      } else {
        throw new NotFoundException(MessageFormat.format(NOT_FOUND_EXCEPTION.getMessage(), id));
      }

    } catch (ElasticsearchException | IOException e) {
      throw new ClientException(CLIENT_EXCEPTION.getMessage());
    }
  }

  public long saveAll(List<UserEntity> users) {
    try {
      BulkRequest.Builder builder = new BulkRequest.Builder();
      for (UserEntity user : users) {
        builder.operations(
            operation ->
                operation.index(
                    index -> index.index(indexName).id(user.getId().toString()).document(user)));
      }
      // todo: stream and paralelstream difference
      BulkResponse response = client.bulk(builder.build());
      long count = response.items().parallelStream().filter(item -> item.error() == null).count();
      if (response.errors()) {
        CompletableFuture.runAsync(
            () -> {
              response
                  .items()
                  .forEach(
                      item -> {
                        if (item.error() != null) {
                          log.warn(
                              "Error indexing user ID {}: {}", item.id(), item.error().reason());
                        }
                      });
            });
      }

      return count;
    } catch (Exception ex) {
      log.warn("Failed to save users data in Elastic Search", ex);
      return 0L;
    }
  }

  public boolean isIndexEmpty(String indexName) {
    try {
      SearchResponse<Void> response =
          client.search(
              builder ->
                  builder
                      .index(indexName)
                      .size(1)
                      .query(
                          queryBuilder ->
                              queryBuilder.matchAll(matchAllBuilder -> matchAllBuilder)),
              Void.class);

      return response.hits().hits().isEmpty();
    } catch (Exception e) {
      return true;
    }
  }
}
