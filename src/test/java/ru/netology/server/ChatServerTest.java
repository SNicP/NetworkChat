package ru.netology.server;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;
import java.io.*;
import java.nio.file.*;
import java.util.Collections;


class ChatServerTest {

    @Test
    void testLoadSettings() throws IOException {
        //given
        int serverPortExpected = 8080;

        //when
        File settingsFile = new File("settings.txt");
        settingsFile.delete();

        //then
        int serverPortActual = ChatServer.loadSettings(settingsFile.getAbsolutePath());

        assertEquals(serverPortExpected, serverPortActual);
    }
}