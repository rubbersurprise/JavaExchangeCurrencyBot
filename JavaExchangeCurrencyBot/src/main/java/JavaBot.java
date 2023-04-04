import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class JavaBot extends TelegramLongPollingBot {
    private BankAPIParser data;
    State state = new State();







    @Override
    public void onUpdateReceived(Update update) {




        if (update.hasMessage() && update.getMessage().hasText()) {

            String message_text = update.getMessage().getText();

            long chat_id = update.getMessage().getChatId();

            if (message_text.equals("/menu")) {


                state.setState("/menu");

                SendMessage message = new SendMessage();
                message.setChatId(chat_id);
                message.setText("Here is your menu");

                ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
                List<KeyboardRow> keyboard = new ArrayList<>();
                KeyboardRow row = new KeyboardRow();
                row.add("Exchange ratio right now");
                row.add("Currency Converter");
                keyboard.add(row);
                keyboardMarkup.setKeyboard(keyboard);
                message.setReplyMarkup(keyboardMarkup);
                try {
                    System.out.println(state.getState());
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            } else if (message_text.equals("Exchange ratio right now")) {


                state.setState("Exchange ratio right now");


                SendMessage message = new SendMessage();
                message.setChatId(chat_id);
                message.setText("EUR -> USD or USD -> EUR");
                ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
                List<KeyboardRow> keyboard = new ArrayList<>();
                KeyboardRow row = new KeyboardRow();
                row.add("EUR -> USD");
                row.add("USD -> EUR");
                keyboard.add(row);
                keyboardMarkup.setKeyboard(keyboard);
                message.setReplyMarkup(keyboardMarkup);
                try {
                    System.out.println(state.getState());
                    execute(message);

                } catch (TelegramApiException e) {
                    System.out.println(e);
                    e.printStackTrace();
                }


            } else if (message_text.equals("Currency Converter")) {


                state.setState("Currency Converter");

                SendMessage message = new SendMessage();
                message.setChatId(chat_id);
                message.setText("Please select, how you want to convert your currency");
                ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();
                List<KeyboardRow> keyboard = new ArrayList<>();
                KeyboardRow row = new KeyboardRow();
                row.add("EUR --> USD");
                row.add("USD --> EUR");
                keyboard.add(row);
                keyboardMarkup.setKeyboard(keyboard);
                message.setReplyMarkup(keyboardMarkup);

                try {
                    System.out.println(state.getState());
                    execute(message);
                } catch (TelegramApiException e) {
                    System.out.println(e);
                    throw new RuntimeException(e);
                }

            } else if (message_text.equals("EUR --> USD")) {

                state.setState("EUR --> USD");

                SendMessage message = new SendMessage();
                message.setChatId(chat_id);
                message.setText("How much EUR do you want to exchange?");
                try {
                    System.out.println(state.getState());
                    execute(message);
                } catch (TelegramApiException e) {
                    System.out.println(e);
                    e.printStackTrace();
                }








               /* OkHttpClient client = new OkHttpClient().newBuilder().build();

                String to = "USD";
                String from = "EUR";
                String amount = "5";

                Request request = new Request.Builder()
                        .url("https://api.apilayer.com/fixer/convert?to=" + to + "&from=" + from + "&amount=" + amount)
                        .addHeader("apikey", "APIKEY")
                        .method("GET", null)
                        .build();

                SendMessage message = new SendMessage();
                message.setChatId(chat_id);









                try{
                    Response response = client.newCall(request).execute();
                    String json = response.body().string();



                    message.setText("5 EUR equals" + result + " USD");
                    execute(message);


                } catch ( TelegramApiException | IOException e) {
                    e.printStackTrace();
                }

                */


            } else if (message_text.equals("EUR -> USD")) {

                state.setState("EUR -> USD");

                ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                File ApiFile = new File("src/main/resources/data.json");
                SendMessage message = new SendMessage();
                message.setChatId(chat_id);


                try {
                    System.out.println(state.getState());
                    this.data = objectMapper.readValue(ApiFile, BankAPIParser.class);
                    message.setText("1 EUR equals " + this.data.getResult() + " USD \nTo return to main menu type /menu");
                    execute(message);
                } catch (TelegramApiException | IOException e) {
                    e.printStackTrace();
                }

            } else if (message_text.equals("USD -> EUR")) {


                state.setState("USD -> EUR");

                ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                File ApiFile = new File("src/main/resources/data.json");
                SendMessage message = new SendMessage();
                message.setChatId(chat_id);


                try {
                    System.out.println(state.getState());
                    this.data = objectMapper.readValue(ApiFile, BankAPIParser.class);
                    message.setText("1 USD equals " + (1 / this.data.getResult()) + " EUR \nTo return to main menu type /menu");
                    execute(message);
                } catch (TelegramApiException | IOException e) {
                    e.printStackTrace();
                }
            } else if (update.hasMessage() && state.getState().equals("EUR --> USD")) {



                SendMessage message = new SendMessage();
                message.setChatId(chat_id);
                double convEurToUsd = Double.parseDouble(update.getMessage().getText());

                ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                File ApiFile = new File("src/main/resources/data.json");

                try {
                    this.data = objectMapper.readValue(ApiFile, BankAPIParser.class);
                    message.setText(convEurToUsd + " EUR is equal " + (convEurToUsd * this.data.getResult()) + " USD\n To return to main menu type /menu");
                    execute(message);
                } catch (IOException | TelegramApiException e) {
                    System.out.println(e);
                    throw new RuntimeException(e);
                }


            } else if (message_text.equals("USD --> EUR")) {

                state.setState("USD --> EUR");

                SendMessage message = new SendMessage();
                message.setChatId(chat_id);
                message.setText("How much USD do you want to exchange?");
                try {
                    System.out.println(state.getState());
                    execute(message);
                } catch (TelegramApiException e) {
                    System.out.println(e);
                    e.printStackTrace();
                }





            } else if (update.hasMessage() && state.getState().equals("USD --> EUR")) {

                SendMessage message = new SendMessage();
                message.setChatId(chat_id);
                double convUsdToEur = Double.parseDouble(update.getMessage().getText());

                ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                File ApiFile = new File("src/main/resources/data.json");

                try {
                    this.data = objectMapper.readValue(ApiFile, BankAPIParser.class);
                    message.setText(convUsdToEur + " USD is equal " + ((1 / this.data.getResult()) * convUsdToEur) + " EUR\n To return to main menu type /menu");
                    execute(message);
                } catch (IOException | TelegramApiException e) {
                    System.out.println(e);
                    throw new RuntimeException(e);
                }


            } else {
                    state.setState("Unknown command");

                SendMessage message = new SendMessage();
                message.setChatId(chat_id);
                message.setText("Unknown command, please use /menu");

                try{
                    execute(message);
                    System.out.println(state);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            }



        }

        }




    @Override
    public String getBotUsername() {
        // TODO
        return "BOTNAME";
    }

    @Override
    public String getBotToken() {
        // TODO
        return "APIKEY";
    }




}
