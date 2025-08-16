package com.scrapper;

import com.scrapper.model.Product;
import com.scrapper.service.impl.PoshmarkScraperImpl;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Set;

@Slf4j
public class PoshmarkScrapper {
    private static final String CLOSET_URL = "https://poshmark.com/closet/peechypies?availability=available";
    private static final int PRODUCT_COUNT = 20;

    public static void main(String[] args) {
        log.info("Starting Poshmark Scraper...");
        PoshmarkScraperImpl service = new PoshmarkScraperImpl(false);
        Set<String> productUrls = service.getProductUrls(CLOSET_URL, PRODUCT_COUNT);
        log.info("Extracted {} links from Poshmark", productUrls.size());
        List<Product> products = service.scrapeProducts(productUrls);
        log.info("Scraped {} products from Poshmark", products.size());
    }

}
