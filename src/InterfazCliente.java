import static chat.Communication.writeTo;

import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import chat.ACTION;
import chat.Communication;

import java.awt.Font;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.awt.event.ActionEvent;
import javax.swing.JTextArea;

public class InterfazCliente {

	private JFrame frame;
	private JTextField textFieldNombre;
	private ACTION accion = ACTION.EXIT;
	private JLabel lblRespuestaServidor;
	private JTextArea mensajesRecibidos;
	private boolean enviar = false;
	private JTextArea textAreaMensaje;
	private boolean enviarMensaje = false;
	private JButton btnLogin;
	private JButton btnEnviar;
	private JLabel lblMain;
	private JButton btnEnviarMensaje;
	private JButton btnExit;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					InterfazCliente window = new InterfazCliente();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public InterfazCliente() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 990, 620);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);

		JLabel lblNewLabel = new JLabel("Chat");
		lblNewLabel.setFont(new Font("Tahoma", Font.PLAIN, 29));
		lblNewLabel.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel.setBounds(397, 11, 127, 63);
		frame.getContentPane().add(lblNewLabel);

		textFieldNombre = new JTextField();
		textFieldNombre.setBounds(49, 187, 317, 20);
		frame.getContentPane().add(textFieldNombre);
		textFieldNombre.setColumns(10);

		btnLogin = new JButton("Login");

		btnLogin.setBounds(49, 345, 89, 23);
		frame.getContentPane().add(btnLogin);

		btnEnviar = new JButton("Conectar");

		btnEnviar.setBounds(159, 345, 89, 23);
		frame.getContentPane().add(btnEnviar);

		textAreaMensaje = new JTextArea();
		textAreaMensaje.setBounds(49, 218, 324, 110);
		frame.getContentPane().add(textAreaMensaje);

		mensajesRecibidos = new JTextArea();
		mensajesRecibidos.setBounds(488, 95, 397, 392);
		frame.getContentPane().add(mensajesRecibidos);

		JLabel lblMensajesRecibidos = new JLabel("Mensajes Recibidos");
		lblMensajesRecibidos.setFont(new Font("Tahoma", Font.PLAIN, 17));
		lblMensajesRecibidos.setHorizontalAlignment(SwingConstants.CENTER);
		lblMensajesRecibidos.setBounds(605, 60, 167, 24);
		frame.getContentPane().add(lblMensajesRecibidos);

		lblMain = new JLabel("Nombre");
		lblMain.setHorizontalAlignment(SwingConstants.CENTER);
		lblMain.setBounds(49, 162, 317, 14);
		frame.getContentPane().add(lblMain);

		lblRespuestaServidor = new JLabel("...");
		lblRespuestaServidor.setHorizontalAlignment(SwingConstants.CENTER);
		lblRespuestaServidor.setBounds(49, 426, 324, 61);
		frame.getContentPane().add(lblRespuestaServidor);

		btnEnviarMensaje = new JButton("Enviar Mensaje");

		btnEnviarMensaje.setBounds(277, 345, 136, 23);
		frame.getContentPane().add(btnEnviarMensaje);

		btnExit = new JButton("Exit");
		btnExit.setBounds(49, 391, 89, 23);
		frame.getContentPane().add(btnExit);

		btnEnviarMensaje.setVisible(false);
		btnEnviar.setVisible(false);
		textAreaMensaje.setVisible(false);
		btnExit.setVisible(false);

		btnExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				accion = ACTION.EXIT;
				enviarMensaje = true;
			}
		});

		btnEnviar.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				accion = ACTION.CHAT;
				enviar = true;
			}
		});

		btnEnviarMensaje.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				enviarMensaje = true;
			}
		});

		btnLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					accion = getAction("LOGIN");
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});

		new Thread(() -> {
			try {
				Cliente cliente = new Cliente();
			} catch (IOException | InterruptedException e1) {
				e1.printStackTrace();
			}
		}).start();
	}

	private ACTION getAction(String actionString) throws IOException {
		boolean valid = true;
		ACTION actionEnum = ACTION.EXIT;
		do {
			try {
				actionEnum = ACTION.valueOf(actionString);
				valid = true;
			} catch (IllegalArgumentException e) {
				System.out.println("ACCIÓN INCORRECTA (LOGIN, CHAT, EXIT)");
				valid = false;
			}
		} while (!valid);
		return actionEnum;
	}

	public class Cliente {
		private boolean logeado = false;
		private String ultimoMensaje = "";

		public Cliente() throws IOException, InterruptedException {
			Socket conn = new Socket("localhost", 1234);
			BufferedReader lector = new BufferedReader(new InputStreamReader(System.in));
			BufferedWriter bufferedWriter = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			leerMensajes(bufferedReader);
			while (true) {
				switch (accion) {
				case LOGIN:
					login(bufferedWriter, bufferedReader, textFieldNombre.getText());
					textFieldNombre.setText("");
					break;
				case CHAT:
					accion = ACTION.DEFAULT;
					writeTo(bufferedWriter, ACTION.CHAT.name());
					if (!logeado)
						break;
					String to = textFieldNombre.getText();
					writeTo(bufferedWriter, to);
					Thread.sleep(100);
					while (enviar && !ultimoMensaje.equals(Communication.NO_SE_PUDO_ENCONTRAR_AL_USUARIO_STRING)) {
						textAreaMensaje.setVisible(true);
						btnEnviarMensaje.setVisible(true);
						btnEnviar.setVisible(false);
						textFieldNombre.setEditable(false);
						btnExit.setVisible(true);
						if (enviarMensaje) {
							String mensajeString = textAreaMensaje.getText();
							if (accion.equals(ACTION.EXIT)) {
								writeTo(bufferedWriter, accion.name());
								enviarMensaje = false;
								enviar = false;
								accion = ACTION.DEFAULT;
								btnEnviar.setVisible(true);
								textAreaMensaje.setVisible(false);
								btnEnviarMensaje.setVisible(false);
								textFieldNombre.setEditable(true);
								btnExit.setVisible(false);
								break;
							}
							writeTo(bufferedWriter, mensajeString);
							mensajesRecibidos.setText(mensajesRecibidos.getText() + "\n" + "TU: " + mensajeString);
							enviarMensaje = false;
						}

					}
					break;
				}
			}
		}

		private boolean login(BufferedWriter bw, BufferedReader br, String username) {
			writeTo(bw, ACTION.LOGIN.name());
			writeTo(bw, username);
			accion = ACTION.DEFAULT;
			return logeado;
		}

		private void leerMensajes(BufferedReader lector) {
			new Thread(() -> {
				String mensaje;
				try {
					while ((mensaje = lector.readLine()) != null) {
						ultimoMensaje = mensaje;
						mensajesRecibidos.setText(mensajesRecibidos.getText() + "\n" + mensaje);
						if (mensaje.equals("registro-correcto")) {
							logeado = true;
							btnLogin.setVisible(false);
							btnEnviar.setVisible(true);
							lblMain.setText("Conectar con: ");
						}

					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}).start();
		}
	}
}
