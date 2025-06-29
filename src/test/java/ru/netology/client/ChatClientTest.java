package ru.netology.client;

import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.*;
import org.mockito.*;
import ru.netology.logger.Logger;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ChatClientTest {

    @Test
    void testLoadSettings() throws IOException {
        //given
        String filePath = "./src/main/java/ru/netology/server/Settings.txt";

        //when
        BufferedReader mockedReader = mock(BufferedReader.class);
        when(mockedReader.readLine()).thenReturn("port=8888");
        ChatClient.setReader(mockedReader);
        ChatClient.loadSettings(filePath);

        //then
        assertEquals(8888, ChatClient.getPort());
    }

    @Test
    void testMessagesListener() throws IOException, InterruptedException {
        //given
        BufferedReader mockedInput = Mockito.mock(BufferedReader.class);
        ExecutorService executor = Executors.newSingleThreadExecutor();

        when(mockedInput.readLine()).thenReturn("exit", "Test message");

        //when
        String tempFileName = "temp.txt";
        ChatClient.setLogFile(tempFileName);
        ChatClient.messagesListener(mockedInput, executor);

        executor.shutdown();

        executor.awaitTermination(200, TimeUnit.MILLISECONDS);

        BufferedReader logReader = new BufferedReader(new FileReader(tempFileName));
        String lastLine = null;
        String line;
        while ((line = logReader.readLine()) != null) {
            lastLine = line;
        }

        //then
        assertNotNull(lastLine);
        assertTrue(lastLine.contains("Test message"));
        logReader.close();

        Thread.sleep(500);
        new File(tempFileName).delete();
    }

    @Test
    void testMessagesSender() throws IOException, InterruptedException {
        //given
        PrintWriter mockedOutput = mock(PrintWriter.class);
        BufferedReader mockedUserInput = mock(BufferedReader.class);
        ExecutorService executor = Executors.newSingleThreadExecutor();
        when(mockedUserInput.readLine()).thenReturn("test message", "exit");

        //when
        String tempFileName = "temp.txt";
        ChatClient.setLogFile(tempFileName);
        ChatClient.messagesSender(mockedOutput, mockedUserInput, executor);

        executor.shutdown();

        executor.awaitTermination(200, TimeUnit.MILLISECONDS);

        //then
        verify(mockedOutput).println("test message");
        verify(mockedOutput).println("exit");

        BufferedReader logReader = new BufferedReader(new FileReader(tempFileName));
        String lastLine = null, line;
        while ((line = logReader.readLine()) != null) {
            lastLine = line;
        }

        assertNotNull(lastLine);
        assertTrue(lastLine.contains("Отправка сообщения на сервер: exit"));

        logReader.close();

        Thread.sleep(500);
        new File(tempFileName).delete();
    }
}