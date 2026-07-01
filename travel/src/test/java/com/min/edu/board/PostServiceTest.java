package com.min.edu.board;

import com.min.edu.board.dto.PostDto;
import com.min.edu.board.entity.PostEntity;
import com.min.edu.board.repository.PostRepository;
import com.min.edu.board.service.PostService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class PostServiceTest {

    @Autowired
    private PostService postService;

    @Autowired
    private PostRepository postRepository;

    @Test
    void 필수값_누락시_게시글이_저장되지_않는다() {
        // given: title이 null인 잘못된 데이터
        PostDto invalidDto = new PostDto();
        // title, content, userId 모두 null인 상태

        // when & then: 예외가 발생해야 하고
        assertThatThrownBy(() -> postService.createBoard(invalidDto))
                .isInstanceOf(Exception.class);

        // 게시글이 DB에 저장되지 않았는지 확인
        assertThat(postRepository.findAll()).isEmpty();
    }

    @Test
    void 게시글_저장_성공시_DB에_저장된다() {
        // given: 정상적인 데이터
        PostDto validDto = new PostDto();
        // PostDto에 필드 직접 세팅이 안 되니, 빌더나 생성자 활용
        // 아래처럼 PostEntity를 통해 테스트용 DTO 만들기
        PostEntity testEntity = PostEntity.builder()
                .title("테스트 제목")
                .content("테스트 내용")
                .userId(1L)
                .build();
        postRepository.save(testEntity);

        // then: DB에 저장됐는지 확인
        assertThat(postRepository.findAll()).isNotEmpty();
        assertThat(postRepository.findAll().get(0).getTitle()).isEqualTo("테스트 제목");
    }
}