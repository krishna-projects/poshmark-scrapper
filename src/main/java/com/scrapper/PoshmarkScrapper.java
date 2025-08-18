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
        String closetUrl;
        int productCount;
        String fileFormat;
        boolean headless;

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
        } else {
            // if no command line arguments are provided, prompt the user
            try {
                log.info("No command line arguments provided. Please provide the following:");
                Scanner scanner = new Scanner(System.in);

                log.info("Enter closet URL, press enter for default (default: {}):", DEFAULT_CLOSET_URL);
                String input = scanner.nextLine().trim();
                closetUrl = input.isEmpty() ? DEFAULT_CLOSET_URL : input;

                log.info("Enter product count, press enter for default (default: {}):", DEFAULT_PRODUCT_COUNT);
                input = scanner.nextLine().trim();
                productCount = input.isEmpty() ? DEFAULT_PRODUCT_COUNT : Integer.parseInt(input);

                log.info("Enter file format, press enter for default (default: {}):", DEFAULT_FILE_FORMAT);
                input = scanner.nextLine().trim();
                fileFormat = input.isEmpty() ? DEFAULT_FILE_FORMAT : input.toLowerCase();

                log.info("Enter headless mode, press enter for default (default: {}):", DEFAULT_HEADLESS);
                input = scanner.nextLine().trim();
                headless = input.isEmpty() ? DEFAULT_HEADLESS : Boolean.parseBoolean(input);
            } catch (Exception e) {
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

        // create a new instance of PoshmarkScraper with the provided settings
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
