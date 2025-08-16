package com.scrapper.config;

import com.microsoft.playwright.*;
import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class PlaywrightConfig {
    private static final String[] USER_AGENTS = {
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36",
        "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.4 Safari/605.1.15",
        "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36 Edg/120.0.0.0"
    };
    private static final int SLOW_MO = 200; // Increased delay between operations
    
    private static String getRandomUserAgent() {
        return USER_AGENTS[(int) (Math.random() * USER_AGENTS.length)];
    }

    /**
     * Creates and configures a new Playwright browser instance
     * @return Configured Browser instance
     */
    public static Browser createBrowser(boolean headless) {
        return Playwright.create()
                .chromium()
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
     * @param browser Browser instance to create context from
     * @return Configured BrowserContext
     */
    public static BrowserContext createBrowserContext(Browser browser) {
        // Generate a random viewport size to appear more human-like
        int width = 1280 + (int)(Math.random() * 500);  // 1280-1780
        int height = 800 + (int)(Math.random() * 500);  // 800-1300
        
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
     * @param context BrowserContext to create page from
     * @return New Page instance
     */
    public static Page createPage(BrowserContext context) {
        Page page = context.newPage();
        // Set default navigation timeout (30 seconds)
        page.setDefaultNavigationTimeout(30000);
        // Set default timeout for other operations (10 seconds)
        page.setDefaultTimeout(10000);
        return page;
    }
}
