# HomeCentre Selenium Java (Cucumber + TestNG)

This project automates the 10-step flow you described using **Selenium**, **TestNG**, **Cucumber (BDD)**, **WebDriverManager**, and a simple **Page Object Model**. It scrapes the **"You may also like"** products on a PDP and saves them to `target/artifacts/you_may_also_like.html`, then proceeds to the **Gifting → Shop Gift Card** journey on Woohoo, enters an invalid email, and saves a screenshot in `target/artifacts/`.

## How to run (IntelliJ or CLI)

### Prerequisites
- JDK 17+
- Maven 3.8+
- Google Chrome installed

### Run from IntelliJ
1. **Open** this folder as a Maven project (`pom.xml`).
2. Wait for dependencies to download.
3. Run the class: `runners.CucumberTestRunner`.

### Run from command line
```bash
mvn -q -Dtest=runners.CucumberTestRunner test
```

## Artifacts
- **HTML**: `target/artifacts/you_may_also_like.html`
- **Screenshot**: `target/artifacts/invalid_email_<timestamp>.png`

## Notes
- Locators are designed to be resilient to minor DOM changes. If the website UI shifts, tweak the XPaths in `pages/`.
- Tests run **headed** by default so you can observe the steps. Switch to headless by adding `options.addArguments("--headless=new");` in `DriverFactory`.
