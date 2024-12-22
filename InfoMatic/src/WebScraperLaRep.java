import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.util.LinkedList;
import java.util.List;

public class WebScraperLaRep {
    public static List<Article> notizieOggi(){
        String url = "https://www.repubblica.it/";
        List<Article> Articoli = new LinkedList<>();
        try {
            Document document = Jsoup.connect(url).get();
            Elements articles = document.select("article");
            int cont=0;
            for (Element article : articles) {
                Element titleElement = article.selectFirst("span");
                Element descriptionElement = article.selectFirst("h2");
                if (titleElement != null && descriptionElement != null) {
                    String title = titleElement.text();
                    String description = descriptionElement.text();
                    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd");
                    LocalDateTime now = LocalDateTime.now();
                    Articoli.add(new Article(title,description,dtf.format(now)));
                    if (cont++==9){
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Errore durante il web scraping.");
        }
        return Articoli;
    }

    public static List<Article> notizieNome(String nome){
        String url_ricerca="https://ricerca.repubblica.it/ricerca/repubblica?query="+nome+"&view=repubblica&ref=HRHS";
        List<Article> Articoli = new LinkedList<>();
        try {
            Document document = Jsoup.connect(url_ricerca).get();
            Elements articles = document.select("article");
            int cont=0;
            for (Element article : articles) {
                Element titleElement = article.selectFirst("h1");
                Element descriptionElement = article.selectFirst("p");
                if (titleElement != null && descriptionElement != null) {
                    String title = titleElement.text();
                    String description = descriptionElement.text();
                    Element asideElement = article.selectFirst("aside");
                    Elements aElements = asideElement.select("a");
                    Element data_estratta = aElements.size()>=2 ? aElements.get(1):aElements.get(0);
                    String data = data_estratta.text();
                    Articoli.add(new Article(title,description,data));
                    if (cont++==5){
                        break;
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Errore durante il web scraping.");
        }
        return Articoli;
    }
}
