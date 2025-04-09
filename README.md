# Cochrane Library Review Scraper

This Java command-line application scrapes reviews and metadata from the [Cochrane Library](https://www.cochranelibrary.com/cdsr/reviews/topics) by topic.

## 🧩 Features

- Scrapes all reviews under a specific topic (e.g., Allergy & Intolerance)
- Extracts:
  - Review URL
  - Topic
  - Title
  - Authors
  - Publication date
- Writes output to a `cochrane_reviews.txt` file in pipe-delimited format

## 📦 Requirements

- Java 17+
- Maven 3.3+
- Internet connection

## 🚀 How to Run

### 1. Clone the repo

```bash
git clone https://github.com/yourusername/cochrane-scraper.git
cd cochrane-scraper
