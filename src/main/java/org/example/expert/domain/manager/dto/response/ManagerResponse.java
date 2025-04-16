package org.example.expert.domain.manager.dto.response;

import org.example.expert.domain.user.dto.response.UserResponse;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class ManagerResponse {

	private final Long id;
	private final UserResponse user;

	public ManagerResponse(Long id, UserResponse user) {
		this.id = id;
		this.user = user;
	}
}
