package utils;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WaitUtils {
    private final WebDriver driver;
    private final WebDriverWait wait;

    public WaitUtils(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(25));
    }

    public WebElement waitForVisible(By locator) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
    }

    public WebElement waitForClickable(By locator) {
        return wait.until(ExpectedConditions.elementToBeClickable(locator));
    }

    public void scrollIntoView(WebElement el) {
        try {
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center', inline:'nearest'});", el);
        } catch (Exception ignored) {}
    }

    /** New: scroll the viewport by a vertical delta (positive = down, negative = up) */
    public void scrollBy(int pixelsY) {
        try {
            ((JavascriptExecutor) driver).executeScript("window.scrollBy(0, arguments[0]);", pixelsY);
        } catch (Exception ignored) {}
    }

    /** New: scroll to the very bottom of the page */
    public void scrollToBottom() {
        try {
            ((JavascriptExecutor) driver).executeScript("window.scrollTo(0, document.body.scrollHeight);");
        } catch (Exception ignored) {}
    }

    // Click an element located by 'locator' with waits + JS fallback
    public void safeClick(By locator) {
        WebElement el = null;
        try {
            el = wait.until(ExpectedConditions.elementToBeClickable(locator));
            scrollIntoView(el);
            el.click();
        } catch (Exception clickErr) {
            // Retry with JS if a sticky overlay or animation intercepts
            try {
                if (el == null) {
                    el = wait.until(ExpectedConditions.presenceOfElementLocated(locator));
                    scrollIntoView(el);
                }
                ((JavascriptExecutor) driver).executeScript("arguments[0].click();", el);
            } catch (Exception jsErr) {
                throw new TimeoutException("safeClick failed for: " + locator, jsErr);
            }
        }
    }

    // (Optional) Type text with visibility wait; useful across pages
    public void safeType(By locator, CharSequence... text) {
        WebElement el = wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        scrollIntoView(el);
        try { el.clear(); } catch (Exception ignored) {}
        el.sendKeys(text);
    }

    /** Optional: small sleep utility if you prefer not to litter tests with Thread.sleep */
    public void pause(long millis) {
        try { Thread.sleep(millis); } catch (InterruptedException ignored) {}
    }
}