package chat;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;

import static chat.Communication.writeTo;

public class Servidor {
    private final Map<String, Cliente> users = new HashMap();
    ReentrantLock lock = new ReentrantLock();

    public void startServer() throws IOException {
        try (ServerSocket servidor = new ServerSocket(1234)) {
            while (true) {
                Socket cliente = servidor.accept();
                System.out.println("Cliente conectado");
                new Thread(new Cliente(cliente)).start();
            }
        }
    }

    private void addUser(String username, Cliente socket) {
        try {
            lock.lock();
            users.put(username, socket);
        } finally {
            lock.unlock();
        }
    }

    class Cliente implements Runnable {
        private final Socket socket;
        private String user = "";
        private boolean logeado = false;

        public Cliente(Socket socket) {
            this.socket = socket;
        }

        private boolean logIn(BufferedReader bufferedReader, BufferedWriter bufferedWriter) throws IOException {
            boolean r = true;
            String username = bufferedReader.readLine();
            if (!users.containsKey(username)) {
                writeTo(bufferedWriter, "registro-correcto");
                addUser(username, this);
                user = username;
            } else {
                r = false;
                writeTo(bufferedWriter, "registro-incorrecto, el usuario ya existe");
            }
            return r;
        }

        private void sendMessage(String to, String message) {

            if (users.containsKey(to)) {
                try {
                    BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(users.get(to).getSocket().getOutputStream()));
                    bufferedWriter.write(user + " - " + message + "\n");
                    bufferedWriter.flush();
                    System.out.println(user + " to " + to + message);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        public Socket getSocket() {
            return this.socket;
        }

        @Override
        public void run() {
            try {
                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
                BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));

                while (true) {
                    ACTION accion = ACTION.valueOf(bufferedReader.readLine()); //leer acción del usuario
                    System.out.println("ACCION LEIDA " + accion.name());
                    switch (accion) {
                        case LOGIN:
                            logeado = logIn(bufferedReader, bufferedWriter);
                            break;
                        case CHAT:
                            if (!logeado) {
                                writeTo(bufferedWriter, "No te has identificado");
                                break;
                            }
                            String username = "";
                            
                                username = bufferedReader.readLine();
                                if (!users.containsKey(username)) {
                                    writeTo(bufferedWriter, Communication.NO_SE_PUDO_ENCONTRAR_AL_USUARIO_STRING);
                                    break;
                                }else {
                                	writeTo(bufferedWriter, Communication.CONEXION_ESTABLECIDA_STRING);
                                }
                           
                            String mensaje;
                            while ((mensaje = bufferedReader.readLine()) != null) {
                                if (mensaje.equals(ACTION.EXIT.name())) {
                                    writeTo(bufferedWriter, "Conexión cerrada.");
                                    break;
                                }
                                sendMessage(username, mensaje);
                            }
                            break;
                    }
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
