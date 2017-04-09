
import java.io.*;
import java.net.Socket;

public final class EchoClient {

    public static void main(String[] args) throws Exception {

		
        try (Socket socket = new Socket("localhost", 22222);
				
				PrintStream out = new PrintStream(socket.getOutputStream(), true, "UTF-8");
				BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
				BufferedReader con = new BufferedReader(new InputStreamReader(System.in));
			) {

			
				while(true){
					
					String lines;
					String input;
					while(true){
						System.out.print("Client> ");
						input = con.readLine();
						
						out.println(input);
						if(input.equals("exit")){
							out.close();
							socket.close();
							con.close();
							System.exit(0);
						}

						lines = in.readLine();
						System.out.println("Server> " + lines);

					}

						
				}
			}
    }
}















