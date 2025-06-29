package ru.netology.logger;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    private static Logger instance = null; // начальное значение Logger для реализации паттерна Singleton
    private static String log_path = new File("./src/main/java/ru/netology/logger/file.log")
            .getAbsolutePath(); // путь к файлу логирования
    private static final DateTimeFormatter TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"); // формат даты и времени

    // закрываем доступ к конструктору
    private Logger() {}

    public static Logger getInstance() {
        // если экземпляра класса не существует - создаем новый
        if (instance == null){
            instance = new Logger();
        }
        // возвращаем экземпляр класса
        return instance;
    }


    public static void log(String messageType, String message) {
        try(PrintWriter writer = new PrintWriter(new FileWriter(log_path, true))) {
            writer.write(messageType + ": [" + TIME_FORMATTER.format(LocalDateTime.now()) + "] " + message + "\n");
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // метод для изменения пути к файлу логирования
    public void setLogPath(String file) {
        log_path = file;
    }
}
