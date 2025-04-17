package org.example.expert.domain.manager.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.List;
import java.util.Optional;

import org.example.expert.AbstractMockTest;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.manager.dto.request.ManagerSaveRequest;
import org.example.expert.domain.manager.dto.response.ManagerResponse;
import org.example.expert.domain.manager.dto.response.ManagerSaveResponse;
import org.example.expert.domain.manager.entity.Manager;
import org.example.expert.domain.manager.repository.ManagerRepository;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.example.expert.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.test.util.ReflectionTestUtils;

@DisplayName("ManagerService 클래스")
class ManagerServiceTest extends AbstractMockTest {

	@Mock
	private ManagerRepository managerRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private TodoRepository todoRepository;

	@InjectMocks
	private ManagerService managerService;

	@Nested
	@DisplayName("getManagers 메서드는")
	class GetManagersMethod {

		@Nested
		@DisplayName("성공 케이스")
		class SuccessCase {

			@Test
			@DisplayName("Todo가 있다면 매니저 목록을 정상적으로 조회한다")
			void should_return_manager_list_successfully() {
				// given
				long todoId = 1L;
				User user = new User("user1@example.com", "password", UserRole.USER);
				Todo todo = new Todo("Title", "Contents", "Sunny", user);
				ReflectionTestUtils.setField(todo, "id", todoId);

				Manager mockManager = new Manager(todo.getUser(), todo);
				List<Manager> managerList = List.of(mockManager);

				given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
				given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(managerList);

				// when
				List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

				// then
				assertEquals(1, managerResponses.size());
				assertEquals(mockManager.getId(), managerResponses.get(0).getId());
				assertEquals(mockManager.getUser().getEmail(), managerResponses.get(0).getUser().getEmail());
			}

			@Test
			@DisplayName("매니저가 없는 Todo에 대해 빈 목록을 반환한다")
			void should_return_empty_list_when_no_managers() {
				// given
				long todoId = 1L;
				User user = new User("user1@example.com", "password", UserRole.USER);
				Todo todo = new Todo("Title", "Contents", "Sunny", user);
				ReflectionTestUtils.setField(todo, "id", todoId);

				List<Manager> emptyList = List.of();

				given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
				given(managerRepository.findByTodoIdWithUser(todoId)).willReturn(emptyList);

				// when
				List<ManagerResponse> managerResponses = managerService.getManagers(todoId);

				// then
				assertEquals(0, managerResponses.size());
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class FailureCase {

			@Test
			@DisplayName("Todo가 없다면 InvalidRequestException 에러를 던진다")
			void should_throw_exception_when_todo_not_found() {
				// given
				long todoId = 1L;
				given(todoRepository.findById(todoId)).willReturn(Optional.empty());

				// when & then
				InvalidRequestException exception = assertThrows(InvalidRequestException.class,
					() -> managerService.getManagers(todoId));
				assertEquals("Todo not found", exception.getMessage());
			}
		}
	}

	@Nested
	@DisplayName("saveManager 메서드는")
	class SaveManagerMethod {

		@Nested
		@DisplayName("성공 케이스")
		class SuccessCase {

			@Test
			@DisplayName("매니저를 정상적으로 등록한다")
			void should_save_manager_successfully() {
				// given
				AuthUser authUser = new AuthUser(1L, "test1@test.com", UserRole.USER);
				User user = User.fromAuthUser(authUser);  // 일정을 만든 유저

				long todoId = 1L;
				Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);

				long managerUserId = 2L;
				User managerUser = new User("test2@test.com", "password", UserRole.USER);  // 매니저로 등록할 유저
				ReflectionTestUtils.setField(managerUser, "id", managerUserId);

				ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId); // request dto 생성

				given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
				given(userRepository.findById(managerUserId)).willReturn(Optional.of(managerUser));
				given(managerRepository.save(any(Manager.class))).willAnswer(invocation -> invocation.getArgument(0));

