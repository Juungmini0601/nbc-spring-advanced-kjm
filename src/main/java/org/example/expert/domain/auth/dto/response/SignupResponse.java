package org.example.expert.domain.auth.dto.response;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class SignupResponse {

	private final String bearerToken;

	public SignupResponse(String bearerToken) {
		this.bearerToken = bearerToken;
	}
}
