import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database {
    private Connection connection;
    private static final String DB_URL = "jdbc:mysql://localhost:3306/NotizieEventidb";
    private static final String USER = "root";
    private static final String PASSWORD = "";

    public Database() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            this.connection = DriverManager.getConnection(DB_URL, USER, PASSWORD);
            System.out.println("connessione db stabilita...");
        } catch (ClassNotFoundException e) {
            System.err.println("driver JDBC non trovato: " + e.getMessage());
        } catch (SQLException e) {
            System.err.println("errore di connessione al database: " + e.getMessage());
        }
    }

    public Connection getConnection() {
        return this.connection;
    }

    public void inserisciNotizia(String titolo, String descrizione, Date data) {
        String query = "INSERT INTO notizie (titolo, descrizione, data) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, titolo);
            pstmt.setString(2, descrizione);
            pstmt.setDate(3, data);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore durante l'inserimento della notizia: " + e.getMessage());
        }
    }

    public void inserisciEvento(String titolo, String luogo, Date data) {
        String query = "INSERT INTO eventi (titolo, luogo, data) VALUES (?, ?, ?)";

        try (PreparedStatement pstmt = connection.prepareStatement(query)) {
            pstmt.setString(1, titolo);
            pstmt.setString(2, luogo);
            pstmt.setDate(3, data);

            pstmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Errore durante l'inserimento dell'evento: " + e.getMessage());
        }
    }

    public List<Notizia> leggiNotizie() {
        List<Notizia> notizie = new ArrayList<>();
        String query = "SELECT * FROM notizie";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Notizia notizia = new Notizia(
                        rs.getInt("id"),
                        rs.getString("titolo"),
                        rs.getString("descrizione"),
                        rs.getDate("data")
                );
                notizie.add(notizia);
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la lettura delle notizie: " + e.getMessage());
        }

        return notizie;
    }

    public List<Evento> leggiEventi() {
        List<Evento> eventi = new ArrayList<>();
        String query = "SELECT * FROM eventi";

        try (Statement stmt = connection.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {

            while (rs.next()) {
                Evento evento = new Evento(
                        rs.getInt("id"),
                        rs.getString("titolo"),
                        rs.getString("luogo"),
                        rs.getDate("data")
                );
                eventi.add(evento);
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la lettura degli eventi: " + e.getMessage());
        }

        return eventi;
    }

    public ResultSet eseguiQuery(String query) {
        try {
            Statement stmt = connection.createStatement();
            return stmt.executeQuery(query);
        } catch (SQLException e) {
            System.err.println("Errore durante l'esecuzione della query: " + e.getMessage());
            return null;
        }
    }

    public void chiudiConnessione() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("Connessione al database chiusa.");
            }
        } catch (SQLException e) {
            System.err.println("Errore durante la chiusura della connessione: " + e.getMessage());
        }
    }

    public static class Notizia {
        private int id;
        private String titolo;
        private String descrizione;
        private Date data;

        public Notizia(int id, String titolo, String descrizione, Date data) {
            this.id = id;
            this.titolo = titolo;
            this.descrizione = descrizione;
            this.data = data;
        }

        @Override
        public String toString() {
            return "ID: " + id +
                    ", Titolo: " + titolo +
                    ", Descrizione: " + descrizione +
                    ", Data: " + data +
                    "\n";
        }

        public int getId() { return id; }
        public String getTitolo() { return titolo; }
        public String getDescrizione() { return descrizione; }
        public Date getData() { return data; }
    }

    public static class Evento {
        private int id;
        private String titolo;
        private String luogo;
        private Date data;

        public Evento(int id, String descrizione, String luogo, Date data) {
            this.id = id;
            this.titolo = titolo;
            this.luogo = luogo;
            this.data = data;
        }

        @Override
        public String toString() {
            return "Titolo: " + titolo +
                    ", Luogo: " + luogo +
                    ", Data: " + data +
                    "\n";
        }

        public int getId() { return id; }
        public String getDescrizione() { return titolo; }
        public String getLuogo() { return luogo; }
        public Date getData() { return data; }
    }
}
