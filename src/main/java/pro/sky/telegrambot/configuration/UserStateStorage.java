package pro.sky.telegrambot.configuration;

import java.util.HashMap;
import java.util.Map;

public class UserStateStorage {
    private static final Map<Long, UserState> userStates = new HashMap<>();

    public static UserState getState(Long chatId) {
        return userStates.getOrDefault(chatId, UserState.DEFAULT);
    }

    public static void setState(Long chatId, UserState state) {
        userStates.put(chatId, state);
    }
}