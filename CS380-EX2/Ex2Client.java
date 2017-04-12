
import java.io.*;
import java.net.Socket;
import java.util.zip.CRC32;

public final class Ex2Client {

	public static void main(String[] args) throws Exception {

		Socket socket = new Socket("www.codebank.xyz", 38102);
		System.out.println("Connected to Server.");
		PrintStream out = new PrintStream(socket.getOutputStream(), true, "UTF-8");
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		CRC32 crc = new CRC32();
		int received = 0;
		int nibbles[] = new int[200];
		System.out.println("Received Bytes:");
		System.out.print("\t");
		for (int i = 0; i < 200; i++) {
			received = in.read();
			nibbles[i] = received;
			System.out.print(Integer.toHexString(received).toUpperCase());
			if (i > 0 && i < 199 && (i + 1) % 20 == 0) {
				System.out.print("\n\t");
			} else if (i == 199) {
				System.out.println();
			}
			if (i % 2 != 0) {
				int byte1 = (nibbles[i - 1] * 0x10) + nibbles[i];

				crc.update(byte1);
			}
		}

		System.out.println("Gererated CRC32: " + Long.toHexString(crc.getValue()).toUpperCase());

		long crcVal = crc.getValue();
		byte[] toSend = new byte[4];
		toSend[0] = (byte) ((byte) crcVal >> 24);
		toSend[1] = (byte) ((byte) crcVal >> 16);
		toSend[2] = (byte) ((byte) crcVal >> 8);
		toSend[3] = (byte) crcVal;

		String crc1 = Long.toHexString(crc.getValue());

		for (int i = 0; i < 4; i++) {
			out.println(toSend[i]);
			System.out.println("Sent: " + toSend[i]);
		}
		byte outputFromServer = (byte) in.read();
		if (outputFromServer == 1) {
			System.out.println("Response Good.");
		} else {
			System.out.println("Response Bad.");
		}
		in.close();
		out.close();
		socket.close();
		System.out.println("Disconnected from server.");
		System.exit(0);

	}

}
