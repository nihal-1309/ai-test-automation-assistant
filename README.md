# AI-Powered Test Automation Assistant

This project is a Spring Boot backend that generates test cases (via OpenAI or fallback), converts them into Java Selenium scripts (artifact files), executes them using Selenium WebDriver, and produces HTML reports via ExtentReports.

**Architecture (text):**
- Input → REST API (Spring Boot)
- AI Service → OpenAI (or fallback sample)
- Script Generator → writes Java source files under `generated-scripts/`
- Test Executor → runs test steps via Selenium (headless Chrome) and creates `reports/*.html`

## Setup

Prerequisites:
- Java 11+
- Maven
- Internet access (for WebDriverManager and optional OpenAI calls)

Configuration:
- Place your OpenAI API key in env var `OPENAI_API_KEY` to enable AI generation. If not present, sample testcases are used.
- `src/main/resources/application.properties` contains basic config.

Build and run:
```bash
mvn clean package
java -jar target/ai-test-automation-assistant-0.0.1-SNAPSHOT.jar
```

Docker:
```bash
# Build image (after running mvn package)
docker build -t ai-test-automation-assistant:latest .
# Run container
docker run -p 8080:8080 --env OPENAI_API_KEY="$OPENAI_API_KEY" ai-test-automation-assistant:latest
```

CI:
- A GitHub Actions workflow `.github/workflows/ci.yml` builds and runs unit tests on push/PR to `main`.

APIs:

- POST /api/generate-testcases
  - Body: optional raw text (URL or scenario)
  - Returns: JSON array of test cases

- POST /api/generate-script
  - Body: TestCase JSON
  - Returns: path to generated Java source file

- POST /api/run-tests
  - Body: list of TestCase JSON
  - Returns: path to generated report HTML

- GET /api/report
  - Returns: path to latest report

Sample curl (generate testcases):
```bash
curl -X POST -H "Content-Type: text/plain" --data "https://example.com" http://localhost:8080/api/generate-testcases
```

Sample run:
1. Generate testcases
2. POST those testcases to `/api/run-tests`
3. Open returned `reports/*.html`

Notes & Limitations:
- Generated Java files are produced in `generated-scripts/` but not compiled by the server. The executor interprets steps and runs them directly.
- Running tests requires Chrome; WebDriverManager downloads the driver automatically.

License: MIT (placeholder)
