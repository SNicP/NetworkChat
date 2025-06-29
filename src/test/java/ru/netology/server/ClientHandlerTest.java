package ru.netology.server;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.BufferedReader;
import java.io.PrintWriter;
import java.net.Socket;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ClientHandlerTest {

    @Mock
    private Socket socket;//создание Mock-объекта для класса Socket

    @Mock
    private BufferedReader in;//создание Mock-объекта для имитации BufferedReader, чтобы управлять вводом данных

    @Mock
    private PrintWriter out;//создание Mock-объекта для имитации PrintWriter, чтобы управлять выводом данных

    @Mock
    private ChatServer chatServer;//создание Mock-объекта для класса ChatServer

    private ClientHandler clientHandler;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);

        clientHandler = spy(new ClientHandler(socket));

        //установка Mock-объектов
        clientHandler.setIn(in);//устанавливается Mock-объект in в качестве входного потока
        clientHandler.setOut(out);//устанавливается Mock-объект out в качестве выходного потока
        clientHandler.setChatServer(chatServer);//устанавливается Mock-объект chatServer в качестве сервера чата
    }

    @Test
    public void testIsNameCorrect() {
        //given
        String name = "Петр";

        //when
        boolean resultActual = clientHandler.isNameCorrect(name);

        //then
        assertTrue(resultActual);
    }


    @Test
    public void testDisconnect() {
        //given
        doNothing().when(clientHandler).disconnect();

        //when
        clientHandler.disconnect();

        //then
        verify(clientHandler, times(1)).disconnect();
    }
}
