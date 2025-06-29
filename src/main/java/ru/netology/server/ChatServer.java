package ru.netology.server;

import ru.netology.logger.Logger;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatServer {
    private static final String SETTING_PATH =
            "./src/main/java/ru/netology/server/Settings.txt"; // путь к файлу с настройками для чата
    private static final Logger LOGGER = Logger.getInstance(); // создание экземпляра Logger

    private final static int DEFAULT_PORT = 8080; // значение порта по умолчанию
    private static int serverPort;

    protected static ConcurrentHashMap<String, Socket> users =
            new ConcurrentHashMap<>(); // коллекция хранения имен пользователей и их сокетов

    private static final int threadPoolSize = Runtime.getRuntime().availableProcessors(); // количество потоков в пуле

    public static void main(String[] args) {
        serverPort = loadSettings(SETTING_PATH); //загрузка настроек сервера из файла и присваивание их переменной PORT

        try {
            ServerSocket serverSocket = new ServerSocket(serverPort); // запуск сервера

            String start = "Сервер чата запущен. Порт: " + serverPort; // создание переменной с текстом сообщения
            System.out.println(start); // вывод сообщения в консоль
            LOGGER.log("INFO", start); // логирование сообщения

            ExecutorService threadPool = Executors.newFixedThreadPool(threadPoolSize); // создание пула потоков

            while (!serverSocket.isClosed()) {
                Socket clientSocket = serverSocket.accept(); // ожидание подключения клиентов

                // в поток из пула передается объект ClientHandler
                // для обработки клиентского соединения,
                // далее поток работает в фоновом режиме
                threadPool.execute(new ClientHandler(clientSocket));
            }
        } catch (IOException e) {
            LOGGER.log("ERROR", "Ошибка запуска сервера чата: "
                    + e); // логирование ошибки загрузки сервера
        }
    }

    //считывание настроек из файла и сохранение их в переменную PORT
    public static int loadSettings(String file) {
        int result;
        //буферизация потока и считывание данных из файла настроек
        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(file))) {
            // создание экземпляра класса Properties
            Properties properties = new Properties();
            // загрузка настроек из файла
            properties.load(bufferedReader);
            // сохранение настроек порта в переменную
            result = Integer.parseInt(properties.getProperty("server.port"));
        } catch (IOException e) {
            LOGGER.log("WARNING", "Не удалось загрузить файл настроек. "
                    + e); // логирование исключения
            LOGGER.log("WARNING", "Использую порт по умолчанию. " + DEFAULT_PORT);
            // сохранение настроек порта по умолчанию в переменную serverPort
            result = DEFAULT_PORT;
        }
        return result;
    }

    // метод getServerPort для тестирования
    public static int getServerPort() {
        return serverPort;
    }
}