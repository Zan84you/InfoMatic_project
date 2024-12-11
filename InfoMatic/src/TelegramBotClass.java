import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.util.ArrayList;
import java.util.List;

public class TelegramBotClass extends TelegramLongPollingBot {
    private static final String BOT_USERNAME = credenziali.get_username();

    private static final String BOT_TOKEN = credenziali.get_token();

    private boolean scrivereScelta=false;
    private int scelta=-1;
    private String scelta_txt="";

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

            scrivereScelta=true;
            handleCallback(callbackData, chatId);
        }
    }

    private void handleCommand(String command, long chatId) {
        switch (command) {
            case "/start":
                sendMessageWithButton(chatId);
                scrivereScelta=false;
                scelta_txt="";
                scelta=-1;
                break;
            case "/help":
                sendTextMessage(chatId, "Lista comandi disponibili: \n/start\n /help\n /info\n se vuoi applicare filtri quando devi scrivere inizia la frase con:\n date: /d");
                break;
            case "/info":
                sendTextMessage(chatId, "Questo progetto mira a sviluppare un assistente digitale accessibile tramite Telegram, " +
                        "pensato per offrire un accesso rapido e personalizzato a notizie e informazioni aggiornate su " +
                        "eventi di attualità. Attraverso algoritmi appositi di ricerca e organizzazione dei dati, il bot mira " +
                        "a semplificare la consultazione delle notizie, consentendo agli utenti di rimanere informati in " +
                        "modo semplice e efficace.");
                break;
            default:
                sendTextMessage(chatId, "Comando non conosciuto...");
        }
    }

    private void handleText(String text, long chatId) {
        if (text.startsWith("/d")) {
            sendTextMessage(chatId,"data presa");
        } else {
            sendTextMessage(chatId,"desso cerco");
        }
    }

    private void handleCallback(String callbackData, long chatId) {
        switch (callbackData) {
            case "scelta_notizia":
                sendTextMessage(chatId, "Che notizia stai cercando?");
                scelta=0;
                break;
            case "scelta_evento":
                sendTextMessage(chatId, "Che evento stai cercando?");
                scelta=1;
                break;
            default:
                sendTextMessage(chatId, "Unknown action.");
        }
    }

    private void sendMessageWithButton(long chatId) {
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
}
