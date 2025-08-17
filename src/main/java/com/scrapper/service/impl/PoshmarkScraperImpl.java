package com.scrapper.service.impl;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.ElementHandle;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitUntilState;
import com.scrapper.config.PlaywrightConfig;
import com.scrapper.model.Product;
import com.scrapper.model.ScrapingSummary;
import com.scrapper.service.PostmarkScraperService;
import com.scrapper.util.ScraperUtility;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
public class PoshmarkScraperImpl implements PostmarkScraperService, AutoCloseable {
    private final BrowserContext context;
    private final Browser browser;
    private final PlaywrightConfig playwrightConfig;
    private final ScrapingSummary summary = new ScrapingSummary();

    public PoshmarkScraperImpl(boolean headless) {
        this.playwrightConfig = PlaywrightConfig.getInstance();
        this.browser = playwrightConfig.createBrowser(headless);
        this.context = playwrightConfig.createBrowserContext(browser);
    }

    @Override
    public Set<String> getProductUrls(String closetUrl, int productCount) {
        // if no product count provided, scrape as many as possible
        summary.start();
        productCount = productCount > 0 ? productCount : Integer.MAX_VALUE;
        Set<String> productUrls = new HashSet<>();

        // Create a new page
        Page page = playwrightConfig.createPage(context);

        // Navigate to the closet page
        log.info("Navigating to: {}", closetUrl);
        page.navigate(closetUrl);

        // Wait for the product grid to load
        page.waitForSelector("div.tiles_container > div");

        while (true) {
            // Get current set of product cards
            List<ElementHandle> productCards = page.querySelectorAll("div.tiles_container > div");
            int previousSize = productUrls.size();

            // Process newly loaded products
            for (ElementHandle productCard : productCards) {
                try {
                    String productUrl = "https://poshmark.com" + productCard.querySelector("div.card.card--small > a")
                            .getAttribute("href");
                    if (productUrls.add(productUrl)) { // Only process new URLs
                        log.info("Extracted link: {} (Total: {}/{})",
                                productUrl, productUrls.size(), productCount);
                        if (productUrls.size() >= productCount) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.error("Error extracting product: {}", e.getMessage());
                }
            }
            // Check if we've reached our target or if no new products were found
            if (productUrls.size() >= productCount) {
                break;
            }
            // Check if we've reached the end or no new products were found
            if (previousSize == productUrls.size()) {
                log.info("No new products found in the last scroll. Ending scroll.");
                break;
            }

            // Scroll to load more products
            page.evaluate("window.scrollTo(0, document.body.scrollHeight);");

            // Wait for new content to load
            page.waitForTimeout(2000);

        }
        log.info("Found {} products after scrolling", productUrls.size());
        return productUrls;
    }


    @Override
    public List<Product> scrapeProducts(Set<String> productUrls) {
        log.info("Scraping {} products", productUrls.size());
        List<Product> products = new ArrayList<>();
        if (productUrls.isEmpty()) {
            return products;
        }

        // Create a new page for each batch of products to avoid detection
        Page page = null;
        int processed = 0;
        final int MAX_PRODUCTS_PER_SESSION = 10;

        try {
            for (String productUrl : productUrls) {
                try {
                    // Create a new page every MAX_PRODUCTS_PER_SESSION products
                    if (page == null || processed >= MAX_PRODUCTS_PER_SESSION) {
                        if (page != null) {
                            page.close();
                        }
                        page = playwrightConfig.createPage(context);
                        processed = 0;
                        // Add a longer delay when creating a new page
                        ScraperUtility.randomSleep(5, 10);
                    }

                    log.info("Scraping product: {}", productUrl);

                    // Random delay before navigation
                    ScraperUtility.randomSleep(2, 4);

                    // Navigate to the product page
                    page.navigate(productUrl, new Page.NavigateOptions()
                            .setWaitUntil(WaitUntilState.DOMCONTENTLOADED));

                    // Wait for the page to load
                    page.waitForSelector("h1", new Page.WaitForSelectorOptions()
                            .setTimeout(15000));

                    // Human-like scroll through the page
                    humanScroll(page, 300, 800, 2);

                    // Extract product details with null checks
                    String title = "";
                    try {
                        ElementHandle titleElement = page.querySelector("h1");
                        if (titleElement != null) {
                            title = titleElement.textContent().trim();
                        }
                    } catch (Exception e) {
                        log.error("Error extracting title for {}: {}", productUrl, e.getMessage());
                    }

                    // Add more product details here as needed

                    products.add(Product.builder()
                            .productId(ScraperUtility.extractProductIdFromUrl(productUrl))
                            .productTitle(title)
                            .productUrl(productUrl)
                            .build());

                    processed++;

                    // Random delay after processing
                    ScraperUtility.randomSleep(2, 4);

                } catch (Exception e) {
                    log.error("Error processing product {}: {}", productUrl, e.getMessage());
                    // Continue with next product on error
                }
            }

        } catch (Exception e) {
            log.error("Error in scrapeProducts: {}", e.getMessage());
        } finally {
            if (page != null) {
                page.close();
            }
        }

        log.info("Successfully scraped {} out of {} products", products.size(), productUrls.size());
        return products;
    }


    private void humanScroll(Page page, int minScroll, int maxScroll, int scrolls) {
        for (int i = 0; i < scrolls; i++) {
            int scrollAmount = minScroll + (int) (Math.random() * (maxScroll - minScroll));
            page.evaluate("window.scrollBy(0, " + scrollAmount + ");");
            ScraperUtility.randomSleep(2, 3);
        }
    }

    /**
     * Saves the summary report to a file
     */
    private void saveSummaryToFile() {
        try {
            String summaryReport = summary.generateReport();
            Path summaryPath = Paths.get("scraping_summary.txt");
            Files.writeString(summaryPath, summaryReport);
            log.debug("Scraping summary saved to: {}", summaryPath.toAbsolutePath());
        } catch (Exception e) {
            log.error("Failed to save scraping summary: {}", e.getMessage());
        }
    }

    @Override
    public void close() {
        log.info("Closing browser resources");
        try {
            if (context != null) {
                context.close();
            }
            if (browser != null) {
                browser.close();
            }
            if (playwrightConfig != null) {
                playwrightConfig.close();
            }
        } catch (Exception e) {
            log.warn("Error closing browser resources: {}", e.getMessage());
        }
    }

    /**
     * Scrapes product details using Jsoup (faster than Playwright for simple pages)
     *
     * @param productUrls URL of the product to scrape
     * @return Product object with scraped details
     */
    public List<Product> scrapeWithJsoup(Set<String> productUrls) {
        // Initialize summary
        summary.setTotalProducts(productUrls.size());
        summary.setTotalProducts(productUrls.size());
        // Add random delay to avoid being blocked
        ScraperUtility.randomSleep(2, 4);
        List<Product> products = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger counter = new AtomicInteger(1);
        int totalProducts = productUrls.size();

        productUrls.parallelStream().forEach(productUrl -> {
            int currentIndex = counter.getAndIncrement();
            log.info("Processing product {}/{}: {}", currentIndex, totalProducts, productUrl);
            ScraperUtility.randomSleep(2, 4);
            try {
                // Connect with headers to mimic a real browser request
                Document doc = playwrightConfig.getJsoupDocument(productUrl);

                // Extract product details
                String productId = ScraperUtility.extractProductIdFromUrl(productUrl);
                String productTitle = ScraperUtility.getElementText(doc, "h1.listing__title-container");
                String brandName = ScraperUtility.getElementText(doc, "a.listing__brand");
                String price = ScraperUtility.getElementText(doc, "span.m--l--2");
                String discountedPrice = ScraperUtility.getElementText(doc, "p.h1").split(" ")[0];
                String size = ScraperUtility.getElementText(doc, "button.size-selector__size-option");
                List<String> colors = ScraperUtility.getListElementText(doc, "div.m--r--7:nth-child(2) > div");
                String description = ScraperUtility.getElementText(doc.selectFirst("div.listing__description"));
                List<String> categories = ScraperUtility.getListElementText(doc, "div.m--r--7:nth-child(1) > div");
                List<String> imageUrls = ScraperUtility.getImageUrls(doc);
                String sellerUsername = ScraperUtility.getElementText(doc, ".listing__header-container .d--fl > .d--fl a");
                String listingDate = ScraperUtility.getElementText(doc, ".timestamp");

                // Build and return product
                Product product = Product.builder()
                        .productId(ScraperUtility.extractProductIdFromUrl(productId))
                        .productTitle(productTitle)
                        .brandName(brandName)
                        .price(price)
                        .discountedPrice(discountedPrice)
                        .size(size)
                        .colors(colors)
                        .categories(categories)
                        .description(description)
                        .productUrl(productUrl)
                        .imageUrls(imageUrls)
                        .sellerUsername(sellerUsername)
                        .listingDate(listingDate)
                        .build();
                products.add(product);
                summary.addSuccessfulProduct(productUrl);
            } catch (Exception e) {
                String errorMsg = String.format("Error processing product %s: %s", 
                    productUrl, e.getMessage());
                summary.addFailedProduct(productUrl, e.getMessage());
                log.error("Error processing {}/{} ({}): {}",
                        currentIndex, totalProducts, productUrl, errorMsg);
            }
        });
        
        // Finalize summary
        summary.end();
        saveSummaryToFile();
        log.info("Completed processing {}/{} products", totalProducts, totalProducts);
        log.info("Scraping summary has been saved to 'scraping_summary.txt'");
        return products;
    }


}