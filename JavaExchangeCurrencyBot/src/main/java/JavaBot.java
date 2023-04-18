import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;


public class JavaBot extends TelegramLongPollingBot {
    private BankAPIParser data;
    State state = new State();







    @Override
    public void onUpdateReceived(Update update) {

        String url_sql = "jdbc:postgresql://localhost:5432/myfirstdb";
        String user_sql = "postgres";
        String password_sql = "postgres";

        String date = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());

        BankAPIParser bankAPIParser = new BankAPIParser();
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        File ApiFile = new File("/Users/romanprib/Downloads/JavaExchangeCurrencyBot-main/JavaExchangeCurrencyBot/src/main/resources/data.json");
        try {
            bankAPIParser = objectMapper.readValue(ApiFile, BankAPIParser.class);

            if (bankAPIParser.getDate().equals(date)){
                System.out.println("no API request needed");
            } else {

                OkHttpClient client = new OkHttpClient().newBuilder().build();

                Request request = new Request.Builder()
                        .url("https://api.apilayer.com/fixer/convert?to=USD&from=EUR&amount=1")
                        .addHeader("apikey", "bYDLgO53AMPWeK3hpULRNwriVfGiplmg")
                        .method("GET", null)
                        .build();

                Response response = client.newCall(request).execute();
                String json = response.body().string();

                ObjectMapper mapper = new ObjectMapper();
                JsonNode actualObj = mapper.readTree(json);

                objectMapper.writeValue(ApiFile,actualObj);
                ApiFile = new File("/Users/romanprib/Downloads/JavaExchangeCurrencyBot-main/JavaExchangeCurrencyBot/src/main/resources/data.json" , json);

                System.out.println(" api request successful, json file was replaced");
            }
        } catch (IOException e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }




