public class Article {
    //con articolo tratto sia notizia che evento

    private String title;
    private String description;
    private String data;

    public Article(String title, String description, String author) {
        this.title = title;
        this.description = description;
        this.data = author;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getData() {
        return data;
    }

    @Override
    public String toString() {
        return "Titolo: " + title + "\n" +
                "Descrizione: " + description + "\n" +
                "Data: " + data;
    }
}
