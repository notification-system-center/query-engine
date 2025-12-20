package com.organization.query_engine.api;

import com.organization.query_engine.api.model.ModelApiResponse;
import com.organization.query_engine.api.model.UserDetail;
import com.organization.query_engine.service.UserService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController implements UserApi {

  private final UserService userService;

  @Override
  public ResponseEntity<ModelApiResponse> findByUserId(UUID id) {
    UserDetail user = userService.findByUserId(id.toString());
    return ResponseEntity.ok(
        new ModelApiResponse().code(200).message("User fetched successfully").data(user));
  }
}
