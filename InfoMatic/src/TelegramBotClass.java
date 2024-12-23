import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;

import java.sql.*;

public class TelegramBotClass extends TelegramLongPollingBot {
    private static final String BOT_USERNAME = credenziali.get_username();

    private static final String BOT_TOKEN = credenziali.get_token();

    private boolean scrivereScelta=false;
    private int scelta=-1;

    @Override
    public String getBotUsername() {
        return BOT_USERNAME;
    }

    @Override
    public String getBotToken() {
        return BOT_TOKEN;
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String userInput = update.getMessage().getText().toLowerCase();
            long chatId = update.getMessage().getChatId();
            if (userInput.startsWith("/")) {
                handleCommand(userInput, chatId);
            } else {
                if (scrivereScelta){
                    handleText(userInput, chatId);
                }
            }
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();
            long chatId = update.getCallbackQuery().getMessage().getChatId();
            if (callbackData.equals("scelta_notizia") || callbackData.equals("scelta_evento")){
                scrivereScelta=true;
            }
            handleCallback(callbackData, chatId);
        }
    }

    private void handleCommand(String command, long chatId) {
        switch (command) {
            case "/start":
                sendStartMessageWithButton(chatId);
                scrivereScelta=false;
                scelta=-1;
                break;
            case "mkRequest":
                break;
            case "/help":
                sendTextMessage(chatId, "Lista comandi disponibili: \n/start -> avvia bot\n /help -> lista comandi\n /info -> informazioni su InfoMatic\n /quickres -> ricerca rapida\n");
                scrivereScelta=false;
                scelta=-1;
                break;
            case "/info":
                sendTextMessage(chatId, "Questo progetto mira a sviluppare un assistente digitale accessibile tramite Telegram, " +
                        "pensato per offrire un accesso rapido e personalizzato a notizie e informazioni aggiornate su " +
                        "eventi di attualità. Attraverso algoritmi appositi di ricerca e organizzazione dei dati, il bot mira " +
                        "a semplificare la consultazione delle notizie, consentendo agli utenti di rimanere informati in " +
                        "modo semplice e efficace.");
                scrivereScelta=false;
                scelta=-1;
                break;
            case "/quickres":
                sendGuideMessageWithButton(chatId);
                break;
            default:
            sendTextMessage(chatId, "Comando non conosciuto...");
        }
    }

    private void sendStartMessageWithButton(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Per qualsiasi informazione sul funzionamento premi: /help");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Notizia");
        button1.setCallbackData("scelta_notizia");
        row.add(button1);

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Evento");
        button2.setCallbackData("scelta_evento");
        row.add(button2);

        rows.add(row);
        markup.setKeyboard(rows);

        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendGuideMessageWithButton(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Notizie di oggi");
        button1.setCallbackData("scelta_notoggi");
        row.add(button1);

        InlineKeyboardButton button2 = new InlineKeyboardButton();
        button2.setText("Top 10 artisti");
        button2.setCallbackData("scelta_top10art");
        row.add(button2);

        InlineKeyboardButton button3 = new InlineKeyboardButton();
        button3.setText("Mostra eventi salvati");
        button3.setCallbackData("eventi_salvati");
        row.add(button3);

        rows.add(row);
        markup.setKeyboard(rows);

        message.setReplyMarkup(markup);
        message.setText("Scegli un'opzione:");

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void sendSaveEventMessageWithButton(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Vuoi salvare gli eventi trovati?");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rows = new ArrayList<>();

        List<InlineKeyboardButton> row = new ArrayList<>();
        InlineKeyboardButton button1 = new InlineKeyboardButton();
        button1.setText("Salva");
        button1.setCallbackData("salva_eventi");
        row.add(button1);

        rows.add(row);
        markup.setKeyboard(rows);

        message.setReplyMarkup(markup);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private void handleCallback(String callbackData, long chatId) {
        Database db;
        switch (callbackData) {
            case "scelta_notizia":
                sendTextMessage(chatId, "Che notizia stai cercando (inseriscila come parola chiave)?");
                scelta=0;
                break;
            case "scelta_evento":
                sendTextMessage(chatId, "Che evento stai cercando (inseriscilo come parola chiave)?");
                scelta=1;
                break;
            case "scelta_notoggi":
                db = new Database();
                try{
                    Thread.sleep(1000);
                }catch (Exception e){}
                try {
                    ResultSet rs = db.eseguiQuery("SELECT * FROM notizie WHERE data = CURDATE()");

                    if (db.leggiNotizie().isEmpty() || !rs.next()) {
                        List<Article> notizie = WebScraperLaRep.notizieOggi();
                        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
                        for (Article notizia : notizie) {
                            String dataString = notizia.getData();
                            Date utilDate = null;
                            try {
                                utilDate = dateFormat.parse(dataString);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());
                            db.inserisciNotizia(notizia.getTitle(), notizia.getDescription(), sqlDate);

                            sendTextMessage(chatId, notizia.toString());
                        }
                    } else {
                        while (rs.next()) {
                            String message=rs.getString("titolo")+"\n"+rs.getString("descrizione")+"\n"+rs.getDate("data")+"\n";
                            sendTextMessage(chatId,message);
                        }
                    }
                } catch (SQLException e) {
                    System.err.println("Errore durante la gestione del database: " + e.getMessage());
                }
                finally {
                    db.chiudiConnessione();
                }
                break;
            case "scelta_top10art":
                String[] top10=TicketOneWebScraper.top10Artists();
                for (String element:top10){
                    element=element.replace("Biglietti","");
                    sendTextMessage(chatId,element);
                }
                break;
            case "eventi_salvati":
                db = new Database();
                List<Database.Evento> eventi=db.leggiEventi();
                for (Database.Evento e:eventi){
                    sendTextMessage(chatId,e.toString());
                }
                break;
            case "salva_eventi":
                db = new Database();
                try{
                    Thread.sleep(1000);
                }catch (Exception e){}

                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    for (Article a : eventi_cercati) {
                        String dataString = a.getData();
                        Date utilDate = null;
                        try {
                            utilDate = dateFormat.parse(dataString);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        java.sql.Date sqlDate = new java.sql.Date(utilDate.getTime());

                        String query = "SELECT * FROM eventi WHERE titolo = ? AND data = ?";
                        try (PreparedStatement stmt = db.getConnection().prepareStatement(query)) {
                            stmt.setString(1, a.getTitle());
                            stmt.setDate(2, sqlDate);
                            ResultSet rs = stmt.executeQuery();

                            if (!rs.next()) {
                                db.inserisciEvento(a.getTitle(), a.getDescription(), sqlDate);
                            }
                        } catch (SQLException e) {
                            e.printStackTrace();
                        }
                    }
                } catch (Exception e) {
                    System.err.println("Errore durante la gestione del database: " + e.getMessage());
                } finally {
                    db.chiudiConnessione();
                }

                break;
        }
    }

    private void sendTextMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    List<Article> eventi_cercati;
    private void handleText(String text, long chatId) {
        if (scelta==0){
            List<Article> notizie=WebScraperLaRep.notizieNome(text);
            for (Article notizia:notizie){
                sendTextMessage(chatId,notizia.toString());
            }
        }else if (scelta==1){
            eventi_cercati= new LinkedList<>();
            List<Article> eventi=TicketOneWebScraper.eventiNome(text);
            for (Article evento:eventi){
                sendTextMessage(chatId,evento.toString());
            }
            if (!eventi.isEmpty()){
                sendSaveEventMessageWithButton(chatId);
                eventi_cercati=eventi;
            }
        }
    }
}
