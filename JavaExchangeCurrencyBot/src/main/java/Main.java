import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;


public class Main {
    public static void main(String [] args){
        BankAPIParser json;


        try {
            TelegramBotsApi botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(new JavaBot());
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
        System.out.println("Bot successfully started");

        String date = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
        System.out.println(date);

        BankAPIParser bankAPIParser = new BankAPIParser();
        ObjectMapper objectMapper = new ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        File ApiFile = new File("src/main/resources/data.json");

        try {
            bankAPIParser = objectMapper.readValue(ApiFile, BankAPIParser.class);
            if (bankAPIParser.getDate().equals(date)){
                System.out.println("true");
            } else {
                System.out.println("false");
            }
        } catch (IOException e) {
            System.out.println(e);
            throw new RuntimeException(e);
        }





    }
}