				// when
				ManagerSaveResponse response = managerService.saveManager(authUser, todoId, managerSaveRequest);

				// then
				assertNotNull(response);
				assertEquals(managerUser.getId(), response.getUser().getId());
				assertEquals(managerUser.getEmail(), response.getUser().getEmail());
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class FailureCase {

			@Test
			@DisplayName("Todo의 user가 null인 경우 예외가 발생한다")
			void should_throw_exception_when_todo_user_is_null() {
				// given
				AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
				long todoId = 1L;
				long managerUserId = 2L;

				Todo todo = new Todo();
				ReflectionTestUtils.setField(todo, "user", null);

				ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

				given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

				// when & then
				InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
					managerService.saveManager(authUser, todoId, managerSaveRequest)
				);

				assertEquals("담당자를 등록하려고 하는 유저가 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
			}

			@Test
			@DisplayName("Todo가 존재하지 않으면 예외가 발생한다")
			void should_throw_exception_when_todo_not_found() {
				// given
				AuthUser authUser = new AuthUser(1L, "a@a.com", UserRole.USER);
				long todoId = 1L;
				long managerUserId = 2L;

				ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

				given(todoRepository.findById(todoId)).willReturn(Optional.empty());

				// when & then
				InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
					managerService.saveManager(authUser, todoId, managerSaveRequest)
				);

				assertEquals("Todo not found", exception.getMessage());
			}

			@Test
			@DisplayName("매니저로 등록할 유저가 존재하지 않으면 예외가 발생한다")
			void should_throw_exception_when_manager_user_not_found() {
				// given
				AuthUser authUser = new AuthUser(1L, "test1@test.com", UserRole.USER);
				User user = User.fromAuthUser(authUser);
				long todoId = 1L;
				long managerUserId = 2L;

				Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);
				ManagerSaveRequest managerSaveRequest = new ManagerSaveRequest(managerUserId);

				given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
				given(userRepository.findById(managerUserId)).willReturn(Optional.empty());

				// when & then
				InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
					managerService.saveManager(authUser, todoId, managerSaveRequest)
				);

