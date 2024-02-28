package chat;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        Servidor servidor = new Servidor();
        System.out.println("servidor iniciado");
        servidor.startServer();

    }
}
