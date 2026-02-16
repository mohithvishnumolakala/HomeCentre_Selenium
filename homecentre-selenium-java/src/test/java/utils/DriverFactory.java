
package utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;

public class DriverFactory {
    private static final ThreadLocal<WebDriver> tlDriver = new ThreadLocal<>();

    public static void initDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-infobars");
        options.addArguments("--disable-gpu");
        // options.addArguments("--headless=new"); // enable if needed
        tlDriver.set(new ChromeDriver(options));
    }

    public static WebDriver getDriver(){
        return tlDriver.get();
    }

    public static void quitDriver(){
        WebDriver driver = tlDriver.get();
        if (driver != null){
            driver.quit();
            tlDriver.remove();
        }
    }
}
