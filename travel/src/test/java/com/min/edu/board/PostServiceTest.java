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
    void 존재하지않는_게시글_조회시_예외가_발생한다() {
        // given: DB에 없는 id로 조회 시도
        Long notExistId = 999999L;

        // when & then: 예외가 발생해야 함
        assertThatThrownBy(() -> postService.getBoard(notExistId))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("게시글이 존재하지 않습니다");
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

    @Test
    void BCrypt_비밀번호_생성() {
        org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder encoder
                = new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
        String encoded = encoder.encode("Abcd1234!@");
        System.out.println("암호화된 비밀번호: " + encoded);
    }


}