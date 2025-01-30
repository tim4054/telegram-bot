package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.TelegramBot;
import com.pengrad.telegrambot.UpdatesListener;
import com.pengrad.telegrambot.model.Update;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import pro.sky.telegrambot.commands.CommandNotify;
import pro.sky.telegrambot.commands.CommandNotifyAI;
import pro.sky.telegrambot.commands.CommandStart;
import pro.sky.telegrambot.configuration.UserState;
import pro.sky.telegrambot.configuration.UserStateStorage;
import pro.sky.telegrambot.service.CommandService;

import javax.annotation.PostConstruct;
import java.util.List;

@Service
public class TelegramBotUpdatesListener implements UpdatesListener {

    private Logger logger = LoggerFactory.getLogger(TelegramBotUpdatesListener.class);

    @Autowired
    private TelegramBot telegramBot;

    @Autowired
    private CommandService commandService;

    @Autowired
    private CommandNotify commandNotify;
    @Autowired
    private CommandNotifyAI commandNotifyAI;

    @Autowired
    private CommandStart commandStart;

    @PostConstruct
    public void init() {
        telegramBot.setUpdatesListener(this);
    }

    @Override
    public int process(List<Update> updates) {
        updates.forEach(update -> {
            Long chatID = update.message().chat().id();
            UserState currentState = UserStateStorage.getState(chatID);
            logger.info("Processing update: {}", update);

            if (update.message() != null && update.message().text() != null
                    && !update.message().text().equals("/start")) {
                if (currentState == UserState.WAITING_FOR_NOTIFICATION) {
                    commandNotify.handle(update);
                } else if (currentState == UserState.WAITING_FOR_NOTIFICATIONAI) {
                    commandNotifyAI.handle(update);
                } else {
                    commandService.handleCommand(update);
                }
            } else if (update.message().text().equals("/start")) {
                commandStart.handle(update);
            } else {
                logger.warn("Received update without text message: {}", update);
            }
        });
        return UpdatesListener.CONFIRMED_UPDATES_ALL;
    }
}

