package lk.ijse.dep12.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


public class ServerAppInitializer {
    private static final List<Socket> CLIENT_LIST = new CopyOnWriteArrayList<>();

    public static void main(String[] args) throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(5050)) {
            while (true) {
                Socket localSocket = serverSocket.accept();
                CLIENT_LIST.add(localSocket);
                broadCastMessage(localSocket, "Entered: " + localSocket.getRemoteSocketAddress() + "\n");

                new Thread(() -> {
                    try {
                        InputStream is = localSocket.getInputStream();
                        localSocket.setKeepAlive(true);
                        ObjectInputStream ois = new ObjectInputStream(is);
                        while (true) {
                            Object object = ois.readObject();
                            broadCastMessage(localSocket, object);
                        }


                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    } catch (ClassNotFoundException e) {
                        throw new RuntimeException(e);
                    }
                }).start();
            }
        }

    }

    private static void broadCastMessage(Socket client, Object message) {
        new Thread(() -> {

            for (Socket socket : CLIENT_LIST) {
                if (socket == client) continue;
                try {
                    if (socket.isConnected()) {
                        //socket.getOutputStream().write(message);
                        new ObjectOutputStream(socket.getOutputStream()).writeObject(message);
                    }
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            //same can be done like this
//            CLIENT_LIST.stream().filter(socket -> socket != client).forEach(socket -> {
//                try {
//                    socket.getOutputStream().write(message.getBytes());
//                } catch (IOException e) {
//                    throw new RuntimeException(e);
//                }
//            });

        }).start();
    }
}
