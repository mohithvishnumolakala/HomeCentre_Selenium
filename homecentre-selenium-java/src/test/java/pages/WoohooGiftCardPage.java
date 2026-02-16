package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.WaitUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class WoohooGiftCardPage {

    private final WebDriver driver;
    private final WaitUtils wait;
    private final WebDriverWait shortWait;
    private final WebDriverWait midWait;

    // ---- Core form fields (from the HTML you provided) ----
    private final By nameInput      = By.id("name");              // Receiver's Name
    private final By emailInput     = By.id("email");             // Receiver's Email
    private final By mobileInput    = By.id("receiver-mobile");   // Receiver's Mobile Number
    private final By messageTextarea= By.id("message");           // Message for Receiver

    // A generic "No thanks" button that often appears as a marketing/updates popup
    private final By noThanksBtn    = By.xpath(
            "//*[self::button or self::a][contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'no thanks') or " +
                    " contains(translate(@aria-label,'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'no thanks')]"
    );

    // After invalid email, Woohoo shows red helper/error text below the field
    // We wait for any small text near the email input container that indicates invalidity.
    private final By emailErrorNear = By.xpath(
            "//*[@id='email']/ancestor::*[contains(@class,'form-group')][1]//*[self::small or self::div or self::span]" +
                    "[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'invalid') or " +
                    " contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'must have') or " +
                    " contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'not valid')]"
    );

    public WoohooGiftCardPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
        this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(6));
        this.midWait   = new WebDriverWait(driver, Duration.ofSeconds(12));
    }

    // -------------------------------------------------------
    // 1) If a new tab was opened for Woohoo, move to it
    // -------------------------------------------------------
    public void switchToWoohooTab() {
        String current = driver.getWindowHandle();
        List<String> handles = new ArrayList<>(driver.getWindowHandles());
        for (String h : handles) {
            driver.switchTo().window(h);
            String url = "";
            try { url = driver.getCurrentUrl(); } catch (Exception ignored) {}
            // Woohoo URLs are like https://www.woohoo.in/home-centre-online-e-gift-cards
            if (url.contains("woohoo.in")) {
                // give the page a moment to finish lazy scripts
                try { Thread.sleep(500); } catch (InterruptedException ignored) {}
                return;
            }
        }
        // If we didn't find a woohoo tab, switch back to original just in case
        driver.switchTo().window(current);
    }

    // -------------------------------------------------------
    // 2) Dismiss “No thanks” popup if it shows up
    // -------------------------------------------------------
    public void dismissNoThanksIfPresent() {
        try {
            WebElement btn = shortWait.until(ExpectedConditions.elementToBeClickable(noThanksBtn));
            wait.scrollIntoView(btn);
            try { btn.click(); }
            catch (Exception e) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", btn); }
            // small settle
            try { Thread.sleep(200); } catch (InterruptedException ignored) {}
        } catch (TimeoutException ignored) {
            // Popup not shown in this run.
        }
    }

    // -------------------------------------------------------
    // 3) Enter details: receiver name, invalid email, optional mobile/message
    // -------------------------------------------------------
    public void enterReceiverDetails(String receiverName, String invalidEmail) {
        // Name
        WebElement nameEl = midWait.until(ExpectedConditions.visibilityOfElementLocated(nameInput));
        wait.scrollIntoView(nameEl);
        try { nameEl.clear(); } catch (Exception ignored) {}
        nameEl.sendKeys(receiverName);

        // Email (INTENTIONALLY invalid as per the step)
        WebElement emailEl = midWait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
        wait.scrollIntoView(emailEl);
        try { emailEl.clear(); } catch (Exception ignored) {}
        emailEl.sendKeys(invalidEmail);

        // Optional: add a friendly message so your screenshot shows a complete example
        try {
            WebElement msg = shortWait.until(ExpectedConditions.visibilityOfElementLocated(messageTextarea));
            wait.scrollIntoView(msg);
            if (msg.getAttribute("value") == null || msg.getAttribute("value").isBlank()) {
                msg.sendKeys("hello means");
            }
        } catch (TimeoutException ignored) {}

        // Optional: type a sample mobile (may also trigger validation if format differs)
        try {
            WebElement mob = shortWait.until(ExpectedConditions.visibilityOfElementLocated(mobileInput));
            wait.scrollIntoView(mob);
            if (mob.getAttribute("value") == null || mob.getAttribute("value").isBlank()) {
                mob.sendKeys("+91 9793992999");
            }
        } catch (TimeoutException ignored) {}

        // Nudge focus to trigger field validation (blur email)
        try {
            new Actions(driver).moveToElement(nameEl).click().pause(Duration.ofMillis(200)).perform();
        } catch (Exception ignored) {}

        // Wait briefly for the red error helper below email
        try {
            shortWait.until(ExpectedConditions.presenceOfElementLocated(emailErrorNear));
        } catch (TimeoutException ignored) {
            // Even if the helper didn't appear, proceed—the test only needs a screenshot
        }
    }
}
