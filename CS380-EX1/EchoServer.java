
import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public final class EchoServer {

	public static void main(String[] args) throws Exception {
		try (ServerSocket serverSocket = new ServerSocket(22222)) {

			while (true) {
				Socket socket = serverSocket.accept();
					Runnable client = () -> {
						startThread(socket);
					};
					new Thread(client).start();
				}
			
		}

	}
	public static void startThread(Socket socket){
		try {
			
			PrintStream out = new PrintStream(socket.getOutputStream(), true, "UTF-8");
			BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			String address = socket.getInetAddress().getHostAddress();
			System.out.printf("Client connected: %s%n", address);

			String input;
			while ((input = in.readLine()) != null) {

				out.println(input);
				System.out.println("Client said: " + input);
				if (input.equals("exit")) {
					System.out.printf("Client disconnected: %s%n", address);
					break;
				}

			}

		} catch (Exception E) {

		}
	}
}
