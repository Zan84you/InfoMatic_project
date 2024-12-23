import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import java.io.IOException;
import java.io.PrintStream;
import java.time.Duration;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.checkerframework.checker.units.qual.A;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.ui.ExpectedConditions;

public class TicketOneWebScraper {
    public static String[] top10Artists() {
        String url="https://www.ticketone.it/?affiliate=IGA";
        String[] top10 = new String[10];
        try {
            Document document = Jsoup.connect(url).get();
            int count = 0;
            for (Element element : document.getAllElements()) {
                if (count >= 10) {
                    break;
                }
                String content = element.ownText().trim();
                if (!content.isEmpty()) {
                    if (content.matches("^(0[1-9]|10)\\b.*")) {
                        String nextContent = (element.nextElementSibling() != null) ? element.nextElementSibling().ownText().trim() : "";

                        top10[count] = content + " " + nextContent;
                        count++;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Errore durante il web scraping: " + e.getMessage());
        }
        return top10;
    }

    public static List<Article> eventiNome(String text) {
        String url="https://www.ticketone.it/search/?affiliate=IGA&searchterm="+text;

        List<Article> articoli=new LinkedList<>();

        WebDriver driver = null;
        WebDriverWait wait = null;

        PrintStream errinutile = System.err;
        System.setErr(new PrintStream(new java.io.OutputStream() {public void write(int b) {}}));

        try {
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--headless");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0 Safari/537.36");
            options.addArguments("--disable-blink-features=AutomationControlled");
            options.addArguments("--disable-extensions");

            driver = new ChromeDriver(options);
            wait = new WebDriverWait(driver, Duration.ofSeconds(30));

            driver.get(url);

            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("body")));

            WebElement parentElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(".search-result-content.search-result-content-listing")));

            if (parentElement != null && parentElement.isDisplayed()) {
                List<WebElement> childElements = parentElement.findElements(By.xpath(".//div[contains(@class, 'listing-description') and contains(@class, 'theme-text-color') and contains(@class, 'text-overflow-ellipsis')]"));
                for (int i = 0; i < childElements.size(); i++) {
                    if (articoli.size()>3){
                        break;
                    }
                    WebElement currentChild = childElements.get(i);

                    WebElement listingDescription = null;
                    try {
                        listingDescription = currentChild.findElement(
                                By.cssSelector(".listing-data.theme-text-color")
                        );
                    } catch (Exception e) {
                    }

                    WebElement eventListingCity = null;
                    try {
                        eventListingCity = currentChild.findElement(
                                By.xpath("./preceding-sibling::*[contains(@class, 'event-listing-city')]")
                        );
                    } catch (Exception e) {
                    }

                    if (listingDescription != null && !listingDescription.getText().trim().isEmpty() && eventListingCity != null && !eventListingCity.getText().trim().isEmpty()) {
                        String titolo = eventListingCity.getText().trim();
                        String luogoData = listingDescription.getText().trim();
                        String luogo = null;
                        String data = null;
                        String regex = "(\\d{2}/\\d{2}/\\d{4})";
                        Pattern pattern = Pattern.compile(regex);
                        Matcher matcher = pattern.matcher(luogoData);
                        if (matcher.find()) {
                            data = matcher.group(1);
                            luogo = luogoData.substring(matcher.end()).trim();
                            if (luogo.startsWith("─")) {
                                luogo = luogo.substring(1).trim();
                            }
                            if (luogo.contains("Eventi")) {
                                Pattern eventPattern = Pattern.compile("(\\d+)\\s*Eventi");
                                Matcher eventMatcher = eventPattern.matcher(luogo);
                                if (eventMatcher.find()) {
                                    String count = eventMatcher.group(1);
                                    luogo = count + " Eventi";
                                }
                            }
                        } else {
                            luogo = luogoData;
                        }
                        luogo=luogo.substring(4);
                        articoli.add(new Article(titolo,luogo,data));
                    }
                }
            } else {
                System.err.println("elemento padre non trovato");
            }
        } catch (Exception e) {
        } finally {
            System.setErr(errinutile);
            if (driver != null) {
                driver.close();
            }
        }

        return articoli;
    }
}
