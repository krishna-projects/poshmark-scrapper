package com.scrapper.util;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.util.List;
import java.util.Objects;

@Slf4j
public class ScraperUtility {

    // Extract the product ID from the URL (e.g., from "/listing/Product-Name-1234567890123456789")
    public static String extractProductIdFromUrl(String url) {
        try {
            String[] parts = url.split("/");
            return parts[parts.length - 1];
        } catch (Exception e) {
            log.warn("Could not extract product ID from URL: {}", url);
        }
        return "unknown";
    }

    // Add a random delay to avoid being blocked
    public static void randomSleep(int minSeconds, int maxSeconds) {
        int min = minSeconds * 1000;
        int max = maxSeconds * 1000;
        try {
            int sleepTime = (int) (Math.random() * (max - min + 1) + min);
            log.info("waiting for {} seconds", ((double) sleepTime) / 1000);
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            log.error("Error waiting: {}", e.getMessage());
        }
    }

    // Extract text from an HTML Document
    public static String getElementText(Document element, String selector) {
        try {
            return Objects.requireNonNull(element.selectFirst(selector)).text();
        } catch (Exception e) {
            log.warn("Could not extract text from document: {}", e.getMessage());
            return "";
        }
    }

    // Extract text from an HTML element
    public static String getElementText(Element element) {
        try {
            return element.text();
        } catch (Exception e) {
            log.warn("Could not extract text from element: {}", e.getMessage());
            return "";
        }
    }

    // Extract image URLs from a product page
    public static List<String> getImageUrls(Document doc) {
        return doc.select("ul.carousel-vertical__inner > li").stream()
                .map(element -> getImages(element.selectFirst("img")))
                .toList();
    }

    // Extract text from a list of elements
    public static List<String> getListElementText(Document doc, String selector) {
        return doc.select(selector).stream()
                .map(ScraperUtility::getElementText)
                .toList();
    }

    // Extract image URLs from an HTML element
    public static String getImages(Element image) {
        try {
            return image.attr("src").isEmpty() ? image.attr("data-src") : image.attr("src");
        } catch (Exception e) {
            log.warn("Could not extract images from element: {}", e.getMessage());
        }
        return null;
    }
}
