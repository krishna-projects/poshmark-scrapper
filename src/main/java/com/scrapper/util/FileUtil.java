package com.scrapper.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.scrapper.model.Product;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@UtilityClass
public class FileUtil {
    private static final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT);

    // Save results to file
    public static String saveResultsToFile(List<Product> products, String format, double executionTimeSeconds, String sourceUrl) {
        if (products == null || products.isEmpty()) {
            log.warn("No products provided to save");
            return "";
        }

        String timestamp = String.valueOf(System.currentTimeMillis());
        String fileName = String.format("poshmark_products_%s.%s", timestamp, format.toLowerCase());
        Path outputPath = Paths.get(fileName);

        try {
            if ("csv".equalsIgnoreCase(format)) {
                // Save as CSV
                return saveAsCsv(products, outputPath);
            } else {
                // Create metadata
                Map<String, Object> metadata = new LinkedHashMap<>();
                metadata.put("source_url", sourceUrl);
                metadata.put("total_products", products.size());
                metadata.put("scrape_date", Instant.now().toString());
                metadata.put("execution_time_seconds", executionTimeSeconds);


                // Create root object
                ObjectNode data = objectMapper.createObjectNode();
                data.set("metadata", objectMapper.valueToTree(metadata));
                data.set("products", objectMapper.valueToTree(products));

                // Write to file with pretty print
                String json = objectMapper.writerWithDefaultPrettyPrinter()
                        .writeValueAsString(data);

                Files.writeString(outputPath, json);
                log.info("Successfully saved {} products to JSON file: {}", products.size(), outputPath);
                return outputPath.toAbsolutePath().toString();
            }
        } catch (IOException e) {
            log.error("Error writing to {} file: {}", format, e.getMessage());
            return "";
        }
    }

    // Save as CSV
    private static String saveAsCsv(List<Product> products, Path outputPath) throws IOException {
        try (BufferedWriter writer = Files.newBufferedWriter(outputPath)) {
            // Write CSV header
            writer.write("product_id,product_title,brand_name,price,discounted_price,size,color,category,description,product_url,seller_username,listing_date\n");

            // Write each product as a CSV row
            for (Product product : products) {
                String colors = product.getColors() != null ? String.join("|", product.getColors()) : "";
                String categories = product.getCategories() != null ? String.join("|", product.getCategories()) : "";
                String imageUrls = product.getImageUrls() != null ? String.join("|", product.getImageUrls()) : "";

                String row = String.format("%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s,%s\n",
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
                        escapeCsvField(imageUrls),
                        escapeCsvField(product.getSellerUsername()),
                        escapeCsvField(product.getListingDate())
                );
                writer.write(row);
            }
            log.info("Successfully saved {} products to CSV file: {}", products.size(), outputPath);
            return outputPath.toAbsolutePath().toString();
        }
    }

    // Helper method to escape CSV fields
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
