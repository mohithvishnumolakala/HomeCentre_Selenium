package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.WaitUtils;

import java.time.Duration;

public class GiftingPage {
    private final WebDriver driver;
    private final WaitUtils wait;
    private final WebDriverWait shortWait;
    private final WebDriverWait midWait;

    private final By giftingNav = By.xpath(
            "//a[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'gifting')]" +
                    " | //a[contains(@href,'/gifting')]"
    );

    private final By shopGiftCard = By.xpath(
            "//*[self::a or self::button]" +
                    "[contains(translate(normalize-space(.),'ABCDEFGHIJKLMNOPQRSTUVWXYZ','abcdefghijklmnopqrstuvwxyz'),'shop gift card') " +
                    " or contains(@aria-label,'Gift Card') or contains(@href,'gift-card')]"
    );

    public GiftingPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WaitUtils(driver);
        this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(4));
        this.midWait = new WebDriverWait(driver, Duration.ofSeconds(8));
    }

    public void clickGiftingInHeaderIfVisible() {
        try {
            WebElement nav = shortWait.until(ExpectedConditions.visibilityOfElementLocated(giftingNav));
            wait.scrollIntoView(nav);
            try { nav.click(); }
            catch (Exception e) {
                try { new Actions(driver).moveToElement(nav).pause(Duration.ofMillis(200)).click().perform(); }
                catch (Exception ignored) { ((JavascriptExecutor) driver).executeScript("arguments[0].click();", nav); }
            }
            try { Thread.sleep(100); } catch (InterruptedException ignored) {}
        } catch (TimeoutException ignored) { /* header link not visible; we’ll rely on direct button/URL later */ }
    }

    public void scrollToAndClickShopGiftCard() {
        for (int i = 0; i < 10; i++){
            try {
                WebElement btn = shortWait.until(ExpectedConditions.elementToBeClickable(shopGiftCard));
                wait.scrollIntoView(btn);
                try { btn.click(); }
                catch (Exception e) { ((JavascriptExecutor)driver).executeScript("arguments[0].click();", btn); }
                return;
            } catch (TimeoutException te) {
                wait.scrollBy(300);
            }
        }

        driver.navigate().to("https://www.homecentre.in/in/en/gifting/gift-card");
    }

    public void openGifting(){
        clickGiftingInHeaderIfVisible();
        scrollToAndClickShopGiftCard();
    }
}