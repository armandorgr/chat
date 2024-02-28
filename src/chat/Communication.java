package chat;

import java.io.BufferedWriter;
import java.io.IOException;

public class Communication {
    public static final String PREGUNTA_USUARIO_MANDAR = "Introduce el nombre de usuario a quien le quieres mandar un mensaje: ";
    public static final String CONEXION_ESTABLECIDA_STRING = "Conexion establecida para chatear";
    public static final String NO_SE_PUDO_ENCONTRAR_AL_USUARIO_STRING = "No se pudo encontrar el usuario a quien deseas mandar un mensaje";

    public static boolean writeTo(BufferedWriter br, String message) {
        boolean r = true;
        try {
            br.write(message + "\n");
            br.flush();
        } catch (IOException e) {
            r = false;
        }
        return r;

    }
}
