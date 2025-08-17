# Poshmark Scraper

A Java-based web scraper for extracting product information from Poshmark closets using Playwright and Jsoup.

## Features

- Extract product URLs from Poshmark closets
- Scrape detailed product information including:
  - Product title, brand, and description
  - Pricing information (original and discounted)
  - Size, colors, and categories
  - Product images
  - Seller information and listing date
- Parallel processing for improved performance
- Detailed scraping summary with success/failure reports
- Configurable output formats (JSON/CSV)
- Interactive command-line interface with sensible defaults
- Progress tracking during scraping

## Setup Instructions

### Prerequisites

- Java 8 or higher
- Maven 3.6.0 or higher
- Chrome or Firefox browser installed (for Playwright)

### Installation

1. Clone the repository:
   ```bash
   git clone https://github.com/krishna-projects/poshmark-scrapper.git
   cd poshmark-scrapper
   ```

2. Build the project:
   ```bash
   mvn clean install
   ```
   This will create an executable JAR file in the `target` directory named `poshmark-scrapper-1.0-SNAPSHOT-jar-with-dependencies.jar`

### Running the Application

#### Option 1: Using Maven (Interactive Mode)
```bash
mvn exec:java -Dexec.mainClass="com.scrapper.PoshmarkScrapper"
```
This will launch the scraper in interactive mode, prompting you for any required information.

#### Option 2: Using Executable JAR (Recommended)
```bash
# Basic usage (User input / if no parameters are provided, uses all defaults)
java -jar target/poshmark-scrapper-1.0-SNAPSHOT-jar-with-dependencies.jar

# With custom parameters (positional arguments)
java -jar target/poshmark-scrapper-1.0-SNAPSHOT-jar-with-dependencies.jar "https://poshmark.com/closet/peechypies?availability=available"  20  json true

# Parameter breakdown (all parameters are optional and will use defaults if not provided):
# 1. Poshmark closet URL (default: https://poshmark.com/closet/peechypies?availability=available)
# 2. Number of products to scrape (default: 10)
# 3. Output format: json or csv (default: json)
# 4. Headless mode: true or false (default: true)
```

#### Interactive Mode
If you run the JAR without parameters, the application will prompt you for any missing information:

1. First, it will ask for the Poshmark closet URL
2. Then, it will ask how many products to scrape
3. Next, it will ask for the output format (json/csv)
4. Finally, it will ask if you want to run in headless mode (true/false)

Default values are shown in square brackets and will be used if you press Enter without typing anything.

> **Note:** The first time you run the JAR, it will download the required browser binaries for Playwright.

### Default Values

| Setting | Default Value |
|---------|---------------|
| Closet URL | https://poshmark.com/closet/peechypies?availability=available |
| Product Count | 10 |
| Output Format | json |
| Headless Mode | true |

### Output Files

The scraper generates two output files:

1. `scraping_summary.txt` - Contains detailed statistics about the scraping process
2. `poshmark_products_[timestamp].[json/csv]` - Contains the scraped product data

> **Note:** The first time you run the JAR, it will download the required browser binaries for Playwright.


## Dependencies

### Core Dependencies
- Java 17+
- Maven 3.6.0+
- Playwright for Java
- Jsoup (HTML parsing)
- Lombok (reduces boilerplate code)
- SLF4J (logging)

## Known Limitations

1. **Rate Limiting**: Poshmark may block or rate limit requests if too many are made in a short period. The scraper includes random delays to mitigate this.

2. **Dynamic Content**: Some content is loaded dynamically with JavaScript. The scraper uses Playwright to handle this, but may require updates if Poshmark changes their frontend.

3. **Structure Changes**: If Poshmark changes their HTML structure, the selectors in the code may need to be updated.

## Output

The scraper generates a detailed summary in `scraping_summary.txt` and saves the scraped data in the specified format (JSON/CSV).

### json Output