        if (update.hasMessage() && update.getMessage().hasText()) {

            String message_text = update.getMessage().getText();

            long chat_id = update.getMessage().getChatId();




            List<String> userList = new ArrayList<>();
            try(Connection connection = DriverManager.getConnection(url_sql, user_sql, password_sql)){

                String sql_check = "SELECT * FROM public.bot_users WHERE chat_id = '" + chat_id + "';";
                PreparedStatement statement = connection.prepareStatement(sql_check);
                ResultSet resultSet = statement.executeQuery();

                while (resultSet.next()){

                    String user_id = resultSet.getString("chat_id");
                    userList.add(user_id);

                }

                if (userList.size() == 0) {


                    String sql = "INSERT INTO bot_users (chat_id) VALUES (?)";

                    statement = connection.prepareStatement(sql);
                    statement.setLong(1, chat_id);

                    int rowsAffected = statement.executeUpdate();
                    System.out.println("Inserted " + rowsAffected + " row(s) successfully.");
                }

            } catch (SQLException e){

            System.out.println(e);

            }





            if (message_text.equals("/menu")) {

            try(Connection connection = DriverManager.getConnection(url_sql, user_sql, password_sql)) {

                String stateUpdate = "UPDATE bot_users SET user_state = ? WHERE chat_id = '"+ chat_id +"'";
                PreparedStatement statement = connection.prepareStatement(stateUpdate);
                statement.setString(1, "menu");
                statement.executeUpdate();

            } catch (SQLException e) {

                System.out.println(e);

            }

            try(Connection connection = DriverManager.getConnection(url_sql, user_sql, password_sql)){

                String sql_request = "SELECT user_state FROM bot_users WHERE chat_id = '" + chat_id + "';";
                PreparedStatement statement = connection.prepareStatement(sql_request);
                ResultSet resultSet = statement.executeQuery();

                resultSet.next();
                state.setState(resultSet.getString("user_state"));
                System.out.println(state.getState());




            } catch (SQLException e){
                System.out.println(e);
            }




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
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            } else if (message_text.equals("Exchange ratio right now")) {

                try(Connection connection = DriverManager.getConnection(url_sql, user_sql, password_sql)) {

                    String stateUpdate = "UPDATE bot_users SET user_state = ? WHERE chat_id = '"+ chat_id +"'";
                    PreparedStatement statement = connection.prepareStatement(stateUpdate);
                    statement.setString(1, "exchange now");
                    statement.executeUpdate();

                } catch (SQLException e) {

                    System.out.println(e);

                }


                try(Connection connection = DriverManager.getConnection(url_sql, user_sql, password_sql)){

                    String sql_request = "SELECT user_state FROM bot_users WHERE chat_id = '" + chat_id + "';";
                    PreparedStatement statement = connection.prepareStatement(sql_request);
                    ResultSet resultSet = statement.executeQuery();

                    resultSet.next();
                    state.setState(resultSet.getString("user_state"));
                    System.out.println(state.getState());




                } catch (SQLException e){
                    System.out.println(e);
                }


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
                    execute(message);

                } catch (TelegramApiException e) {
                    System.out.println(e);
                    e.printStackTrace();
                }


            } else if (message_text.equals("Currency Converter")) {

                try(Connection connection = DriverManager.getConnection(url_sql, user_sql, password_sql)) {

                    String stateUpdate = "UPDATE bot_users SET user_state = ? WHERE chat_id = '"+ chat_id +"'";
                    PreparedStatement statement = connection.prepareStatement(stateUpdate);
                    statement.setString(1, "currency converter");
                    statement.executeUpdate();

                } catch (SQLException e) {

                    System.out.println(e);

                }


                try(Connection connection = DriverManager.getConnection(url_sql, user_sql, password_sql)){

                    String sql_request = "SELECT user_state FROM bot_users WHERE chat_id = '" + chat_id + "';";
                    PreparedStatement statement = connection.prepareStatement(sql_request);
                    ResultSet resultSet = statement.executeQuery();

                    resultSet.next();
                    state.setState(resultSet.getString("user_state"));
                    System.out.println(state.getState());




                } catch (SQLException e){
                    System.out.println(e);
                }

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

                try(Connection connection = DriverManager.getConnection(url_sql, user_sql, password_sql)) {

                    String stateUpdate = "UPDATE bot_users SET user_state = ? WHERE chat_id = '"+ chat_id +"'";
                    PreparedStatement statement = connection.prepareStatement(stateUpdate);
                    statement.setString(1, "EUR to USD");
                    statement.executeUpdate();

                } catch (SQLException e) {

                    System.out.println(e);

                }

                try(Connection connection = DriverManager.getConnection(url_sql, user_sql, password_sql)){

                    String sql_request = "SELECT user_state FROM bot_users WHERE chat_id = '" + chat_id + "';";
                    PreparedStatement statement = connection.prepareStatement(sql_request);
                    ResultSet resultSet = statement.executeQuery();

                    resultSet.next();
                    state.setState(resultSet.getString("user_state"));
                    System.out.println(state.getState());




                } catch (SQLException e){
                    System.out.println(e);
                }

                SendMessage message = new SendMessage();
                message.setChatId(chat_id);
                message.setText("How much EUR do you want to exchange?");
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    System.out.println(e);
                    e.printStackTrace();
                }

            } else if (message_text.equals("EUR -> USD")) {

                try(Connection connection = DriverManager.getConnection(url_sql, user_sql, password_sql)) {

                    String stateUpdate = "UPDATE bot_users SET user_state = ? WHERE chat_id = '"+ chat_id +"'";
                    PreparedStatement statement = connection.prepareStatement(stateUpdate);
                    statement.setString(1, "1 EUR in USD");
                    statement.executeUpdate();

                } catch (SQLException e) {

                    System.out.println(e);

                }

                try(Connection connection = DriverManager.getConnection(url_sql, user_sql, password_sql)){

                    String sql_request = "SELECT user_state FROM bot_users WHERE chat_id = '" + chat_id + "';";
                    PreparedStatement statement = connection.prepareStatement(sql_request);
                    ResultSet resultSet = statement.executeQuery();

                    resultSet.next();
                    state.setState(resultSet.getString("user_state"));
                    System.out.println(state.getState());




                } catch (SQLException e){
                    System.out.println(e);
                }

                objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                ApiFile = new File("/Users/romanprib/Downloads/JavaExchangeCurrencyBot-main/JavaExchangeCurrencyBot/src/main/resources/data.json");
                SendMessage message = new SendMessage();
                message.setChatId(chat_id);


                try {
                    this.data = objectMapper.readValue(ApiFile, BankAPIParser.class);
                    message.setText("1 EUR equals " + this.data.getResult() + " USD \nTo return to main menu type /menu");
                    execute(message);
                } catch (TelegramApiException | IOException e) {
                    e.printStackTrace();
                }

            } else if (message_text.equals("USD -> EUR")) {

                try(Connection connection = DriverManager.getConnection(url_sql, user_sql, password_sql)) {

                    String stateUpdate = "UPDATE bot_users SET user_state = ? WHERE chat_id = '"+ chat_id +"'";
                    PreparedStatement statement = connection.prepareStatement(stateUpdate);
                    statement.setString(1, "1 USD in EUR");
                    statement.executeUpdate();

                } catch (SQLException e) {

                    System.out.println(e);

                }


                try(Connection connection = DriverManager.getConnection(url_sql, user_sql, password_sql)){

                    String sql_request = "SELECT user_state FROM bot_users WHERE chat_id = '" + chat_id + "';";
                    PreparedStatement statement = connection.prepareStatement(sql_request);
                    ResultSet resultSet = statement.executeQuery();

                    resultSet.next();
                    state.setState(resultSet.getString("user_state"));
                    System.out.println(state.getState());




                } catch (SQLException e){
                    System.out.println(e);
                }

                objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                ApiFile = new File("/Users/romanprib/Downloads/JavaExchangeCurrencyBot-main/JavaExchangeCurrencyBot/src/main/resources/data.json");
                SendMessage message = new SendMessage();
                message.setChatId(chat_id);


                try {
                    this.data = objectMapper.readValue(ApiFile, BankAPIParser.class);
                    message.setText("1 USD equals " + (1 / this.data.getResult()) + " EUR \nTo return to main menu type /menu");
                    execute(message);
                } catch (TelegramApiException | IOException e) {
                    e.printStackTrace();
                }
            } else if (update.hasMessage() && state.getState().equals("EUR to USD")) {



                SendMessage message = new SendMessage();
                message.setChatId(chat_id);
                double convEurToUsd = Double.parseDouble(update.getMessage().getText());

                objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                ApiFile = new File("/Users/romanprib/Downloads/JavaExchangeCurrencyBot-main/JavaExchangeCurrencyBot/src/main/resources/data.json");

                try {
                    this.data = objectMapper.readValue(ApiFile, BankAPIParser.class);
                    message.setText(convEurToUsd + " EUR is equal " + (convEurToUsd * this.data.getResult()) + " USD\n To return to main menu type /menu");
                    execute(message);
                } catch (IOException | TelegramApiException e) {
                    System.out.println(e);
                    throw new RuntimeException(e);
                }

                try(Connection connection = DriverManager.getConnection(url_sql, user_sql, password_sql)) {

                    String stateUpdate = "UPDATE bot_users SET user_state = ? WHERE chat_id = '"+ chat_id +"'";
                    PreparedStatement statement = connection.prepareStatement(stateUpdate);
                    statement.setString(1, "done with converting");
                    statement.executeUpdate();

                } catch (SQLException e) {

                    System.out.println(e);

                }

                try(Connection connection = DriverManager.getConnection(url_sql, user_sql, password_sql)){

                    String sql_request = "SELECT user_state FROM bot_users WHERE chat_id = '" + chat_id + "';";
                    PreparedStatement statement = connection.prepareStatement(sql_request);
                    ResultSet resultSet = statement.executeQuery();

                    resultSet.next();
                    state.setState(resultSet.getString("user_state"));
                    System.out.println(state.getState());




                } catch (SQLException e){
                    System.out.println(e);
                }


            } else if (message_text.equals("USD --> EUR")) {

                try(Connection connection = DriverManager.getConnection(url_sql, user_sql, password_sql)) {

                    String stateUpdate = "UPDATE bot_users SET user_state = ? WHERE chat_id = '"+ chat_id +"'";
                    PreparedStatement statement = connection.prepareStatement(stateUpdate);
                    statement.setString(1, "USD to EUR");
                    statement.executeUpdate();

                } catch (SQLException e) {

                    System.out.println(e);

                }

                try(Connection connection = DriverManager.getConnection(url_sql, user_sql, password_sql)){

                    String sql_request = "SELECT user_state FROM bot_users WHERE chat_id = '" + chat_id + "';";
                    PreparedStatement statement = connection.prepareStatement(sql_request);
                    ResultSet resultSet = statement.executeQuery();

                    resultSet.next();
                    state.setState(resultSet.getString("user_state"));
                    System.out.println(state.getState());




                } catch (SQLException e){
                    System.out.println(e);
                }

                SendMessage message = new SendMessage();
                message.setChatId(chat_id);
                message.setText("How much USD do you want to exchange?");
                try {
                    execute(message);
                } catch (TelegramApiException e) {
                    System.out.println(e);
                    e.printStackTrace();
                }





            } else if (update.hasMessage() && state.getState().equals("USD to EUR")) {

                SendMessage message = new SendMessage();
                message.setChatId(chat_id);
                double convUsdToEur = Double.parseDouble(update.getMessage().getText());

                objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                ApiFile = new File("/Users/romanprib/Downloads/JavaExchangeCurrencyBot-main/JavaExchangeCurrencyBot/src/main/resources/data.json");

                try {
                    this.data = objectMapper.readValue(ApiFile, BankAPIParser.class);
                    message.setText(convUsdToEur + " USD is equal " + ((1 / this.data.getResult()) * convUsdToEur) + " EUR\n To return to main menu type /menu");
                    execute(message);
                } catch (IOException | TelegramApiException e) {
                    System.out.println(e);
                    throw new RuntimeException(e);
                }

                try(Connection connection = DriverManager.getConnection(url_sql, user_sql, password_sql)) {

                    String stateUpdate = "UPDATE bot_users SET user_state = ? WHERE chat_id = '"+ chat_id +"'";
                    PreparedStatement statement = connection.prepareStatement(stateUpdate);
                    statement.setString(1, "done with converting");
                    statement.executeUpdate();

                } catch (SQLException e) {

                    System.out.println(e);

                }
                try(Connection connection = DriverManager.getConnection(url_sql, user_sql, password_sql)){

                    String sql_request = "SELECT user_state FROM bot_users WHERE chat_id = '" + chat_id + "';";
                    PreparedStatement statement = connection.prepareStatement(sql_request);
                    ResultSet resultSet = statement.executeQuery();

                    resultSet.next();
                    state.setState(resultSet.getString("user_state"));
                    System.out.println(state.getState());




                } catch (SQLException e){
                    System.out.println(e);
                }


            } else {


                SendMessage message = new SendMessage();
                message.setChatId(chat_id);
                message.setText("Unknown command, please use /menu");

                try{
                    execute(message);
                } catch (TelegramApiException e) {
                    e.printStackTrace();
                }

            }



        }

        }




    @Override
    public String getBotUsername() {
        // TODO
        return "JavaExchangeCurrencyBot";
    }

    @Override
    public String getBotToken() {
        // TODO
        return "6273731405:AAFjgf8cQLUujNpgj1FOMaVKd2ZV7-lo9Ro";
    }




}
