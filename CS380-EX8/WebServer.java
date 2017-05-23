import java.net.ServerSocket;
import java.net.Socket;
import java.io.*;

public class WebServer {

	public static void main(String[] args) throws Exception {

		ServerSocket serverSocket = new ServerSocket(8080);

		while (true) {

			Socket socket = serverSocket.accept();

			InputStream is = socket.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			String in;
			String path = "";
			while(!(in = br.readLine()).isEmpty()) {

				if (in.contains("GET")) {

					path = in.split(" ", 3)[1];

				}

			}

			File file = new File("www" + path);
			OutputStream os = socket.getOutputStream();
			PrintStream out = new PrintStream(os, false);


			if (file.exists()) {

				BufferedReader fileReader = new BufferedReader(new FileReader(file));
				String content = "";
				while ((in = fileReader.readLine()) != null) {

					content += in;

				}


				String header = "HTTP/1.1 200 OK\r\nContent-type: text/html\r\nContent-length: " + content.length() + "\r\n\r\n";

				out.println(header + content);


				out.flush();

			} else {
				
				file = new File("www/notfound.html");
				BufferedReader fileReader = new BufferedReader(new FileReader(file));
				String content = "";
				while ((in = fileReader.readLine()) != null) {

					content += in;

				}
				String header = "HTTP/1.1 404 Not Found\r\nContent-type: text/html\r\nContent-length: " + content.length() + "\r\n\r\n";
				out.println(header + content);
				out.flush();

			}

			socket.close();

		}
			
	}

}