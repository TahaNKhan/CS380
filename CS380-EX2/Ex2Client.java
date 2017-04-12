
import java.io.*;
import java.net.Socket;
import java.util.zip.CRC32;

public final class Ex2Client {

	public static void main(String[] args) throws Exception {

		Socket socket = new Socket("www.codebank.xyz", 38102);
		PrintStream out = new PrintStream(socket.getOutputStream(), true, "UTF-8");
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		CRC32 crc = new CRC32();
		int received = 0;
		int nibbles[] = new int[200];

		for(int i = 0; i < 200;i++) {
			received = in.read();
			nibbles[i] = received;
			System.out.print(Integer.toHexString(received));
			if(i%2 != 0){
				int byte1 = (nibbles[i-1]*0x10)+nibbles[i];

				crc.update(byte1);
			}
		}
		
		System.out.println();
		

		System.out.println("CRC Value that'll be sent: " + Long.toHexString(crc.getValue()));
		
		String crc1 = Long.toHexString(crc.getValue());
		String[] bytes4 = {crc1.substring(0, 2),crc1.substring(2, 4),crc1.substring(4, 6),crc1.substring(6, 8)};
		

		
		for(int i = 0; i < 4; i++){
			out.println(bytes4[i]);
			System.out.println("Sent: " + bytes4[i]);
		}
		int outputFromServer = in.read();
		System.out.println(outputFromServer);
		in.close();
		out.close();
		socket.close();
		System.exit(0);

	}


}
