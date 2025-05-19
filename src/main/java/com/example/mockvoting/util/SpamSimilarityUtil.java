package com.example.mockvoting.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class SpamSimilarityUtil {
    private static final LevenshteinDistance DISTANCE = new LevenshteinDistance();

    /**
     * 문자열 유사도 계산 (0.0 ~ 1.0)
     */
    public double calculateSimilarity(String a, String b) {
        if (a == null || b == null || a.isBlank() || b.isBlank()) return 0.0;

        String cleanA = stripHtml(a);
        String cleanB = stripHtml(b);

        int maxLength = Math.max(cleanA.length(), cleanB.length());
        if (maxLength == 0) return 1.0;

        int distance = DISTANCE.apply(cleanA, cleanB);
        return 1.0 - ((double) distance / maxLength);
    }

    /**
     * HTML 태그 제거 후 순수 텍스트만 남김
     */
    private String stripHtml(String html) {
        return Jsoup.parse(html).text().trim();
    }
}
