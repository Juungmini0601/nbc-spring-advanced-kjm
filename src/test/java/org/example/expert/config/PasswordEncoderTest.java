package org.example.expert.config;

import static org.junit.jupiter.api.Assertions.*;

import org.example.expert.AbstractMockTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;

@DisplayName("PasswordEncoder 클래스")
class PasswordEncoderTest extends AbstractMockTest {

	@InjectMocks
	private PasswordEncoder passwordEncoder;

	@Nested
	@DisplayName("encode 메서드는")
	class EncodeMethod {

		@Nested
		@DisplayName("성공 케이스")
		class Success {
			@Test
			@DisplayName("패스워드를 암호화하여 원본과 다른 값을 반환한다")
			void should_return_different_value_from_original() {
				// given
				String rawPassword = "testPassword";

				// when
				String encodedPassword = passwordEncoder.encode(rawPassword);

				// then
				assertNotEquals(rawPassword, encodedPassword);
			}
		}
	}

	@Nested
	@DisplayName("matches 메서드는")
	class MatchesMethod {

		@Nested
		@DisplayName("성공 케이스")
		class Success {
			@Test
			@DisplayName("원본 패스워드와 암호화된 패스워드가 일치하면 true를 반환한다")
			void should_return_true_when_passwords_match() {
				// given
				String rawPassword = "testPassword";
				String encodedPassword = passwordEncoder.encode(rawPassword);

				// when
				boolean matches = passwordEncoder.matches(rawPassword, encodedPassword);

				// then
				assertTrue(matches);
			}

			@Test
			@DisplayName("원본 패스워드와 암호화된 패스워드가 일치하지 않으면 false를 반환한다")
			void should_return_false_when_passwords_do_not_match() {
				// given
				String rawPassword = "testPassword";
				String wrongPassword = "wrongPassword";
				String encodedPassword = passwordEncoder.encode(rawPassword);

				// when
				boolean matches = passwordEncoder.matches(wrongPassword, encodedPassword);

				// then
				assertFalse(matches);
			}
		}
	}
}
