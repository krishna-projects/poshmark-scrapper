package com.scrapper.config;

import com.microsoft.playwright.*;
import com.scrapper.util.ScraperUtility;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.util.List;

@Slf4j
public class PlaywrightConfig implements AutoCloseable {
    private static final String[] USER_AGENTS = {
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.4 Safari/605.1.15",
            "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0",
            "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36"
    };
    private static final int SLOW_MO = 200; // Increased delay between operations
    private static PlaywrightConfig instance;
    private Playwright playwright;

    // Private constructor
    private PlaywrightConfig() {
        this.playwright = Playwright.create();
    }

    // Singleton PlaywrightConfig instance
    public static synchronized PlaywrightConfig getInstance() {
        if (instance == null) {
            instance = new PlaywrightConfig();
        }
        return instance;
    }

    public String getRandomUserAgent() {
        return USER_AGENTS[(int) (Math.random() * USER_AGENTS.length)];
    }

    /**
     * Creates and configures a new Playwright browser instance
     *
     * @return Configured Browser instance
     */
    public Browser createBrowser(boolean headless) {
        return playwright.chromium()
                .launch(new BrowserType.LaunchOptions()
                        .setHeadless(headless)
                        .setSlowMo(SLOW_MO)
                        .setArgs(List.of(
                                "--disable-blink-features=AutomationControlled",  // Hide automation flags
                                "--window-size=1920,1080"
                        )));
    }

    /**
     * Creates a new browser context with default settings
     *
     * @param browser Browser instance to create context from
     * @return Configured BrowserContext
     */
    public BrowserContext createBrowserContext(Browser browser) {
        // Generate a random viewport size to appear more human-like
        int width = 1280 + (int) (Math.random() * 500);  // 1280-1780
        int height = 800 + (int) (Math.random() * 500);  // 800-1300

        // Create context with more human-like settings
        BrowserContext context = browser.newContext(new Browser.NewContextOptions()
                .setViewportSize(width, height)
                .setUserAgent(getRandomUserAgent())
                .setLocale("en-US,en;q=0.9")
                .setTimezoneId("America/New_York")
                .setPermissions(List.of("geolocation"))
                .setIsMobile(false)
                .setHasTouch(false)
                .setJavaScriptEnabled(true)
                .setHttpCredentials(null)
                .setIgnoreHTTPSErrors(true)
        );

        // Add common headers
        context.setDefaultNavigationTimeout(60000);
        context.setDefaultTimeout(30000);

        // Add common headers to make requests look more like a real browser
        context.setExtraHTTPHeaders(java.util.Map.of(
                "Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,*/*;q=0.8",
                "Accept-Language", "en-US,en;q=0.5",
                "Accept-Encoding", "gzip, deflate, br",
                "Connection", "keep-alive",
                "Upgrade-Insecure-Requests", "1",
                "Sec-Fetch-Dest", "document",
                "Sec-Fetch-Mode", "navigate",
                "Sec-Fetch-Site", "same-origin",
                "Cache-Control", "max-age=0"
        ));

        return context;
    }

    /**
     * Creates a new page with default timeout settings
     *
     * @param context BrowserContext to create page from
     * @return New Page instance
     */
    public Page createPage(BrowserContext context) {
        Page page = context.newPage();
        // Set default navigation timeout (30 seconds)
        page.setDefaultNavigationTimeout(30000);
        // Set default timeout for other operations (10 seconds)
        page.setDefaultTimeout(10000);
        return page;
    }

    // Method to fetch a product page using JSoup with retry logic
    public Document getJsoupDocument(String productUrl) throws IOException {
        final int MAX_RETRIES = 3;

        Exception lastException = null;

        for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
            try {
                if (attempt > 1) {
                    // Add exponential backoff with jitter
                    log.warn("Retry attempt {}/{} ...", attempt, MAX_RETRIES);
                    ScraperUtility.randomSleep(2, 7);
                }

                return Jsoup.connect(productUrl)
                        .userAgent(getRandomUserAgent())
                        .timeout(10000 + (int) (Math.random() * 5000)) // Random timeout between 10-15s
                        .header("accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/avif,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.7")
                        .header("accept-language", "en-US,en;q=0.9,hi;q=0.8")
                        .header("priority", "u=0, i")
                        .header("sec-ch-ua", "\"Not;A=Brand\";v=\"99\", \"Google Chrome\";v=\"139\", \"Chromium\";v=\"139\"")
                        .header("sec-ch-ua-mobile", "?0")
                        .header("sec-ch-ua-platform", "\"macOS\"")
                        .header("sec-fetch-dest", "document")
                        .header("sec-fetch-mode", "navigate")
                        .header("sec-fetch-site", "none")
                        .header("sec-fetch-user", "?1")
                        .header("upgrade-insecure-requests", "1")
                        .header("Cookie", "_csrf=Es9WIOgVYOA7c9PG5Kv88BBk; esid=sift%3A68a08d0b28eb2753945f5a19; ps=%7B%22bid%22%3A%2268a08d0b28eb2753945f5a18%22%2C%22extvid%22%3A%22ext1%3A78674df7-a237-4534-b2e3-48aa6d391e21%22%7D; vsegv3=eyJsMDEiOiIwNDQiLCJsMDIiOiIwNjUiLCJsMDMiOiIxMjgiLCJsMDQiOiIxMDkiLCJsMDUiOiIwNTEiLCJsMDYiOiIwMzUiLCJsMDciOiIwMDQiLCJsMDgiOiIxMjMifQ%3D%3D")
                        .get();
            } catch (Exception e) {
                lastException = e;
                if (attempt < MAX_RETRIES) {
                    log.warn("Attempt {}/{} failed for URL: {}", attempt, MAX_RETRIES, productUrl);
                }
            }
        }

        // If we get here, all retries failed
        log.error("All {} attempts failed for URL: {}", MAX_RETRIES, productUrl);
        throw new IOException("Failed to fetch document after " + MAX_RETRIES + " attempts", lastException);
    }

    /**
     * Closes the Playwright instance and releases all resources.
     * This method should be called when the Playwright instance is no longer needed.
     */
    @Override
    public void close() {
        try {
            if (playwright != null) {
                playwright.close();
                playwright = null;
                instance = null;
                log.info("Playwright instance closed successfully");
            }
        } catch (Exception e) {
            log.error("Error closing Playwright: {}", e.getMessage(), e);
        }
    }
}