				assertEquals("등록하려고 하는 담당자 유저가 존재하지 않습니다.", exception.getMessage());
			}
		}
	}

	@Nested
	@DisplayName("deleteManager 메서드는")
	class DeleteManagerMethod {

		@Nested
		@DisplayName("성공 케이스")
		class SuccessCase {

			@Test
			@DisplayName("매니저를 정상적으로 삭제한다")
			void should_delete_manager_successfully() {
				// given
				long userId = 1L;
				long todoId = 1L;
				long managerId = 1L;

				User user = new User("test1@test.com", "password", UserRole.USER);
				ReflectionTestUtils.setField(user, "id", userId);

				Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);
				ReflectionTestUtils.setField(todo, "id", todoId);

				User managerUser = new User("test2@test.com", "password", UserRole.USER);
				ReflectionTestUtils.setField(managerUser, "id", 2L);

				Manager manager = new Manager(managerUser, todo);
				ReflectionTestUtils.setField(manager, "id", managerId);

				given(userRepository.findById(userId)).willReturn(Optional.of(user));
				given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
				given(managerRepository.findById(managerId)).willReturn(Optional.of(manager));
				willDoNothing().given(managerRepository).delete(manager);

				// when & then
				assertDoesNotThrow(() -> managerService.deleteManager(userId, todoId, managerId));
				verify(managerRepository, times(1)).delete(manager);
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class FailureCase {

			@Test
			@DisplayName("유저가 존재하지 않으면 예외가 발생한다")
			void should_throw_exception_when_user_not_found() {
				// given
				long userId = 1L;
				long todoId = 1L;
				long managerId = 1L;

				given(userRepository.findById(userId)).willReturn(Optional.empty());

				// when & then
				InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
					managerService.deleteManager(userId, todoId, managerId)
				);

				assertEquals("User not found", exception.getMessage());
			}

			@Test
			@DisplayName("Todo가 존재하지 않으면 예외가 발생한다")
			void should_throw_exception_when_todo_not_found() {
				// given
				long userId = 1L;
				long todoId = 1L;
				long managerId = 1L;

				User user = new User("test1@test.com", "password", UserRole.USER);
				ReflectionTestUtils.setField(user, "id", userId);

				given(userRepository.findById(userId)).willReturn(Optional.of(user));
				given(todoRepository.findById(todoId)).willReturn(Optional.empty());

				// when & then
				InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
					managerService.deleteManager(userId, todoId, managerId)
				);

				assertEquals("Todo not found", exception.getMessage());
			}

			@Test
			@DisplayName("Todo의 user가 null이거나 요청한 유저가 Todo 작성자가 아니면 예외가 발생한다")
			void should_throw_exception_when_user_is_not_todo_creator() {
				// given
				long userId = 1L;
				long todoId = 1L;
				long managerId = 1L;

				User user = new User("test1@test.com", "password", UserRole.USER);
				ReflectionTestUtils.setField(user, "id", userId);

				User anotherUser = new User("test2@test.com", "password", UserRole.USER);
				ReflectionTestUtils.setField(anotherUser, "id", 2L);

				Todo todo = new Todo("Test Title", "Test Contents", "Sunny", anotherUser);
				ReflectionTestUtils.setField(todo, "id", todoId);

				given(userRepository.findById(userId)).willReturn(Optional.of(user));
				given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));

				// when & then
				InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
					managerService.deleteManager(userId, todoId, managerId)
				);

				assertEquals("해당 일정을 만든 유저가 유효하지 않습니다.", exception.getMessage());
			}

			@Test
			@DisplayName("매니저가 존재하지 않으면 예외가 발생한다")
			void should_throw_exception_when_manager_not_found() {
				// given
				long userId = 1L;
				long todoId = 1L;
				long managerId = 1L;

				User user = new User("test1@test.com", "password", UserRole.USER);
				ReflectionTestUtils.setField(user, "id", userId);

				Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);
				ReflectionTestUtils.setField(todo, "id", todoId);

				given(userRepository.findById(userId)).willReturn(Optional.of(user));
				given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
				given(managerRepository.findById(managerId)).willReturn(Optional.empty());

				// when & then
				InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
					managerService.deleteManager(userId, todoId, managerId)
				);

				assertEquals("Manager not found", exception.getMessage());
			}

			@Test
			@DisplayName("매니저가 해당 Todo에 등록된 매니저가 아니면 예외가 발생한다")
			void should_throw_exception_when_manager_not_associated_with_todo() {
				// given
				long userId = 1L;
				long todoId = 1L;
				long managerId = 1L;

				User user = new User("test1@test.com", "password", UserRole.USER);
				ReflectionTestUtils.setField(user, "id", userId);

				Todo todo = new Todo("Test Title", "Test Contents", "Sunny", user);
				ReflectionTestUtils.setField(todo, "id", todoId);

				Todo anotherTodo = new Todo("Another Title", "Another Contents", "Rainy", user);
				ReflectionTestUtils.setField(anotherTodo, "id", 2L);

				User managerUser = new User("test2@test.com", "password", UserRole.USER);
				ReflectionTestUtils.setField(managerUser, "id", 2L);

				Manager manager = new Manager(managerUser, anotherTodo);
				ReflectionTestUtils.setField(manager, "id", managerId);

				given(userRepository.findById(userId)).willReturn(Optional.of(user));
				given(todoRepository.findById(todoId)).willReturn(Optional.of(todo));
				given(managerRepository.findById(managerId)).willReturn(Optional.of(manager));

				// when & then
				InvalidRequestException exception = assertThrows(InvalidRequestException.class, () ->
					managerService.deleteManager(userId, todoId, managerId)
				);

				assertEquals("해당 일정에 등록된 담당자가 아닙니다.", exception.getMessage());
			}
		}
	}
}
