package ru.netology.server;

import ru.netology.logger.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private static final Logger LOGGER = Logger.getInstance(); // создание экземпляра Logger

    private final Socket socket; // установка и поддержка связи с сервером
    private BufferedReader in; // чтение входящих сообщений
    private PrintWriter out; // вывод данных в выходной поток
    String username; // хранение имени пользователя
    private ChatServer chatServer; // для доступа к методам класса ChatServer

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    // переопределение метода run() интерфейса Runnable
    @Override
    public void run() {
        try {
            in = new BufferedReader(new InputStreamReader(
                    socket.getInputStream())); // создание и буферизация потока для чтения информации из входящего потока
            out = new PrintWriter(
                    socket.getOutputStream(), true); // создание объекта для записи данных в выходной поток

            while (true) {
                out.println("Введите ваше имя: "); // отправка сообщения клиенту
                username = in.readLine(); // запрос на ввод имени, считывание в строку и сохранение в переменную
                if (isNameCorrect(username)) { // проверка имени на совпадение и корректность
                    ChatServer.users.put(username, socket);
                    broadcastMessage(username + " присоединился к чату."); // отправка сообщения всем клиентам
                    LOGGER.log("INFO", "Пользователь <"
                            + username + "> подключился к серверу."); // логирование сообщения
                    break;
                } else {
                    out.println("Имя уже занято. Введите другое."); // отправка сообщения клиенту
                }
            }

            // ожидание сообщений, если exit - отключаемся от чата,
            // если нет - рассылаем сообщения всем пользователям
            messagesListener();
        } catch (IOException e) {
            LOGGER.log("ERROR", "Ошибка в вводе имени: "
                    + e); // логирование ошибки ввода имени;
        } finally {
            disconnect(); // отключение от сервера
        }
    }

    // отправка и получение сообщений
    public void messagesListener() {
        try {
            String message; // переменная для хранения и обработки сообщений
            while ((message = in.readLine()) != null) {
                //если сообщение от клиента exit
                if (message.equals("exit")) {
                    out.println("Вы покинули чат."); // отправление сообщения клиенту
                    break; // выход из цикла
                }
                broadcastMessage(username + ": " + message); // иначе - отправление сообщения в чат
            }
        } catch (IOException e) {
            LOGGER.log("ERROR", "Ошибка обработки сообщения: "
                    + e); // логирование ошибки обработки сообщения
        } finally {
            disconnect(); // отключение от сервера
        }
    }

    // отправление сообщение всем пользователям, подключенным к серверу
    public void broadcastMessage(String message) {
        // логирование отправляемого сообщения
        LOGGER.log("INFO", message);

        ChatServer.users.forEach((username, client) -> {
            try {
                // создание объекта PrintWriter, связанного с выходным потоком клиентского сокета
                // параметр true указывает, что необходимо сбрасывать буфер после каждой записи
                PrintWriter clientOut = new PrintWriter(client.getOutputStream(), true);
                //записываем сообщение в выходной поток клиента
                clientOut.println(message);
            } catch (IOException e) {
                LOGGER.log("ERROR", "Ошибка отправки сообщения: "
                        + e); // логирование ошибки отправки сообщения
            }
        });
    }

    // проверка наличия такого имени в коллекции,
    public boolean isNameCorrect(String name) {
        if (name == null || name.trim().isEmpty()) { // проверка валидности имени
            return false;
        } else if (ChatServer.users.contains(name)) { // проверка имени на совпадение
            return false;
        } else {
            ChatServer.users.put(name, this.socket); // запись имени пользователя и его сокета в коллекцию
            return true;
        }
    }

    // отключение клиента от сервера чата
    public void disconnect() {
        try {
            socket.close(); // закрытие сокета
        } catch (IOException e) {
            LOGGER.log("ERROR", "Ошибка отключения пользователя от сервера чата: "
                    + e); // логирование ошибки отключения пользователя от сервера
        }
        ChatServer.users.remove(username); // удаление имени пользователя и его сокета в коллекцию
        broadcastMessage(username + " отключился от сервера."); // отправка сообщения всем клиентам
        LOGGER.log("INFO", "Пользователь <" + username + "> отключился от сервера.");//запись лога
    }

    //установка значения in для тестирования
    public void setIn(BufferedReader in) {
        this.in = in;
    }

    //установка значения out для тестирования
    public void setOut(PrintWriter out) {
        this.out = out;
    }

    // установка значения chatServer для тестирования
    public void setChatServer(ChatServer chatServer) {
        this.chatServer = chatServer;
    }
}
