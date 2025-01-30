package pro.sky.telegrambot.listener;

import com.pengrad.telegrambot.model.Update;
import com.pengrad.telegrambot.model.Message;
import com.pengrad.telegrambot.model.Chat;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import pro.sky.telegrambot.commands.CommandNotify;
import pro.sky.telegrambot.commands.CommandStart;
import pro.sky.telegrambot.configuration.UserState;
import pro.sky.telegrambot.configuration.UserStateStorage;
import pro.sky.telegrambot.service.CommandService;

import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class TelegramBotUpdatesListenerTest {

    @Mock
    private CommandService commandService;

    @Mock
    private CommandNotify commandNotify;

    @Mock
    private CommandStart commandStart;

    @InjectMocks
    private TelegramBotUpdatesListener updatesListener;

    @Test
    public void testProcessWithStartCommand() {

        Long chatId = 1L;
        UserStateStorage.setState(chatId, UserState.DEFAULT);

        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("/start");

        List<Update> updates = List.of(update);

        //test
        updatesListener.process(updates);

        //check
        verify(commandStart).handle(update);
        verify(commandNotify, never()).handle(update);
        verify(commandService, never()).handleCommand(update);
    }

    @Test
    public void testProcessWithNotificationCommand() {

        Long chatId = 1L;
        UserStateStorage.setState(chatId, UserState.WAITING_FOR_NOTIFICATION);

        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("/notify");

        List<Update> updates = List.of(update);

        //test
        updatesListener.process(updates);

        //check
        verify(commandNotify).handle(update);
        verify(commandService, never()).handleCommand(update);
        verify(commandStart, never()).handle(update);
    }

    @Test
    public void testProcessWithUnsupportedCommand() {

        Long chatId = 1L;
        UserStateStorage.setState(chatId, UserState.DEFAULT);

        Update update = mock(Update.class);
        Message message = mock(Message.class);
        Chat chat = mock(Chat.class);

        when(update.message()).thenReturn(message);
        when(message.chat()).thenReturn(chat);
        when(chat.id()).thenReturn(chatId);
        when(message.text()).thenReturn("unsupportedCommand");

        List<Update> updates = List.of(update);

        //test
        updatesListener.process(updates);

        //check
        verify(commandService).handleCommand(update);
        verify(commandNotify, never()).handle(update);
        verify(commandStart, never()).handle(update);
    }
}