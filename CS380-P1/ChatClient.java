
import java.io.*;
import java.net.Socket;

public final class ChatClient {

	public static void main(String[] args) throws Exception {
		

		Socket socket = new Socket("www.codebank.xyz", 38001);
		BufferedReader con = new BufferedReader(new InputStreamReader(System.in));
		PrintStream out = new PrintStream(socket.getOutputStream(), true, "UTF-8");
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		System.out.print("Enter a username: ");
		out.println(con.readLine());
		Runnable getMessages = () -> {
			while (true) {
				String message = null;
				try {
					if ((message = in.readLine()) != null) {
						System.out.print("\n" + message + "\nSend Message> ");
					}
				} catch (Exception E) {

				}
			}
		};
		new Thread(getMessages).start();
		while (true) {

			String input;
			while (true) {
				System.out.print("Send Message> ");
				input = con.readLine();
				out.println(input);
				if (input.equals("exit")) {
					out.close();
					socket.close();
					con.close();
					System.exit(0);
				}

			}

		}

	}
}
