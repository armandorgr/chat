package chat;

import java.io.*;
import java.net.Socket;

import static chat.Communication.writeTo;

public class Cliente {
    private static boolean logeado = false;
    private static String ultimoMensaje = "";

    public Cliente() throws IOException, InterruptedException {
        Socket conn = new Socket("localhost", 1234);
        BufferedReader lector = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));

        while (true) {
            leerMensajes(bufferedReader);
            ACTION accion = getAction(lector);
            switch (accion) {
                case LOGIN:
                    login(bufferedWriter, bufferedReader, lector.readLine());
                    break;
                case CHAT:
                    writeTo(bufferedWriter, ACTION.CHAT.name());
                    if (!logeado) break;
                    while(ultimoMensaje.equals(Communication.PREGUNTA_USUARIO_MANDAR)){
                        String to = lector.readLine();
                        writeTo(bufferedWriter, to);
                    }
                    String enviar;
                    while (!(enviar = lector.readLine()).equals("")) {
                        if (enviar.equals(ACTION.EXIT.name())) {
                            writeTo(bufferedWriter, enviar);
                            break;
                        }
                        writeTo(bufferedWriter, enviar);
                    }
                    break;
            }
        }
    }

    private static ACTION getAction(BufferedReader br) throws IOException {
        boolean valid = true;
        ACTION actionEnum = ACTION.EXIT;
        do{
            try{
                String action = br.readLine();
                actionEnum = ACTION.valueOf(action);
                valid = true;
            }catch (IllegalArgumentException e){
                System.out.println("ACCIÓN INCORRECTA (LOGIN, CHAT, EXIT)");
                valid = false;
            }
        }while (!valid);
        return actionEnum;
    }

    private static boolean login(BufferedWriter bw, BufferedReader br, String username) {
        writeTo(bw, ACTION.LOGIN.name());
        writeTo(bw, username);
        return logeado;
    }

    private static void leerMensajes(BufferedReader lector) {
        new Thread(() -> {
            String mensaje;
            try {
                while ((mensaje = lector.readLine()) != null) {
                    ultimoMensaje = mensaje;
                    System.out.println("Lector de mensajes: " + mensaje);
                    if (mensaje.equals("registro-correcto")) logeado = true;
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }
}
