package utils;

import org.openqa.selenium.*;
import org.openqa.selenium.OutputType;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;

public class FileUtilsCustom {

    private static Path artifactsDir() {
        return Paths.get("target", "artifacts");
    }

    private static void ensureArtifactsDir() {
        try {
            Files.createDirectories(artifactsDir());
        } catch (IOException e) {
            throw new RuntimeException("Failed to create artifacts directory", e);
        }
    }

    /** Write a raw HTML string to the artifacts folder. */
    public static void saveHtml(String fileName, String htmlContent) {
        ensureArtifactsDir();
        Path out = artifactsDir().resolve(fileName);
        try {
            Files.write(out, htmlContent.getBytes(StandardCharsets.UTF_8),
                    StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Failed to write HTML file: " + out, e);
        }
    }

    /** Take a screenshot using the SAME driver reference as the step. */
    public static String takeScreenshot(WebDriver driver, String baseName) {
        ensureArtifactsDir();
        String file = baseName.endsWith(".png") ? baseName : baseName + ".png";
        Path out = artifactsDir().resolve(file);
        try {
            TakesScreenshot ts = (TakesScreenshot) driver;
            byte[] png = ts.getScreenshotAs(OutputType.BYTES);
            Files.write(out, png, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING);
            return out.toString();
        } catch (WebDriverException | IOException e) {
            throw new RuntimeException("Failed to take screenshot: " + out, e);
        }
    }
}