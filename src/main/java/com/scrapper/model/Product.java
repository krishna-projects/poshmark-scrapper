package com.scrapper.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {
    private String productId;          // Unique identifier from the URL
    private String productTitle;       // Full name of the product
    private String brandName;          // Brand of the item
    private Double price;          // Original listing price
    private Double discountedPrice; // Sale price (if applicable)
    private String size;               // Size of the item
    private String color;              // Color(s) of the item
    private List<String> categories;   // Product categories (may be multiple)
    private String description;        // Full product description
    private String productUrl;         // Direct link to the product page
    private List<String> imageUrls;    // List of all product image URLs
    private String sellerUsername;     // Username of the seller
    private LocalDateTime listingDate; // When the item was listed (if available)
    
    @Override
    public String toString() {
        return "Product{" +
                "productId='" + productId + '\'' +
                ", productTitle='" + productTitle + '\'' +
                ", brandName='" + brandName + '\'' +
                ", price=" + price +
                ", discountedPrice=" + discountedPrice +
                ", size='" + size + '\'' +
                ", color='" + color + '\'' +
                ", categories=" + categories +
                ", description='" + (description != null ? description.substring(0, Math.min(50, description.length())) + "..." : "") + '\'' +
                ", productUrl='" + productUrl + '\'' +
                ", imageUrls=" + (imageUrls != null ? "[" + imageUrls.size() + " images]" : "[]") +
                ", sellerUsername='" + sellerUsername + '\'' +
                ", listingDate=" + listingDate +
                '}';
    }
}
