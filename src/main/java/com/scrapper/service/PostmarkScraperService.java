package com.scrapper.service;

import com.scrapper.model.Product;

import java.util.List;
import java.util.Set;

public interface PostmarkScraperService {
    // Returns a set of product URLs to scrape from product grid page
    Set<String> getProductUrls(String closetUrl, int productCount);

    // Returns a list of Product objects scraped from product URLs
    List<Product> scrapeProducts(Set<String> productUrls);
}