```json
{
  "metadata" : {
    "source_url" : "https://poshmark.com/closet/peechypies?availability=available",
    "total_products" : 10,
    "scrape_date" : "2025-08-17T12:40:53.570196Z",
    "execution_time_seconds" : 32.936
  },
  "products" : [ {
    "productId" : "Lululemon-winder-train-ribbed-leggings-687eea51bd4ce30d24103ddf",
    "productTitle" : "Lululemon winder train ribbed leggings",
    "brandName" : "lululemon athletica",
    "price" : "$118",
    "discountedPrice" : "$50",
    "size" : "4",
    "colors" : [ "Black" ],
    "categories" : [ "Women", "Pants & Jumpsuits", "Leggings" ],
    "description" : "Lululemon winder train ribbed leggings Size 4 Excellent gently used condition Black",
    "productUrl" : "https://poshmark.com/listing/Lululemon-winder-train-ribbed-leggings-687eea51bd4ce30d24103ddf",
    "imageUrls" : [ "https://di2ponv0v5otw.cloudfront.net/posts/2025/07/21/687eea51bd4ce30d24103ddf/s_687eea64b944910c96911daa.jpg", "https://di2ponv0v5otw.cloudfront.net/posts/2025/07/21/687eea51bd4ce30d24103ddf/s_687eea6521b801d394e9a1bd.jpg", "https://di2ponv0v5otw.cloudfront.net/posts/2025/07/21/687eea51bd4ce30d24103ddf/s_687eea6505deb3ac05422486.jpg", "https://di2ponv0v5otw.cloudfront.net/posts/2025/07/21/687eea51bd4ce30d24103ddf/s_687eea669f19e269e4267852.jpg" ],
    "sellerUsername" : "peechypies",
    "listingDate" : "Updated 7 mins ago"
  }, {
    "productId" : "Lululemon-Mens-Pacemaker-Shorts-687eeb80006e43664ffe5d58",
    "productTitle" : "Lululemon Men’s Pacemaker Shorts",
    "brandName" : "lululemon athletica",
    "price" : "$98",
    "discountedPrice" : "$38",
    "size" : "L",
    "colors" : [ "Black", "Gray" ],
    "categories" : [ "Men", "Shorts", "Athletic" ],
    "description" : "Lululemon Men’s Pacemaker Shorts Size men’s Large Gently used condition Some fraying in crotch shown",
    "productUrl" : "https://poshmark.com/listing/Lululemon-Mens-Pacemaker-Shorts-687eeb80006e43664ffe5d58",
    "imageUrls" : [ "https://di2ponv0v5otw.cloudfront.net/posts/2025/07/21/687eeb80006e43664ffe5d58/s_687eeb92a0dc2b3251266953.jpg", "https://di2ponv0v5otw.cloudfront.net/posts/2025/07/21/687eeb80006e43664ffe5d58/s_687eeb939b2158781a0f117f.jpg", "https://di2ponv0v5otw.cloudfront.net/posts/2025/07/21/687eeb80006e43664ffe5d58/s_687eeb9324d2e744cfd0a1a2.jpg", "https://di2ponv0v5otw.cloudfront.net/posts/2025/07/21/687eeb80006e43664ffe5d58/s_687eeb9449e17b58b99b3a99.jpg", "https://di2ponv0v5otw.cloudfront.net/posts/2025/07/21/687eeb80006e43664ffe5d58/s_687eeb952326c8dd5bffaf86.jpg", "https://di2ponv0v5otw.cloudfront.net/posts/2025/07/21/687eeb80006e43664ffe5d58/s_687eeb96b7f0ac2e8b77c7b1.jpg" ],
    "sellerUsername" : "peechypies",
    "listingDate" : "Updated 7 mins ago"
  }, {
    "productId" : "Carhartt-Woodside-Beanie-687eed5905deb3af86423fed",
    "productTitle" : "Carhartt Woodside Beanie",
    "brandName" : "Carhartt",
    "price" : "$39",
    "discountedPrice" : "$21",
    "size" : "OS",
    "colors" : [ "Tan" ],
    "categories" : [ "Men", "Accessories", "Hats" ],
    "description" : "Carhartt Woodside Beanie Gently used condition Bison colorway",
    "productUrl" : "https://poshmark.com/listing/Carhartt-Woodside-Beanie-687eed5905deb3af86423fed",
    "imageUrls" : [ "https://di2ponv0v5otw.cloudfront.net/posts/2025/07/21/687eed5905deb3af86423fed/s_687eed67850012540c0870a6.jpg", "https://di2ponv0v5otw.cloudfront.net/posts/2025/07/21/687eed5905deb3af86423fed/s_687eed68bd4ce31a16105a40.jpg", "https://di2ponv0v5otw.cloudfront.net/posts/2025/07/21/687eed5905deb3af86423fed/s_687eed68310f1cdd1ede6c95.jpg" ],
    "sellerUsername" : "peechypies",
    "listingDate" : "Updated 7 mins ago"
  } ]
}
```

### CSV Output

| Product ID | Product Title | Brand | Price | Discounted Price | Size | Color | Category | Description | Product URL | Seller | Listed |
|------------|---------------|-------|-------|------------------|------|-------|-----------|-------------|-------------|--------|--------|
| `Sperry-Livingston-Mens-Sandals-687eec11006e43947efe627a` | Sperry Livingston Men's Sandals | - | - | $30 | 12 | Black | Men > Shoes > Sandals & Flip-Flops | Sperry Livingston Men's Sandals Size men's 12 Excellent gently used condition | https://poshmark.com/listing/Sperry-Livingston-Mens-Sandals-687eec11006e43947efe627a | speechypies | Updated a minute ago |
| `Lululemon-winder-train-ribbed-leggings-687eea51bd4ce30d24103ddf` | Lululemon winder train ribbed leggings | lululemon athletica | $118 | $50 | 4 | Black | Women > Pants & Jumpsuits > Leggings | Lululemon winder train ribbed leggings Size 4 Excellent gently used condition Black | https://poshmark.com/listing/Lululemon-winder-train-ribbed-leggings-687eea51bd4ce30d24103ddf | speechypies | Updated a minute ago |

> **Note:** 
> - The actual CSV file will contain the full URLs for images
> - Categories are pipe-separated in the actual CSV output
> - The table above shows a more readable representation of the data

### Sample Summary Output
```
=== Scraping Summary ===

Start Time: 2025-08-17T14:30:45.123
End Time: 2025-08-17T14:32:10.456
Duration: 1 minute 25 seconds
Average time per product: 4.25 seconds

Total Products: 10
Successfully Scraped: 9
Failed: 1

=== Successful Products ===
-  https://poshmark.com/listing/Psycho-Bunny-Kids-Sweatshirt-Hoodie-689ab4c924b20ba5dab1bdd9
-  https://poshmark.com/listing/Soma-lightly-lined-perfect-coverage-bra-687eecb2ff03208219004575

=== Failed Products ===
- https://poshmark.com/listing/SoftSurroundingsStraight-leg-pull-on-pants-6556c52b6da06ecd4acb80d1 - Error: java.lang.NullPointerException
```

