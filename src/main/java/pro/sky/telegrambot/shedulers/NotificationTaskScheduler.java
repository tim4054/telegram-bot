package pro.sky.telegrambot.shedulers;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.request.SendMessage;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pro.sky.telegrambot.model.NotificationTask;
import pro.sky.telegrambot.repository.NotificationTaskRepository;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
public class NotificationTaskScheduler {

    private final NotificationTaskRepository repository;
    private final TelegramBot telegramBot;

    public NotificationTaskScheduler(NotificationTaskRepository repository, TelegramBot telegramBot) {
        this.repository = repository;
        this.telegramBot = telegramBot;
    }

    @Scheduled(cron = "0 0/1 * * * *")
    public void run() {
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.MINUTES);
        List<NotificationTask> tasks = repository.findByDateTime(now);

        for (NotificationTask task : tasks) {
            Long chatId = task.getChatId();
            SendMessage message = new SendMessage(chatId, task.getNotification());
            telegramBot.execute(message);
            task.setStatus("sent");
            repository.save(task);
        }
    }
}