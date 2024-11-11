package pro.sky.telegrambot.interfaces;

import com.pengrad.telegrambot.model.Update;

public interface CommandHandler {
    void handle(Update update);
    String getCommand();
}