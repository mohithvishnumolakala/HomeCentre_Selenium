package pages;

import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import utils.WaitUtils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ProductPage {
    private final WebDriver driver;
    private final WaitUtils wait;
    private final WebDriverWait shortWait;
    private final WebDriverWait midWait;


    private WebElement relatedContainer;

    public ProductPage(WebDriver driver){
        this.driver = driver;
        this.wait = new WaitUtils(driver);
        this.shortWait = new WebDriverWait(driver, Duration.ofSeconds(4));
        this.midWait = new WebDriverWait(driver, Duration.ofSeconds(8));
    }

    // PDP title shows that product page is loaded
    private final By pdpTitle = By.cssSelector("h1, h1[role='heading'], h1[itemprop='name']");

    // Case-insensitive header match (tolerant to &nbsp;)
    private final By relatedHeader = By.xpath(
            "//*[self::h2 or self::h3 or self::div]" +
                    "[contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ\u00A0', 'abcdefghijklmnopqrstuvwxyz '), 'you may also like') " +
                    " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ\u00A0', 'abcdefghijklmnopqrstuvwxyz '), 'similar products') " +
                    " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ\u00A0', 'abcdefghijklmnopqrstuvwxyz '), 'related products') " +
                    " or contains(translate(normalize-space(.), 'ABCDEFGHIJKLMNOPQRSTUVWXYZ\u00A0', 'abcdefghijklmnopqrstuvwxyz '), 'recommended for you')]"
    );

    // Product tile anchors inside a related container
    private final By relatedTiles = By.xpath(
            ".//a[contains(@href,'/p/') or contains(@href,'/product/')] | " +
                    ".//div[contains(@class,'product') or contains(@class,'card')]//a"
    );

    public void waitUntilLoaded(){
        try {
            midWait.until(ExpectedConditions.visibilityOfElementLocated(pdpTitle));
        } catch (Exception ignored) {}
    }

    /** Find the related container (header or known container). No fixed 50% scroll or pause. */
    private WebElement locateRelatedContainer(){
        // Try to find a header and use its nearest container
        try {
            WebElement hdr = shortWait.until(ExpectedConditions.visibilityOfElementLocated(relatedHeader));
            wait.scrollIntoView(hdr);
            try { return hdr.findElement(By.xpath("ancestor::section[1] | ancestor::div[1]")); }
            catch (NoSuchElementException e) { return hdr; }
        } catch (TimeoutException ignored) {}

        // Fallback: generic containers that often host related items
        By containers = By.xpath(
                "//*[contains(@class,'you-may') or contains(@id,'you-may') or " +
                        "  contains(@class,'related') or contains(@id,'related') or " +
                        "  contains(@class,'recommend') or contains(@id,'recommend')] | " +
                        "//div[contains(@class,'YouMayAlsoLike')]"
        );
        try {
            WebElement c = shortWait.until(ExpectedConditions.visibilityOfElementLocated(containers));
            wait.scrollIntoView(c);
            return c;
        } catch (TimeoutException ignored) {}

        return null;
    }

    public void scrollToRelatedSection() {
        waitUntilLoaded();

        // try immediate find
        relatedContainer = locateRelatedContainer();

        // progressive small scrolls if still not found
        for (int i = 0; i < 10 && relatedContainer == null; i++) {
            wait.scrollBy(600);
            relatedContainer = locateRelatedContainer();
        }

        // final attempt: bottom then re-check
        if (relatedContainer == null) {
            wait.scrollToBottom();
            relatedContainer = locateRelatedContainer();
        }

        if (relatedContainer != null) {
            wait.scrollIntoView(relatedContainer);
        }
    }

    /** Return first N items (name, price) from the related section (top-N, default 5). */
    public List<String[]> captureYouMayAlsoLikeTopN(int n) {
        List<String[]> out = new ArrayList<>();
        if (relatedContainer == null) {
            // fallback: attempt one-shot find without caching
            relatedContainer = locateRelatedContainer();
            if (relatedContainer == null) return out;
        }

        List<WebElement> links = relatedContainer.findElements(relatedTiles);
        for (WebElement a : links) {
            if (out.size() >= n) break;
            try {
                WebElement card = a.findElement(By.xpath(
                        "ancestor::div[contains(@class,'card') or contains(@class,'tile') or contains(@class,'product')][1]"
                ));
                String name = a.getAttribute("title");
                if (name == null || name.isBlank()) name = a.getText().trim();
                if ((name == null || name.isBlank())) {
                    try {
                        name = card.findElement(By.xpath(
                                        ".//*[contains(@class,'name') or contains(@class,'Title') or self::h3 or self::h4]"))
                                .getText().trim();
                    } catch (Exception ignored) {}
                }
                String price = "";
                try {
                    price = card.findElement(By.xpath(
                            ".//*[contains(text(),'₹') or contains(@class,'price') or contains(@class,'Price')][1]"
                    )).getText().replace("\n"," ").trim();
                } catch (Exception ignored) {}
                if (name != null && !name.isBlank()) out.add(new String[]{name, price});
            } catch (Exception ignored) {}
        }
        return out;
    }

    /** Kept for compatibility if other code calls it (unlimited). */
    public List<String[]> captureYouMayAlsoLike() {
        return captureYouMayAlsoLikeTopN(Integer.MAX_VALUE);
    }
}