package com.example.mockvoting.domain.news.crawler;

import com.example.mockvoting.domain.news.dto.NewsDTO;
import org.jsoup.Jsoup;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;


import java.util.ArrayList;
import java.util.List;

@Component
public class NewsCrawler {
    private static final String url = "https://news.naver.com/breakingnews/section/100/269";

    public List<NewsDTO> fetchLatestNews() {
        List<NewsDTO> newsDTOList = new ArrayList<>();

        try {
            Document doc = Jsoup.connect(url).get();
            Elements items = doc.select(".sa_list .sa_item");
            for (Element item : items) {
                // 제목, 요약, 링크
                String title = item.select("a.sa_text_title").text();
                String summary = item.select("div.sa_text_lede").text();
                String link = item.select("a.sa_text_title").attr("href");

                // 썸네일 이미지
                Element imgTag = item.selectFirst("a.sa_thumb_link img");
                String imageUrl = "";

                if (imgTag != null) {
                    imageUrl = imgTag.attr("src");

                    // 만약 src가 비어있거나 placeholder면 data-src 확인
                    if (imageUrl.isEmpty() || imageUrl.contains("lazy") || imageUrl.contains("blank")) {
                        imageUrl = imgTag.attr("data-src");
                    }
                }

                newsDTOList.add(new NewsDTO(title, summary+"...", imageUrl, link));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return newsDTOList;
    }
}
