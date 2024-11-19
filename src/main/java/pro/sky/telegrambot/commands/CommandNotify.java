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
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class CommandNotify implements CommandHandler {
    @Autowired
    private TelegramBot telegramBot;

    private Logger logger = LoggerFactory.getLogger(CommandNotify.class);

    @Autowired
    private NotificationTaskRepository repository;

    @Override
    public void handle(Update update) {
        Long chatID = update.message().chat().id();
        String messageText = update.message().text();

        UserState currentState = UserStateStorage.getState(chatID);

        logger.info("Current state for chatID {}: {}", chatID, currentState);

        // Обработка команды /notify
        if (messageText.equals(getCommand()) && currentState == UserState.DEFAULT) {
            logger.info("Command /notify received for chatID: {}", chatID);
            SendMessage sendMessage = new SendMessage(chatID, "Пожалуйста, отправьте напоминание в формате: ДД.ММ.ГГГГ ЧЧ:ММ Текст напоминания");
            telegramBot.execute(sendMessage);
            logger.info("Sent message: Пожалуйста, отправьте напоминание в формате: ДД.ММ.ГГГГ ЧЧ:ММ Текст напоминания");

            // Переводим пользователя в состояние ожидания напоминания
            UserStateStorage.setState(chatID, UserState.WAITING_FOR_NOTIFICATION);
            logger.info("User state set to WAITING_FOR_NOTIFICATION for chatID: {}", chatID);
        }

        // Обработка сообщения, которое пользователь отправил после команды /notify
        if (currentState == UserState.WAITING_FOR_NOTIFICATION) {
            logger.info("Entered WAITING_FOR_NOTIFICATION state for chatID: {}", chatID);

            Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");
            Matcher matcher = pattern.matcher(messageText);

            if (matcher.matches()) {
                logger.info("Pattern matched for message: {}", messageText);

                String dateTimeString = matcher.group(1);
                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");
                LocalDateTime date = LocalDateTime.parse(dateTimeString, formatter);

                repository.save(new NotificationTask(
                        chatID,
                        matcher.group(3),
                        date,
                        "no")
                );
                logger.info("Notification task saved for chatID: {}, notification: {}, date: {}", chatID, matcher.group(3), date);

                SendMessage sendMessage = new SendMessage(chatID, String.format("Напоминание добавлено: %s %s", dateTimeString, matcher.group(3)));
                telegramBot.execute(sendMessage);
                logger.info("Sent message: {}", String.format("Напоминание добавлено: %s, %s", date, matcher.group(3)));

                // Возвращаем пользователя в состояние по умолчанию
                UserStateStorage.setState(chatID, UserState.DEFAULT);
                logger.info("User state set to DEFAULT for chatID: {}", chatID);
            } else {
                logger.info("Pattern did not match for message: {}", messageText);
                SendMessage sendMessage = new SendMessage(chatID, "Неверный формат напоминания. Пожалуйста, используйте формат: ДД.ММ.ГГГГ ЧЧ:ММ Текст напоминания");
                telegramBot.execute(sendMessage);
                logger.info("Sent message: Неверный формат напоминания. Пожалуйста, используйте формат: ДД.ММ.ГГГГ ЧЧ:ММ Текст напоминания");
            }
        }
    }

    @Override
    public String getCommand() {
        return "/notify";
    }
}


