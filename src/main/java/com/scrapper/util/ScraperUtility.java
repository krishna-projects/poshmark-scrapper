package com.scrapper.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.scrapper.model.Product;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
public class ScraperUtility {

    public static String extractProductIdFromUrl(String url) {
        // Extract the product ID from the URL (e.g., from "/listing/Product-Name-1234567890123456789")
        try {
            String[] parts = url.split("/");
            for (String part : parts) {
                if (part.matches(".*\\d{10,}")) {
                    return part.replaceAll("[^0-9]", "");
                }
            }
        } catch (Exception e) {
            log.warn("Could not extract product ID from URL: {}", url);
        }
        return "unknown_" + System.currentTimeMillis();
    }

    public static String saveResultsToFile(List<Product> products) throws IOException {
        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = "poshmark_products_" + timestamp + ".json";
        Path outputPath = Paths.get(fileName);

        try {
            String json = JsonUtils.toJson(products);
            Files.writeString(outputPath, json);
            return outputPath.toAbsolutePath().toString();
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON: {}", e.getMessage(), e);
            throw new IOException("Failed to convert products to JSON", e);
        }
    }

    public static void randomSleep(int minSeconds, int maxSeconds) {
        int min = minSeconds * 1000;
        int max = maxSeconds * 1000;
        try {
            int sleepTime = (int) (Math.random() * (max - min + 1) + min);
            log.info("Sleeping for {} seconds", ((double) sleepTime) / 1000);
            Thread.sleep(sleepTime);
        } catch (InterruptedException e) {
            log.error("Error sleeping: {}", e.getMessage(), e);
        }
    }
}
