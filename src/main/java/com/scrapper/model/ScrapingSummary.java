package com.scrapper.model;

import lombok.Data;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class ScrapingSummary {
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private int totalProducts;
    private int successCount;
    private final List<String> failedProducts = new ArrayList<>();
    private final List<String> successfulProducts = new ArrayList<>();

    public void start() {
        this.startTime = LocalDateTime.now();
    }

    public void end() {
        this.endTime = LocalDateTime.now();
    }

    public void addFailedProduct(String url, String reason) {
        failedProducts.add(url + " - " + reason);
    }

    public void addSuccessfulProduct(String url) {
        successfulProducts.add(url);
    }

    public String generateReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== Scraping Summary ===\n\n");
        
        if (startTime != null && endTime != null) {
            report.append(String.format("Start Time: %s%n", startTime));
            report.append(String.format("End Time: %s%n", endTime));
            
            Duration duration = Duration.between(startTime, endTime);
            String durationStr = String.format("%d minutes %d seconds", 
                duration.toMinutes(), 
                duration.minusMinutes(duration.toMinutes()).getSeconds());
                
            report.append(String.format("Duration: %s%n%n", durationStr));
            double avgSeconds = (double)duration.getSeconds() / totalProducts;
            report.append(String.format("Average time per product: %.2f seconds%n", avgSeconds));
        }

        report.append(String.format("Total Products: %d%n", totalProducts));
        report.append(String.format("Successfully Scraped: %d%n", totalProducts-failedProducts.size()));
        report.append(String.format("Failed: %d%n%n", failedProducts.size()));

        if (!successfulProducts.isEmpty()) {
            report.append("=== Successful Products ===\n");
            successfulProducts.forEach(fp -> report.append("- ").append(fp).append("\n"));
            report.append("\n");
        }

        if (!failedProducts.isEmpty()) {
            report.append("=== Failed Products ===\n");
            failedProducts.forEach(fp -> report.append("- ").append(fp).append("\n"));
            report.append("\n");
        }

        return report.toString();
    }
}
