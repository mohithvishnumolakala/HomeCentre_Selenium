package steps;

import io.cucumber.java.After;
import io.cucumber.java.en.*;
import org.openqa.selenium.WebDriver;
import pages.*;
import utils.DriverFactory;
import utils.FileUtilsCustom;
import utils.WaitUtils;

import java.io.IOException;
import java.util.List;

public class HomeCentreSteps {

    private WebDriver driver;
    private WaitUtils waitUtils;
    private List<String[]> related; // kept for compatibility / debugging

    @Given("I open Chrome and navigate to {string}")
    public void i_open_chrome_and_navigate_to(String url) {
        DriverFactory.initDriver();
        driver = DriverFactory.getDriver();
        waitUtils = new WaitUtils(driver);
        new HomePage(driver).open(url);
    }

    @When("I search for {string} in the header search")
    public void i_search_for_in_the_header_search(String query) {
        new HomePage(driver).search(query);
    }

    @When("I set the maximum price filter to {int} and apply it")
    public void i_set_the_maximum_price_filter_to_and_apply_it(Integer max) {
        new SearchResultsPage(driver).setMaxPriceAndApply(max);
    }

    @When("I expand the Type filter and select {string}")
    public void i_expand_the_type_filter_and_select(String type) {
        new SearchResultsPage(driver).expandTypeAndSelectOpen();
    }

    /**
     * Step-5 (updated, no 5s wait, no fixed 50% scroll):
     * - open first product
     * - locate “You may also like”
     * - capture TOP 5 (or fewer if less available)
     * - SAVE HTML immediately (you_may_also_like_top5.html) with non-empty rows
     */
    @When("I open the first product in results and capture related items")
    public void i_open_the_first_product_and_capture_related() {
        SearchResultsPage srp = new SearchResultsPage(driver);
        srp.openFirstProduct();

        ProductPage pp = new ProductPage(driver);
        pp.waitUntilLoaded();
        pp.scrollToRelatedSection();
        List<String[]> top5 = pp.captureYouMayAlsoLikeTopN(5);
        this.related = top5;

        // Build and save HTML with actual rows
        StringBuilder sb = new StringBuilder();
        sb.append("<html><head><meta charset='utf-8'><title>You may also like - Top ")
                .append(top5.size() > 5 ? 5 : top5.size())
                .append("</title>")
                .append("<style>body{font-family:Arial;margin:24px;} ")
                .append("table{border-collapse:collapse;width:100%;} ")
                .append("th,td{border:1px solid #ccc;padding:8px;} ")
                .append("th{background:#f3f3f3}</style></head><body>")
                .append("<h2>You may also like (Top ").append(top5.size() >= 5 ? "5" : top5.size()).append(") - scraped from HomeCentre</h2>")
                .append("<table><thead><tr><th>#</th><th>Name</th><th>Price</th></tr></thead><tbody>");

        int i = 1;
        if (top5 != null && !top5.isEmpty()) {
            for (String[] r : top5) {
                String name = (r != null && r.length > 0) ? r[0] : "";
                String price = (r != null && r.length > 1) ? r[1] : "";
                if (name == null) name = "";
                if (price == null) price = "";
                sb.append("<tr><td>").append(i++).append("</td><td>")
                        .append(escapeHtml(name)).append("</td><td>")
                        .append(escapeHtml(price)).append("</td></tr>");
            }
        } else {
            sb.append("<tr><td colspan='3'><em>No related products found.</em></td></tr>");
        }

        sb.append("</tbody></table></body></html>");
        FileUtilsCustom.saveHtml("you_may_also_like_top5.html", sb.toString());
    }

    // simple escaper for the HTML we generate here
    private String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;");
    }

    @When("I save the {string} items to an html file")
    public void i_save_the_items_to_html(String section) throws IOException {
        // No-op now; Step-5 already saved the HTML (you_may_also_like_top5.html)
    }

    /**
     * Step-6 & Step-7 (as requested earlier):
     * - (Removed the 5s waits you asked to add previously)
     * - go to Gifting, scroll to “Shop Gift Card”, click.
     */
    @When("I navigate to the Gifting section and click Shop Gift Card")
    public void i_go_to_gifting() {
        GiftingPage g = new GiftingPage(driver);
        g.clickGiftingInHeaderIfVisible();
        g.scrollToAndClickShopGiftCard();
    }

    @When("I dismiss the updates popup with No thanks if it appears")
    public void i_dismiss_no_thanks() {
        WoohooGiftCardPage woo = new WoohooGiftCardPage(driver);
        woo.switchToWoohooTab();
        woo.dismissNoThanksIfPresent();
    }

    @When("I enter receiver name {string} and invalid email {string} then take a screenshot")
    public void i_enter_invalid_email_and_screenshot(String name, String invalid) throws IOException {
        WoohooGiftCardPage woo = new WoohooGiftCardPage(driver);
        woo.enterReceiverDetails(name, invalid);
        FileUtilsCustom.takeScreenshot(driver, "invalid_email");
    }

    @Then("I quit the browser")
    public void i_quit_the_browser() {
        DriverFactory.quitDriver();
    }

    @After
    public void tearDown() {
        DriverFactory.quitDriver();
    }
}