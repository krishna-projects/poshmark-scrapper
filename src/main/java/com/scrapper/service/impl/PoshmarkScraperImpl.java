package com.scrapper.service.impl;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.*;
import com.microsoft.playwright.options.WaitUntilState;
import com.scrapper.config.PlaywrightConfig;
import com.scrapper.model.Product;
import com.scrapper.service.PostmarkScraperService;
import com.scrapper.util.ScraperUtility;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
public class PoshmarkScraperImpl implements PostmarkScraperService {
    private final BrowserContext context;

    public PoshmarkScraperImpl(boolean headless) {
        Browser browser = PlaywrightConfig.createBrowser(headless);
        this.context = PlaywrightConfig.createBrowserContext(browser);
    }

    @Override
    public Set<String> getProductUrls(String closetUrl, int productCount) {
        // if no product count provided, scrape as many as possible
        productCount = productCount > 0 ? productCount : Integer.MAX_VALUE;
        Set<String> productUrls = new HashSet<>();

        // Initialize browser using PlaywrightConfig
        Page page = PlaywrightConfig.createPage(context);

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
                        log.info("✅ Extracted link: {} (Total: {}/{})",
                                productUrl, productUrls.size(), productCount);
                        if (productUrls.size() >= productCount) {
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.error("❌ Error extracting product: {}", e.getMessage());
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
        productUrls.forEach(log::info);

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
                        page = PlaywrightConfig.createPage(context);
                        processed = 0;
                        // Add a longer delay when creating a new page
                        ScraperUtility.randomSleep(5, 10);
                    }
                    
                    log.info("Scraping product: {}", productUrl);
                    
                    // Random delay before navigation
                    ScraperUtility.randomSleep(2, 5);
                    
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
                    log.error("❌ Error processing product {}: {}", productUrl, e.getMessage());
                    // Continue with next product on error
                }
            }
            
        } catch (Exception e) {
            log.error("❌ Error in scrapeProducts: {}", e.getMessage(), e);
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
            int scrollAmount = minScroll + (int)(Math.random() * (maxScroll - minScroll));
            page.evaluate("window.scrollBy(0, " + scrollAmount + ");");
            ScraperUtility.randomSleep(3, 8);
        }
    }
}
