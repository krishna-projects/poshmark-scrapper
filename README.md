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

#### Option 1: Using Maven
```bash
mvn exec:java -Dexec.mainClass="com.scrapper.PoshmarkScrapper"
```

#### Option 2: Using Executable JAR (Recommended)
```bash
# Basic usage
java -jar target/poshmark-scrapper-1.0-SNAPSHOT-jar-with-dependencies.jar

# With custom parameters
java -jar target/poshmark-scrapper-1.0-SNAPSHOT-jar-with-dependencies.jar "https://poshmark.com/closet/peechypies?availability=available" 20 json false

parameter 1 - page url -> "https://poshmark.com/closet/peechypies?availability=available"
parameter 2 - product count -> 20
parameter 3 - file format -> json / csv
parameter 4 - headless -> true / false 
```

> **Note:** The first time you run the JAR, it will download the required browser binaries for Playwright.

### Available Command Line Arguments

| Parameter    | Description | Default |
|--------------|-------------|---------|
| `--url`      | Poshmark closet URL | https://poshmark.com/closet/peechypies?availability=available |
| `--count`    | Number of products to scrape | 10 |
| `--format`   | Output format (json/csv) | json |
| `--headless` | Run browser in headless mode | false |

### Building a Standalone JAR

To create a new JAR file with all dependencies:

```bash
mvn clean package
```

The JAR file will be created at:
```
target/poshmark-scrapper-1.0-SNAPSHOT-jar-with-dependencies.jar
```

You can then distribute and run this JAR on any system with Java 8+ installed.

## Dependencies

### Core Dependencies
- Java 8+
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

