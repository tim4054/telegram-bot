package pro.sky.telegrambot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.YandexGPTClient;
import pro.sky.telegrambot.configuration.UserState;
import pro.sky.telegrambot.configuration.UserStateStorage;
import pro.sky.telegrambot.interfaces.CommandHandler;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class CommandNotifyAI implements CommandHandler {
    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private YandexGPTClient yandexGPTClient;

    @Autowired
    private NotificationTaskRepository repository;

    private Logger logger = LoggerFactory.getLogger(CommandNotifyAI.class);

    @Override
    public void handle(Update update) {
        Long chatID = update.message().chat().id();
        String messageText = update.message().text();

        UserState currentState = UserStateStorage.getState(chatID);

        logger.info("Current state for chatID {}: {}", chatID, currentState);

        // Обработка команды /AI
        if (messageText.equals(getCommand()) && currentState == UserState.DEFAULT) {
            logger.info("Command /notify received for chatID: {}", chatID);
            SendMessage sendMessage = new SendMessage(chatID, "Пожалуйста, отправьте напоминание");
            telegramBot.execute(sendMessage);
            logger.info("Sent message: Пожалуйста, отправьте напоминание");

            // Переводим пользователя в состояние ожидания напоминания
            UserStateStorage.setState(chatID, UserState.WAITING_FOR_NOTIFICATIONAI);
            logger.info("User state set to WAITING_FOR_NOTIFICATION for chatID: {}", chatID);
        }

        // Обработка сообщения, которое пользователь отправил после команды /AI
        if (currentState == UserState.WAITING_FOR_NOTIFICATIONAI) {
            logger.info("Entered WAITING_FOR_NOTIFICATIONAI state for chatID: {}", chatID);

            try {
                String prompt = "Извлеки из сообщения дату, время и текст напоминания и верни в формате ДД.ММ.ГГГГ ЧЧ:ММ Текст напоминания: " + messageText;
                String response = yandexGPTClient.generateText(prompt);

                if (response == null || response.isEmpty()) {
                    logger.error("Пустой ответ от Yandex GPT API");
                    SendMessage sendMessage = new SendMessage(chatID, "Не удалось обработать запрос. Попробуйте еще раз.");
                    telegramBot.execute(sendMessage);
                    return;
                }

                Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");
                Matcher matcher = pattern.matcher(response);

                if (matcher.matches()) {
                    logger.info("Pattern matched for message: {}", response);

                    String dateTimeString = matcher.group(1);
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

                    try {
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
                    } catch (DateTimeParseException e) {
                        logger.error("Неверный формат даты: {}", dateTimeString);
                        SendMessage sendMessage = new SendMessage(chatID, "Неверный формат даты. Пожалуйста, используйте формат: ДД.ММ.ГГГГ ЧЧ:ММ");
                        telegramBot.execute(sendMessage);
                    }
                } else {
                    logger.error("Ответ от Yandex GPT не соответствует ожидаемому формату: {}", response);
                    SendMessage sendMessage = new SendMessage(chatID, "Не удалось распознать напоминание. Пожалуйста, уточните запрос.");
                    telegramBot.execute(sendMessage);
                }
            } catch (IOException e) {
                logger.error("Ошибка при вызове Yandex GPT API: {}", e.getMessage());
                SendMessage sendMessage = new SendMessage(chatID, "Произошла ошибка при обработке запроса. Попробуйте позже.");
                telegramBot.execute(sendMessage);
            } catch (Exception e) {
                logger.error("Неизвестная ошибка: {}", e.getMessage());
                SendMessage sendMessage = new SendMessage(chatID, "Произошла неизвестная ошибка. Попробуйте позже.");
                telegramBot.execute(sendMessage);
            }
        }
    }

    @Override
    public String getCommand() {
        return "/AI";
    }
}