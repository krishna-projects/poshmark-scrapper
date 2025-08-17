package com.scrapper;

import com.scrapper.model.Product;
import com.scrapper.service.impl.PoshmarkScraperImpl;
import com.scrapper.util.FileUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

@Slf4j
public class PoshmarkScrapper {
    // Default values
    private static final String DEFAULT_CLOSET_URL = "https://poshmark.com/closet/peechypies?availability=available";
    private static final int DEFAULT_PRODUCT_COUNT = 10;
    private static final String DEFAULT_FILE_FORMAT = "json";
    private static final boolean DEFAULT_HEADLESS = true;

    public static void main(String[] args) {
        // Parse command line arguments
        String closetUrl = args.length > 0 ? args[0] : DEFAULT_CLOSET_URL;
        int productCount = args.length > 1 ? Integer.parseInt(args[1]) : DEFAULT_PRODUCT_COUNT;
        String fileFormat = args.length > 2 ? args[2].toLowerCase() : DEFAULT_FILE_FORMAT;
        boolean headless = args.length > 3 ? Boolean.parseBoolean(args[3]) : DEFAULT_HEADLESS;

        // Validate file format
        if (!List.of("json", "csv").contains(fileFormat)) {
            log.error("Invalid file format: {}. Using default: {}", fileFormat, DEFAULT_FILE_FORMAT);
            fileFormat = DEFAULT_FILE_FORMAT;
        }

        log.info("Starting Poshmark Scraper with settings:");
        log.info("Closet URL: {}", closetUrl);
        log.info("Product Count: {}", productCount);
        log.info("Output Format: {}", fileFormat);
        log.info("Headless Mode: {}", headless ? "Enabled" : "Disabled");

        try (PoshmarkScraperImpl service = new PoshmarkScraperImpl(headless)){
            double startTime = System.currentTimeMillis();

            log.info("Fetching product URLs...");
            Set<String> productUrls = service.getProductUrls(closetUrl, productCount);
            log.info("Extracted {} links from Poshmark", productUrls.size());

            log.info("Scraping product details...");
            List<Product> products = service.scrapeWithJsoup(productUrls);
            log.info("Successfully scraped {} products", products.size());
            double exceptionTimeSeconds = (System.currentTimeMillis() - startTime) / 1000.0;
            log.info("Total execution time: {} seconds",
                    (System.currentTimeMillis() - startTime) / 1000.0);
            String outputPath = FileUtil.saveResultsToFile(products, fileFormat, exceptionTimeSeconds, closetUrl);
            log.info("Results saved to: {}", outputPath);

        } catch (Exception e) {
            log.error("An error occurred during scraping: {}", e.getMessage());
            System.exit(1);
        }
    }
}
