package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.WaitUtils;

import java.time.Duration;
import java.util.List;

public class SearchResultsPage {
    private final WebDriver driver;
    private final WaitUtils wait;
    private final WebDriverWait shortWait;

    public SearchResultsPage(WebDriver driver){
        this.driver = driver;
        this.wait = new WaitUtils(driver);
        this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(6));
    }

    // Filters launcher on responsive layouts
    private final By filtersLauncher = By.xpath(
            "//*[self::button or self::a or self::div]" +
                    "[contains(translate(normalize-space(.), 'abcdefghijklmnopqrstuvwxyz', 'ABCDEFGHIJKLMNOPQRSTUVWXYZ'), 'FILTERS')]"
    );

    // Header text of the Price facet (sidebar or drawer)
    private final By priceHeader = By.xpath(
            "//*[self::div or self::span or self::p or self::h6 or self::h5][normalize-space(.)='Price']"
    );

    private final By productCard = By.cssSelector("a[href*='/p/'], a[href*='/product/']");

    private WebElement getPriceFacetContainer(WebElement header){
        try {
            return header.findElement(By.xpath(
                    "ancestor::*[contains(@class,'facet') or contains(@class,'accordion') " +
                            "or contains(@class,'Filter') or contains(@class,'filter')][1]"
            ));
        } catch (NoSuchElementException ignored) {
            // fallback: nearest block
            return header.findElement(By.xpath("ancestor::div[1]"));
        }
    }

    private void ensureResultsVisible(){
        new WebDriverWait(driver, Duration.ofSeconds(20))
                .until(ExpectedConditions.presenceOfElementLocated(productCard));
    }

    private void openFiltersIfDrawer(){
        try {
            new WebDriverWait(driver, Duration.ofSeconds(2))
                    .until(ExpectedConditions.presenceOfElementLocated(priceHeader));
        } catch (TimeoutException e) {
            try {
                WebElement launcher = new WebDriverWait(driver, Duration.ofSeconds(3))
                        .until(ExpectedConditions.elementToBeClickable(filtersLauncher));
                launcher.click();
            } catch (Exception ignored) {}
        }
    }

    /** Sets the max price and clicks the right-arrow apply inside the same Price facet */
    public void setMaxPriceAndApply(int maxPrice){
        ensureResultsVisible();
        openFiltersIfDrawer();

        WebElement header = new WebDriverWait(driver, Duration.ofSeconds(15))
                .until(ExpectedConditions.visibilityOfElementLocated(priceHeader));
        wait.scrollIntoView(header);
        try { header.click(); } catch (Exception ignored) {}

        WebElement facet = getPriceFacetContainer(header);
        wait.scrollIntoView(facet);

        // Visible inputs inside the Price facet; assume the last one is "Maximum"
        List<WebElement> inputs = facet.findElements(By.xpath(
                ".//input[not(@type='hidden') and not(contains(@style,'display:none'))]"
        ));
        if (inputs.isEmpty()) {
            throw new TimeoutException("No inputs found inside Price facet");
        }
        WebElement maxInput = inputs.get(inputs.size() - 1);

        // Type + JS events so frameworks react
        try {
            maxInput.click();
            maxInput.sendKeys(Keys.chord(Keys.CONTROL, "a"));
            maxInput.sendKeys(String.valueOf(maxPrice));
        } catch (Exception ignored) {}
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].value = arguments[1];" +
                            "arguments[0].dispatchEvent(new Event('input', {bubbles:true}));" +
                            "arguments[0].dispatchEvent(new Event('change', {bubbles:true}));",
                    maxInput, String.valueOf(maxPrice)
            );
        } catch (Exception ignored) {}

        // Click the exact apply arrow in the facet
        try {
            WebElement applyArrow = facet.findElement(By.xpath(".//button[@aria-label='right-arrow-icon']"));
            wait.scrollIntoView(applyArrow);
            try { applyArrow.click(); }
            catch (Exception e) { ((JavascriptExecutor)driver).executeScript("arguments[0].click();", applyArrow); }
        } catch (NoSuchElementException e) {
            try { maxInput.sendKeys(Keys.ENTER); } catch (Exception ignored) {}
        }

        new WebDriverWait(driver, Duration.ofSeconds(15)).until(
                ExpectedConditions.or(
                        ExpectedConditions.urlContains("price="),
                        ExpectedConditions.urlContains("range="),
                        ExpectedConditions.urlContains("filters"),
                        ExpectedConditions.visibilityOfElementLocated(productCard)
                )
        );
    }

    /**
     * Expand the Type facet by clicking the EXACT '+' you requested and select 'Open'.
     * User-provided XPath for the Type (+): //*[@id="filter-div"]/div[2]/div[6]/div/div/button
     */
    public void expandTypeAndSelectOpen() {
        ensureResultsVisible();
        openFiltersIfDrawer();

        // 1) Click the exact '+' button for Type
        By typePlusExact = By.xpath("//*[@id='filter-div']/div[2]/div[6]/div/div/button");
        WebElement plus = new WebDriverWait(driver, Duration.ofSeconds(12))
                .until(ExpectedConditions.elementToBeClickable(typePlusExact));
        wait.scrollIntoView(plus);
        try { plus.click(); }
        catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", plus); }

        // 2) Find "Open" and tick it
        WebElement filtersScope;
        try {
            filtersScope = new WebDriverWait(driver, Duration.ofSeconds(5))
                    .until(ExpectedConditions.visibilityOfElementLocated(By.id("filter-div")));
        } catch (TimeoutException te) {
            filtersScope = driver.findElement(By.tagName("body"));
        }

        By openTextNode = By.xpath(
                ".//h6[.//span[contains(translate(., 'ABCDEFGHIJKLMNOPQRSTUVWXYZ\u00A0', 'abcdefghijklmnopqrstuvwxyz '), 'open')] " +
                        "      or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ\u00A0', 'abcdefghijklmnopqrstuvwxyz '), 'open')]" +
                        " | .//*[self::label or self::span]" +
                        "    [contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ\u00A0', 'abcdefghijklmnopqrstuvwxyz '), 'open')]"
        );

        WebElement labelNode = new WebDriverWait(driver, Duration.ofSeconds(12))
                .until(ExpectedConditions.presenceOfNestedElementLocatedBy(filtersScope, openTextNode));

        WebElement input;
        try {
            input = labelNode.findElement(By.xpath("preceding::input[@type='checkbox'][1]"));
        } catch (NoSuchElementException e) {
            input = labelNode.findElement(By.xpath("ancestor::*[contains(@class,'MuiBox-root')][1]//input[@type='checkbox']"));
        }
        wait.scrollIntoView(input);

        try {
            WebElement iconBtn = input.findElement(By.xpath("ancestor::span[contains(@class,'IconButton')][1]"));
            try { iconBtn.click(); }
            catch (Exception ex) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", iconBtn); }
        } catch (NoSuchElementException e) {
            try { input.click(); }
            catch (Exception ex) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", input); }
        }

        // 3) Brief confirmation wait
        try {
            new WebDriverWait(driver, Duration.ofSeconds(8)).until(
                    ExpectedConditions.or(
                            ExpectedConditions.urlContains("type="),
                            ExpectedConditions.urlContains("open"),
                            ExpectedConditions.visibilityOfElementLocated(productCard)
                    )
            );
        } catch (Exception ignored) {}
    }

    /**
     * Open the first product by NAVIGATING to its href instead of clicking.
     * This avoids new tabs / overlays / devtools disconnects and prevents NoSuchSessionException.
     */
    public void openFirstProduct() {
        By firstCardLink = By.cssSelector("a[href*='/p/'], a[href*='/product/']");
        WebElement link = wait.waitForClickable(firstCardLink);

        String href = null;
        try { href = link.getAttribute("href"); } catch (Exception ignored) {}

        if (href != null && !href.isBlank()) {
            driver.navigate().to(href);
        } else {
            try { link.click(); }
            catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", link); }
        }

        // settle
        try { Thread.sleep(400); } catch (InterruptedException ignored) {}

        // sanity wait that we reached a PDP
        try {
            new WebDriverWait(driver, Duration.ofSeconds(10)).until(
                    ExpectedConditions.or(
                            ExpectedConditions.urlContains("/p/"),
                            ExpectedConditions.urlContains("/product/")
                    )
            );
        } catch (Exception ignored) {}
    }
}