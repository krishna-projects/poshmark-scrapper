package com.scrapper;

import com.scrapper.model.Product;
import com.scrapper.service.impl.PoshmarkScraperImpl;
import com.scrapper.util.FileUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Scanner;
import java.util.Set;

@Slf4j
public class PoshmarkScrapper {
    // Default values
    private static final String DEFAULT_CLOSET_URL = "https://poshmark.com/closet/peechypies?availability=available";
    private static final int DEFAULT_PRODUCT_COUNT = 10;
    private static final String DEFAULT_FILE_FORMAT = "json";
    private static final boolean DEFAULT_HEADLESS = true;

    public static void main(String[] args) {
        String closetUrl = DEFAULT_CLOSET_URL;
        int productCount = DEFAULT_PRODUCT_COUNT;
        String fileFormat = DEFAULT_FILE_FORMAT;
        boolean headless = DEFAULT_HEADLESS;

        // Parse command line arguments
        if (args.length > 0) {
            try {
                closetUrl = args[0];
                productCount = Integer.parseInt(args[1]);
                fileFormat = args[2].toLowerCase();
                headless = Boolean.parseBoolean(args[3]);
            } catch (Exception e) {
                log.error("Invalid command line arguments. Using default values.");
                closetUrl = DEFAULT_CLOSET_URL;
                productCount = DEFAULT_PRODUCT_COUNT;
                fileFormat = DEFAULT_FILE_FORMAT;
                headless = DEFAULT_HEADLESS;
            }
        }else {
            // if no command line arguments are provided, prompt the user
            try {
                log.info("No command line arguments provided. Please provide the following:");
                Scanner scanner = new Scanner(System.in);
                log.info("Enter closet URL (default: {}):", DEFAULT_CLOSET_URL);
                closetUrl = scanner.nextLine().trim().isEmpty() ? DEFAULT_CLOSET_URL : scanner.nextLine();
                log.info("Enter product count (default: {}):", DEFAULT_PRODUCT_COUNT);
                productCount = scanner.nextInt();
                log.info("Enter file format (default: {}):", DEFAULT_FILE_FORMAT);
                fileFormat = scanner.next().toLowerCase();
                log.info("Enter headless mode (default: {}):", DEFAULT_HEADLESS);
                headless = scanner.nextBoolean();
            }catch (Exception e) {
                log.error("Invalid input. Using default values.");
                closetUrl = DEFAULT_CLOSET_URL;
                productCount = DEFAULT_PRODUCT_COUNT;
                fileFormat = DEFAULT_FILE_FORMAT;
                headless = DEFAULT_HEADLESS;
            }
        }

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

        try (PoshmarkScraperImpl service = new PoshmarkScraperImpl(headless)) {
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
