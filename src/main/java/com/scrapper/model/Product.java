package com.scrapper.model;

import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Data
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private String productId;          // Unique identifier from the URL
    private String productTitle;       // Full name of the product
    private String brandName;          // Brand of the item
    private String price;          // Original listing price
    private String discountedPrice; // Sale price (if applicable)
    private String size;               // Size of the item
    private List<String> colors;              // Color(s) of the item
    private List<String> categories;   // Product categories (may be multiple)
    private String description;        // Full product description
    private String productUrl;         // Direct link to the product page
    private List<String> imageUrls;    // List of all product image URLs
    private String sellerUsername;     // Username of the seller
    private String listingDate; // When the item was listed (if available)
}
