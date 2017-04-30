
import java.io.InputStream;

import java.io.PrintStream;
import java.net.Socket;

public class Ex3Client {
	public static void main(String args[]) throws Exception {
		Socket socket = new Socket("www.codebank.xyz", 38103);
		InputStream is = socket.getInputStream();
		PrintStream out = new PrintStream(socket.getOutputStream());
		System.out.println("Connected to server.");

		int len = is.read();
		byte[] bytes = new byte[len];
		System.out.println("Reading " + bytes.length + " bytes");
		System.out.println("Data Received:");

		for (int i = 0; i < bytes.length; i++) {
			bytes[i] = (byte) (is.read());
			if((int)bytes[i] < 10 && bytes[i] >= 0){
				System.out.print("0");
			}
			System.out.print(Integer.toHexString(Byte.toUnsignedInt(bytes[i])).toUpperCase());
			if((i+1) % 10 == 0 && i != 0)System.out.println();
		}

		System.out.println();

		short checksm = checksum(bytes);
		
		byte[] toSend = new byte[2];
		
		toSend[0] = (byte) ((checksm >> 8) & 0xff);
		toSend[1] = (byte) (checksm & 0xff);
		
		out.write(toSend);
		
		int response = is.read();
		if (response == 1) {
			System.out.println("Response Good.");
		} else {
			System.out.println("Response Bad.");
		}
		is.close();
		out.close();
		socket.close();

	}

	public static short checksum(byte[] b) {
		int sum = 0;
		int[] bytes = new int[2];
		int toAdd;
		for (int i = 0; i < b.length; i++) {
			bytes[0] = 0;
			bytes[1] = 0;
			toAdd = 0;
			bytes[0] = (b[i] << 24) >>> 16;
			i++;
			if(i < b.length) {
				bytes[1] =(int) ((b[i] << 24)>>>24);
			}
			toAdd = (bytes[0] | bytes[1]);
			sum += toAdd;
			if ((sum & 0xFFFF0000) != 0) {
				sum &= 0x0000FFFF;
				sum = sum + 1;
			}
		}
		return (short) ~(sum & 0xFFFF);
	}
}