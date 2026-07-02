package com.min.edu.auth.oauth;

import java.util.List;
import java.util.Random;

public class NicknameGenerator {

    private static final List<String> ADJECTIVES = List.of(
            "행복한", "즐거운", "신나는", "활발한", "따뜻한",
            "씩씩한", "귀여운", "용감한", "멋진", "사랑스러운",
            "유쾌한", "명랑한", "친절한", "반짝이는", "빛나는",
            "설레는", "달콤한", "포근한", "상쾌한", "자유로운"
    );

    private static final List<String> NOUNS = List.of(
            "여행자", "탐험가", "모험가", "나그네", "배낭여행자",
            "여행객", "탐방자", "관광객", "세계여행자", "여행러",
            "여행꾼", "길손", "탐험러", "여행인", "방랑자"
    );

    private static final Random RANDOM = new Random();

    public static String generate() {
        String adjective = ADJECTIVES.get(RANDOM.nextInt(ADJECTIVES.size()));
        String noun = NOUNS.get(RANDOM.nextInt(NOUNS.size()));
        int number = RANDOM.nextInt(9000) + 1000;
        return adjective + noun + number;
    }
}
