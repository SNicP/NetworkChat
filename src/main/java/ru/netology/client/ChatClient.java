package ru.netology.client;

import ru.netology.logger.Logger;

import java.io.*;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatClient {
    private static final String SETTING_PATH =
            "./src/main/resources/Settings.txt"; // путь к файлу с настройками для чата
    private static final Logger LOGGER = Logger.getInstance(); // создание экземпляра Logger

    private final static String DEFAULT_HOST = "localhost"; // значение хоста по умолчанию
    private static String serverHost; // переменная для хранения адреса хоста
    private final static int DEFAULT_PORT = 8080; // значение порта по умолчанию
    private static int serverPort; // переменная для хранения номера порта
    private static BufferedReader reader; // переменная для тестирования

    public static void main(String[] args) {

        // загрузка настроек из файла Settings.txt
        loadSettings(SETTING_PATH);

        try {
            // подключение к серверу
            Socket socket = new Socket(serverHost, serverPort);

            // для считывания данных создается выходной поток и буферизируется
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(
                            socket.getInputStream()));
            // создается объект PrintWriter для отправки исходящих сообщений через сокет
            PrintWriter out = new PrintWriter(
                    socket.getOutputStream(), true);
            // создание пула потоков
            ExecutorService executor = Executors.newFixedThreadPool(2);

            String message;// переменная для хранения сообщений
            // ожидание запроса на ввод имени
            while ((message = in.readLine()) != null) {
                System.out.println(message); // сообщение полученное от сервера
                BufferedReader userInput = new BufferedReader(
                        new InputStreamReader(System.in)); // объект BufferedReader для считывания ввода пользователя из консоли
                String username = userInput.readLine(); // запись сообщения из консоли в переменную
                out.println(username); // отправка значения переменной username в выходной поток

                // считывание ответа сервера
                message = in.readLine();
                // проверка начала получения сообщения от сервера с фразы "Имя уже занято"
                if (message.equals("Имя уже занято!")) {
                    System.out.println(message);// сообщение сервера выводится в консоль
                } else {
                    // считываем входящие сообщения и выводим в консоль в отдельном потоке
                    messagesListener(in, executor);

                    // отправка сообщения в PrintWriter в отдельном потоке
                    messagesSender(out, userInput, executor);

                    System.out.println(message); // сообщение сервера выводится в консоль
                    break;
                }
            }
            // завершение пула потоков
            executor.shutdown();
        } catch (IOException e) {
            LOGGER.log("ERROR", "Ошибка подключения к серверу: "
                    + e);
        }
    }

    // метод для считывания входящих сообщений из BufferedReader
    // в качестве параметров получает входной поток и пул потоков
    public static void messagesListener(BufferedReader in, ExecutorService executor) {
        // запуск потока для считывания сообщений от сервера
        executor.execute(() -> {
            try {
                // в цикле считываются сообщения от сервера и выводятся в консоль
                String message; // переменная для хранения полученных сообщений
                while ((message = in.readLine()) != null) {
                    System.out.println(message); // вывод сообщения в консоль
                    LOGGER.log("INFO", "Получение сообщения от сервера: "
                            + message); // логирование сообщения
                }
            } catch (IOException e) {
                LOGGER.log("ERROR", "Ошибка получения сообщения: "
                        + e); // обработка ошибки ввода/вывода
            }
        });
    }

    // метод для отправки сообщений на сервер
    // в качестве параметров принимает
    // out - для записи сообщений в выходной поток,
    // userInput - для чтения ввода пользователя из консоли
    // executor - пул потоков
    public static void messagesSender(PrintWriter out, BufferedReader userInput, ExecutorService executor) {
        // запуск потока для отправки сообщений
        executor.execute(() -> {
            try {
                String message; // переменная для хранения отправляемых сообщений
                // в цикле считываем сообщения от пользователя
                while ((message = userInput.readLine()) != null) {
                    out.println(message); //вывод сообщения в консоль
                    LOGGER.log("INFO", "Отправка сообщения на сервер: " + message); //логирование сообщения

                    // проверка на выход из цикла
                    if (message.equals("exit")) {
                        break; // выход из цикла и отключение от сервера
                    }
                }
            } catch (IOException e) {
                LOGGER.log("ERROR", "Ошибка отправки сообщения: "
                        + e); // обработка ошибки ввода/вывода
            }
        });
    }

    // считывание настроек из файла settings.txt
    // если считывание не удалось - в переменную HOST передается значение из переменной DEFAULT_HOST,
    // в переменную PORT передается значение из переменной DEFAULT_PORT
    public static void loadSettings(String file) {//в качестве параметра принимается имя файла
        // буферизация потока из файла настроек
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            // создание экземпляра класса Properties
            Properties properties = new Properties();
            // загрузка настроек из файла
            properties.load(bufferedReader);
            // сохранение настроек хоста в переменную
            serverHost = properties.getProperty("server.host");
            // сохранение настроек порта в переменную
            serverPort = Integer.parseInt(properties.getProperty("server.port"));
        } catch (IOException e) {
            LOGGER.log("WARNING", "Не удалось загрузить файл настроек. "
                    + e); // логирование ошибки загрузки настроек
            LOGGER.log("WARNING", "Использую настройки по умолчанию. \n" +
                    "Хост: " + DEFAULT_HOST +
                            "Порт: " + DEFAULT_PORT);
            // в случае возникновения исключения принимаем настройки по умолчанию
            if (serverHost == null || serverHost.isEmpty()) {
                // сохранение настроек хоста по умолчанию в переменную serverHost
                serverHost = DEFAULT_HOST;
            }
            // сохранение настроек порта по умолчанию в переменную serverPort
            serverPort = DEFAULT_PORT;
        }
    }

    //возвращает номер порта
    public static int getPort() {
        return serverPort;
    }

    //Метод принимает в качестве аргумента объект BufferedReader и сохраняет его
    // в статическом поле reader класса ChatClient, это дает возможность другим методам
    //получать доступ к этим данным
    public static void setReader(BufferedReader reader) {
        ChatClient.reader = reader;
    }

    //метод для установки пути к файлу лога, в качестве аргумента принимает строку,
    // которая содержит путь к файлу
    public static void setLogFile(String logFile) {
        LOGGER.setLogPath(logFile);
    }
}