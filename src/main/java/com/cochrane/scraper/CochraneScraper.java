package com.cochrane.scraper;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class CochraneScraper {

    private static final String START_URL = "https://www.cochranelibrary.com/cdsr/reviews/topics";
    private static final String OUTPUT_FILE = "cochrane_reviews.txt";

    public static void main(String[] args) throws Exception {
        List<String> topicLinks = new ArrayList<>();

        // Step 1: Get all topic links from the starting page
        Document doc = fetchWithRetry(START_URL, 3, 30000);
        Elements topicElements = doc.select("ul.browse-by li a");

        for (Element topic : topicElements) {
            String topicUrl = topic.absUrl("href");
            String topicName = topic.text();
            if (!topicUrl.isEmpty() && topicName != null && !topicName.isEmpty()) {
                scrapeTopicReviews(topicUrl, topicName);
            }
        }

        System.out.println("Scraping completed. Output written to " + OUTPUT_FILE);
    }

    private static void scrapeTopicReviews(String topicUrl, String topicName) throws IOException {
        String nextPageUrl = topicUrl;
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(OUTPUT_FILE, true))) {
            while (nextPageUrl != null) {
                Document topicDoc = fetchWithRetry(nextPageUrl, 3, 30000);
                Elements reviewLinks = topicDoc.select("a.result-title");

                for (Element link : reviewLinks) {
                    String reviewUrl = link.absUrl("href");
                    try {
                        Document reviewDoc = fetchWithRetry(reviewUrl, 3, 30000);

                        String title = reviewDoc.selectFirst("h1.publication-title").text();
                        Elements authorElements = reviewDoc.select("span.author-name");
                        List<String> authors = new ArrayList<>();
                        for (Element author : authorElements) {
                            authors.add(author.text());
                        }
                        StringBuilder sb = new StringBuilder();
                        for (int i = 0; i < authors.size(); i++) {
                            sb.append(authors.get(i));
                            if (i < authors.size() - 1) {
                                sb.append(", ");
                            }
                        }
                        String authorsStr = sb.toString();


                        Element dateElement = reviewDoc.selectFirst("span.publication-date");
                        String date = dateElement != null ? dateElement.text() : "Unknown";

                        writer.write(String.format("%s|%s|%s|%s|%s\n",
                                reviewUrl, topicName, title, authorsStr, date));
                        writer.flush();

                    } catch (Exception e) {
                        System.err.println("Failed to parse review: " + reviewUrl);
                        e.printStackTrace();
                    }
                }

                Element nextPage = topicDoc.selectFirst("a[title=Next page]");
                nextPageUrl = nextPage != null ? nextPage.absUrl("href") : null;
            }
        }
    }

    private static Document fetchWithRetry(String url, int retries, int timeout) throws IOException {
        IOException lastException = null;
        for (int i = 0; i < retries; i++) {
            try {
                return Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36")
                        .header("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8")
                        .header("Accept-Language", "en-US,en;q=0.5")
                        .timeout(timeout)
                        .get();
            } catch (IOException e) {
                lastException = e;
                System.err.println("Failed to fetch " + url + " (attempt " + (i + 1) + "/" + retries + ")");
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ignored) {}
            }
        }
        throw lastException;
    }
}