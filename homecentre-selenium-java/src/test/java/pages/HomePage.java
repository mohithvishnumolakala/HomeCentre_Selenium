
package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import utils.WaitUtils;

public class HomePage {
    private final WebDriver driver;
    private final WaitUtils wait;

    private final By searchInput = By.cssSelector("input#js-site-search-input");

    public HomePage(WebDriver driver){
        this.driver = driver;
        this.wait = new WaitUtils(driver);
    }

    public void open(String url){
        driver.get(url);
    }

    public void search(String query){
        WebElement input = wait.waitForVisible(searchInput);
        input.clear();
        input.sendKeys(query);
        input.sendKeys(Keys.ENTER);
    }
}
