package com.scrapper.util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.scrapper.model.Product;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Slf4j
@UtilityClass
public class JsonUtils {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    public static String toJson(Object object) {
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("Error converting object to JSON: {}", e.getMessage());
            return "{}";
        }
    }

    public static String saveResultsToFile(List<Product> products, String format) {
        if (products == null || products.isEmpty()) {
            log.warn("No products provided to save");
            return "";
        }

        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = String.format("poshmark_products_%s.%s", timestamp, format.toLowerCase());
        Path outputPath = Paths.get(fileName);

        try {
            if ("csv".equalsIgnoreCase(format)) {
                try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
                    // Write CSV header
                    writer.write("productId,productTitle,brandName,price,discountedPrice,size,colors,categories,description,productUrl,sellerUsername,listingDate\n");

                    // Write each product as a CSV row
                    for (Product product : products) {
                        String colors = product.getColors() != null ? String.join(",", product.getColors()) : "";
                        String categories = product.getCategories() != null ? String.join(",", product.getCategories()) : "";
                        String images = product.getImageUrls() != null ? String.join(",", product.getImageUrls()) : "";

                        String row = String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                                escapeCsvField(product.getProductId()),
                                escapeCsvField(product.getProductTitle()),
                                escapeCsvField(product.getBrandName()),
                                escapeCsvField(product.getPrice()),
                                escapeCsvField(product.getDiscountedPrice()),
                                escapeCsvField(product.getSize()),
                                escapeCsvField(colors),
                                escapeCsvField(categories),
                                escapeCsvField(product.getDescription()),
                                escapeCsvField(product.getProductUrl()),
                                escapeCsvField(images),
                                escapeCsvField(product.getSellerUsername()),
                                escapeCsvField(product.getListingDate())
                        );
                        writer.write(row);
                    }
                    log.info("Successfully saved {} products to CSV file: {}", products.size(), outputPath);
                    return outputPath.toAbsolutePath().toString();
                }
            } else {
                String json = toJson(products);
                Files.writeString(outputPath, json);
                log.info("Successfully saved {} products to JSON file: {}", products.size(), outputPath);
                return outputPath.toAbsolutePath().toString();
            }
        } catch (IOException e) {
            log.error("Error writing to {} file: {}", format, e.getMessage());
        }
        return "";
    }

    private static String escapeCsvField(String field) {
        if (field == null) {
            return "";
        }
        // Escape double quotes by doubling them
        String escaped = field.replace("\"", "\"\"");
        // If the field contains commas, newlines or double quotes, wrap it in double quotes
        if (escaped.contains(",") || escaped.contains("\n") || escaped.contains("\"")) {
            return "\"" + escaped + "\"";
        }
        return escaped;
    }
}
