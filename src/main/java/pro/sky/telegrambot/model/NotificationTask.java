package pro.sky.telegrambot.model;





import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Objects;

@Entity
public class NotificationTask {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long chatId;
    private String notification;
    private LocalDateTime dateTime;
    private String status;


    public NotificationTask(Long chatId, String notification, LocalDateTime dateTime, String status) {
        this.chatId = chatId;
        this.notification = notification;
        this.dateTime = dateTime;
        this.status = status;
    }

    public NotificationTask() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getChatId() {
        return chatId;
    }

    public void setChatId(Long chatId) {
        this.chatId = chatId;
    }

    public String getNotification() {
        return notification;
    }

    public void setNotification(String notification) {
        this.notification = notification;
    }

    public LocalDateTime getDateTime() {
        return dateTime;
    }

    public void setDateTime(LocalDateTime dateTime) {
        this.dateTime = dateTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NotificationTask task = (NotificationTask) o;
        return Objects.equals(id, task.id) && Objects.equals(chatId, task.chatId) && Objects.equals(notification, task.notification) && Objects.equals(dateTime, task.dateTime) && Objects.equals(status, task.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, chatId, notification, dateTime, status);
    }

    @Override
    public String toString() {
        return "NotificationTask{" +
                "id=" + id +
                ", chatId=" + chatId +
                ", notification='" + notification + '\'' +
                ", dateTime=" + dateTime +
                ", status='" + status + '\'' +
                '}';
    }
}
