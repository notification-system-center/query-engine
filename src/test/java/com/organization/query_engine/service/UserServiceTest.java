package com.organization.query_engine.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch.core.BulkRequest;
import co.elastic.clients.elasticsearch.core.BulkResponse;
import co.elastic.clients.elasticsearch.core.GetResponse;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.bulk.BulkResponseItem;
import co.elastic.clients.elasticsearch.core.bulk.OperationType;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.organization.query_engine.api.model.UserDetail;
import com.organization.query_engine.entity.UserEntity;
import com.organization.query_engine.enumeration.ExceptionMessageEnum;
import com.organization.query_engine.exception.NotFoundException;
import com.organization.query_engine.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    private static final UUID ID = UUID.randomUUID();
    private static final String INDEX_NAME = "users";

    @InjectMocks
    UserService userService;

        @Mock
        ElasticsearchClient client;

    @BeforeEach
    public void init() {
        ReflectionTestUtils.setField(userService, "indexName", INDEX_NAME);
    }

    @Test
    void shouldReturnUserDetail_whenUserExists() throws IOException {
        //GIVEN

        UserEntity userEntity = new UserEntity();
        userEntity.setId(ID);

        GetResponse<UserEntity> response = GetResponse.of(builder -> builder
                .id(ID.toString())
                .index(INDEX_NAME)
                .found(true)
                .source(userEntity));

        UserDetail expectedResponse = UserMapper.toDto(userEntity);

        //WHEN
        when(client.get(any(Function.class), eq(UserEntity.class))).thenReturn(response);
        UserDetail actualResponse = userService.findByUserId(ID.toString());

        //THEN
        assertEquals(expectedResponse, actualResponse);
    }


    @Test
    void shouldReturnNotFoundException_whenUserDoesNotExist() throws IOException {
        //GIVEN
        GetResponse<UserEntity> response = GetResponse.of(builder -> builder
                .id(ID.toString())
                .index(INDEX_NAME)
                .found(false));

        String expectedExceptionMessage = MessageFormat.format(ExceptionMessageEnum.NOT_FOUND_EXCEPTION.getMessage(), ID);
        // WHEN
        when(client.get(any(Function.class), eq(UserEntity.class))).thenReturn(response);

        //THEN
        Exception ex = assertThrows(NotFoundException.class, () -> userService.findByUserId(ID.toString()));
        assertEquals(expectedExceptionMessage, ex.getMessage());
    }

    @Test
    void saveAll_whenFileIsFull_and_NoErrors() throws IOException {
        // GIVEN
        UserEntity userEntity = new UserEntity();
        userEntity.setId(ID);

        BulkResponseItem item = new BulkResponseItem.Builder().index(INDEX_NAME).id(ID.toString())
                .operationType(OperationType.Index)
                .status(201)
                .result("created").build();

        BulkResponse bulkResponse = new BulkResponse.Builder().items(List.of(item))
                .errors(false)
                .took(10L)
                .build();
        long expectedResponse = bulkResponse.items().stream().count();
        // WHEN
        when(client.bulk(any(BulkRequest.class))).thenReturn(bulkResponse);
        long actualResponse = userService.saveAll(List.of(userEntity));

        //then
        assertEquals(expectedResponse, actualResponse);
    }

    @Test
    void saveAll_whenFileIsFull_and_hasSomeErrors() throws IOException {
        //Given
        UserEntity user = new UserEntity();
        user.setId(ID);
        user.setId(UUID.randomUUID());

        BulkResponseItem itemWithError = new BulkResponseItem.Builder()
                .index(INDEX_NAME)
                .id(ID.toString())
                .operationType(OperationType.Index)
                .status(400)
                .result("error")
                .error(error -> error.type("index failed")
                        .reason("Failed to index document"))
                .build();

        BulkResponseItem itemWithSuccess = new BulkResponseItem.Builder().index(INDEX_NAME).id(ID.toString())
                .operationType(OperationType.Index)
                .status(201)
                .result("created").build();

        BulkResponse bulkResponse = new BulkResponse.Builder()
                .items(List.of(itemWithError, itemWithSuccess))
                .errors(true)
                .took(10L)
                .build();

        long expectedCount = bulkResponse.items().stream().filter(item -> item.error() == null).count();
        when(client.bulk(any(BulkRequest.class))).thenReturn(bulkResponse);

        // then
        long actualCount = userService.saveAll(List.of(user));
        assertEquals(actualCount, expectedCount);
    }

    @Test
    void isIndexEmpty_ShouldReturnTrue_WhenIndexHasNoDocuments() throws IOException {
        // Given
        SearchResponse<Void> emptyResponse = SearchResponse.of(voidBuilder ->
                voidBuilder
                        .took(1)
                        .timedOut(false)
                        .shards(builder -> builder
                                .total(1)
                                .successful(1)
                                .failed(0))
                        .hits(builder -> builder.hits(Collections.emptyList())));

        //when
        when(client.search(any(Function.class), eq(Void.class))).thenReturn(emptyResponse);

        //then
        boolean result = userService.isIndexEmpty(INDEX_NAME);
        assertTrue(result);
    }

    @Test
    void isIndexEmpty_ShouldReturnFalse_WhenIndexHasDocument() throws IOException {
        // Given
        UserEntity userEntity = new UserEntity();
        userEntity.setId(ID);
        SearchResponse<Void> response = SearchResponse.of(voidBuilder ->
                voidBuilder
                        .took(1)
                        .timedOut(false)
                        .shards(builder -> builder
                                .total(1)
                                .successful(1)
                                .failed(0))
                        .hits(builder -> builder.
                                hits(Collections.singletonList(Hit.of(hitBuilder ->
                                        hitBuilder.id(ID.toString())
                                                .index(INDEX_NAME))))));
        //when
        when(client.search(any(Function.class), eq(Void.class))).thenReturn(response);

        //then
        boolean result = userService.isIndexEmpty(INDEX_NAME);
        assertFalse(result);
    }

}