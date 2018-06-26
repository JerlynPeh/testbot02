import org.telegram.telegrambots.api.methods.send.SendMessage;
import org.telegram.telegrambots.api.objects.Update;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.exceptions.TelegramApiException;

public class test02Bot extends TelegramLongPollingBot {


        public void onUpdateReceived(Update update) {
            //System.out.println(update.getMessage().getText());
            //System.out.println(update.getMessage().getFrom().getFirstName());

            String command=update.getMessage().getText();

            //var to send strings to bot
            SendMessage message = new SendMessage();

            if (command.equals((("/start")))){
                System.out.println("ohi there, " +update.getMessage().getFrom().getUserName() + "!");
                message.setText("hello there, " +update.getMessage().getFrom().getUserName() + "!");
            }

            //set chatID to reply to
            message.setChatId(update.getMessage().getChatId());

            //send out the message
            try {
                execute(message);
            } catch (TelegramApiException e) {
                e.printStackTrace();
            }
        }

        public String getBotUsername() {
            return "test02_bot";
        }

        public String getBotToken() {
            return "YOUR TOKEN";
        }
    }
