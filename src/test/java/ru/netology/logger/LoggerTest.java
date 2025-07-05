package ru.netology.logger;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class LoggerTest {

    private Path tempLogFile;

    @BeforeEach
    public void setUp() throws IOException {
        tempLogFile = Files.createTempFile("test_log", ".log");
        Logger.getInstance().setLogPath(tempLogFile.toString());
        // Очистка файла перед каждым тестом
        Files.writeString(tempLogFile, "");
    }

    @AfterEach
    public void tearDown() throws IOException {
        Files.deleteIfExists(tempLogFile);
    }

    @Test
    public void testSingletonInstance() {
        Logger logger1 = Logger.getInstance();
        Logger logger2 = Logger.getInstance();
        assertSame(logger1, logger2, "Должны быть одинаковые экземпляры (синглтон)");
    }

    @Test
    public void testLogWritesToFile() throws IOException {
        String messageType = "INFO";
        String message = "Test message";

        // Записываем лог
        Logger.getInstance().log(messageType, message);

        // Читаем содержимое файла
        String content = Files.readString(tempLogFile);

        assertTrue(content.contains(messageType));
        assertTrue(content.contains(message));
    }

    @Test
    public void testSetLogPathChangesPath() throws IOException {
        Path newTempFile = Files.createTempFile("new_log", ".log");
        try {
            Logger.getInstance().setLogPath(newTempFile.toString());

            String messageType = "ERROR";
            String message = "Error occurred";

            Logger.getInstance().log(messageType, message);

            String content = Files.readString(newTempFile);
            assertTrue(content.contains(messageType));
            assertTrue(content.contains(message));
        } finally {
            Files.deleteIfExists(newTempFile);
        }
    }
}