package org.example.expert.domain.auth.dto.response;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class SigninResponse {

	private final String bearerToken;

	public SigninResponse(String bearerToken) {
		this.bearerToken = bearerToken;
	}
}
