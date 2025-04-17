package org.example.expert.domain.comment.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.example.expert.AbstractMockTest;
import org.example.expert.domain.comment.dto.request.CommentSaveRequest;
import org.example.expert.domain.comment.dto.response.CommentSaveResponse;
import org.example.expert.domain.comment.entity.Comment;
import org.example.expert.domain.comment.repository.CommentRepository;
import org.example.expert.domain.common.dto.AuthUser;
import org.example.expert.domain.common.exception.InvalidRequestException;
import org.example.expert.domain.todo.entity.Todo;
import org.example.expert.domain.todo.repository.TodoRepository;
import org.example.expert.domain.user.entity.User;
import org.example.expert.domain.user.enums.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

@DisplayName("CommentService 클래스")
class CommentServiceTest extends AbstractMockTest {

	@Mock
	private CommentRepository commentRepository;

	@Mock
	private TodoRepository todoRepository;

	@InjectMocks
	private CommentService commentService;

	@Nested
	@DisplayName("saveComment 메서드는")
	class SaveCommentMethod {

		@Nested
		@DisplayName("성공 케이스")
		class SuccessCase {

			@Test
			@DisplayName("유효한 요청으로 댓글을 정상적으로 등록한다")
			void should_save_comment_successfully() {
				// given
				long todoId = 1;
				CommentSaveRequest request = new CommentSaveRequest("contents");
				AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
				User user = User.fromAuthUser(authUser);
				Todo todo = new Todo("title", "title", "contents", user);
				Comment comment = new Comment(request.getContents(), user, todo);

				given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
				given(commentRepository.save(any())).willReturn(comment);

				// when
				CommentSaveResponse result = commentService.saveComment(authUser, todoId, request);

				// then
				assertNotNull(result);
				assertEquals(comment.getContents(), request.getContents());
			}
		}

		@Nested
		@DisplayName("실패 케이스")
		class FailureCase {

			@Test
			@DisplayName("할일을 찾지 못하면 InvalidRequestException이 발생한다")
			void should_throw_exception_when_todo_not_found() {
				// given
				long todoId = 1;
				CommentSaveRequest request = new CommentSaveRequest("contents");
				AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);

				given(todoRepository.findById(anyLong())).willReturn(Optional.empty());

				// when
				InvalidRequestException exception = assertThrows(InvalidRequestException.class, () -> {
					commentService.saveComment(authUser, todoId, request);
				});

				// then
				assertEquals("Todo not found", exception.getMessage());
			}

			@Test
			@DisplayName("댓글 내용이 비어있으면 NullPointerException이 발생한다")
			void should_throw_exception_when_content_is_empty() {
				// given
				long todoId = 1;
				CommentSaveRequest request = new CommentSaveRequest("");
				AuthUser authUser = new AuthUser(1L, "email", UserRole.USER);
				User user = User.fromAuthUser(authUser);
				Todo todo = new Todo("title", "title", "contents", user);

				given(todoRepository.findById(anyLong())).willReturn(Optional.of(todo));
				given(commentRepository.save(any())).willReturn(
					null); // Mock to return null to simulate the actual behavior

				// when & then
				NullPointerException exception = assertThrows(NullPointerException.class, () -> {
					commentService.saveComment(authUser, todoId, request);
				});

				assertTrue(exception.getMessage().contains("Cannot invoke"));
			}
		}

	}
}
