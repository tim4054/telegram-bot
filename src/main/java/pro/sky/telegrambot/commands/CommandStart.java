package pro.sky.telegrambot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.configuration.UserState;
import pro.sky.telegrambot.configuration.UserStateStorage;
import pro.sky.telegrambot.interfaces.CommandHandler;


@Component
public class CommandStart implements CommandHandler {
    @Autowired
    private TelegramBot telegramBot;

    private Logger logger = LoggerFactory.getLogger(CommandStart.class);

    @Override
    public void handle(Update update) {
        Long chatID = update.message().chat().id();

        String username = update.message().chat().firstName();
        String text = username + ", добро пожаловать в Главное меню!\nСписок доступных команд:\n/notify - добавить напоминание" +
                "\n/start - выход в Главное меню";
        SendMessage sendMessage = new SendMessage(chatID, text);

        telegramBot.execute(sendMessage);
        logger.info("Sent message: {}", text);
        // Возвращаем пользователя в состояние по умолчанию
        UserStateStorage.setState(chatID, UserState.DEFAULT);
        logger.info("User state set to DEFAULT for chatID: {}", chatID);
    }

    @Override
    public String getCommand() {
        return "/start";
    }
}
