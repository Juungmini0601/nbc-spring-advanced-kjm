package org.example.expert.domain.manager.dto.response;

import org.example.expert.domain.user.dto.response.UserResponse;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class ManagerSaveResponse {

	private final Long id;
	private final UserResponse user;

	public ManagerSaveResponse(Long id, UserResponse user) {
		this.id = id;
		this.user = user;
	}
}
