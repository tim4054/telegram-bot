package pro.sky.telegrambot.commands;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.request.SendMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.interfaces.CommandHandler;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.util.regex.Matcher;
import java.util.regex.Pattern;


@Component
public class CommandNotify implements CommandHandler {
    @Autowired
    private TelegramBot telegramBot;

    private NotificationTask task;

    private Logger logger = LoggerFactory.getLogger(CommandStart.class);

    @Autowired
    private NotificationTaskRepository repository;

    @Override
    public void handle(Update update) {
        Long chatID = update.message().chat().id();
        String notify = update.message().text();
        String item = "";
        String date = "";
        String status = "no";

        String text = "Напоминание добавлено: ";
        Pattern pattern = Pattern.compile("(\\d{2}\\.\\d{2}\\.\\d{4}\\s\\d{2}:\\d{2})(\\s+)(.+)");
        Matcher matcher = pattern.matcher(notify);
        if (matcher.matches()) {
            date = matcher.group(1);
            item = matcher.group(3);
            text = text + " " + item + " " + date;
        }


        NotificationTask task = new NotificationTask(chatID, item, date, status);
        repository.save(task);

        SendMessage sendMessage = new SendMessage(chatID, text);
        telegramBot.execute(sendMessage);
        logger.info("Sent message: {}", text);
    }

    @Override
    public String getCommand() {
        return "01.01.2022 20:00 Сделать домашнюю работу";
    }
}
