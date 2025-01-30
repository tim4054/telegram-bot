package pro.sky.telegrambot;

import okhttp3.*;
import org.json.JSONObject;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class YandexGPTClient {
    private static final String API_KEY = "";
    private static final String FOLDER_ID = "";
    private static final String YANDEX_GPT_URL = "";

    private final OkHttpClient httpClient = new OkHttpClient();

    public String generateText(String prompt) throws IOException {
        // Создаем JSON-тело запроса
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "general"); // Модель Yandex GPT
        requestBody.put("instruction", prompt); // Запрос пользователя
        requestBody.put("folderId", FOLDER_ID);

        // Создаем HTTP-запрос
        Request request = new Request.Builder()
                .url(YANDEX_GPT_URL)
                .addHeader("Authorization", "Api-Key " + API_KEY)
                .post(RequestBody.create(requestBody.toString(), MediaType.parse("application/json")))
                .build();

        // Отправляем запрос и получаем ответ
        try (Response response = httpClient.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("Ошибка при выполнении запроса: " + response.code() + " " + response.message());
            }

            // Парсим ответ
            JSONObject responseBody = new JSONObject(response.body().string());
            return responseBody.getJSONObject("result").getString("text");
        }
    }
}