import java.io.*;
import java.net.Socket;
import java.util.HashMap;

public class PhysLayerClient {

	public static void main(String[] args) throws Exception {
		Socket socket = new Socket("codebank.xyz", 38002);
		PrintStream out = new PrintStream(socket.getOutputStream(), true, "UTF-8");
		BufferedReader is = new BufferedReader(new InputStreamReader(socket.getInputStream()));

		HashMap<Byte, Byte> fourBFiveB = new HashMap<Byte, Byte>();
		fourBFiveB.put((byte) 30, (byte) 0);
		fourBFiveB.put((byte) 9, (byte) 1);
		fourBFiveB.put((byte) 20, (byte) 2);
		fourBFiveB.put((byte) 21, (byte) 3);
		fourBFiveB.put((byte) 10, (byte) 4);
		fourBFiveB.put((byte) 11, (byte) 5);
		fourBFiveB.put((byte) 14, (byte) 6);
		fourBFiveB.put((byte) 15, (byte) 7);
		fourBFiveB.put((byte) 18, (byte) 8);
		fourBFiveB.put((byte) 19, (byte) 9);
		fourBFiveB.put((byte) 22, (byte) 10);
		fourBFiveB.put((byte) 23, (byte) 11);
		fourBFiveB.put((byte) 26, (byte) 12);
		fourBFiveB.put((byte) 27, (byte) 13);
		fourBFiveB.put((byte) 28, (byte) 14);
		fourBFiveB.put((byte) 29, (byte) 15);
		System.out.println("Connected to Server.");

		double baseline = 0;
		for (int i = 0; i < 64; i++) {
			baseline += (double) is.read();
		}

		baseline /= (double) 64;
		System.out.println("Baseline established from preamble: " + baseline);

		byte[] signals = new byte[64];
		byte lastSignal = 0;

		for (int i = 0; i < signals.length; i++) {

			byte sig = 0;
			byte signal = 0;

			for (int j = 0; j < 5; j++) {
				int currentSignal = is.read();

				if (i == 0 && j == 0) {

					if (currentSignal >= baseline) {
						lastSignal = 1;
						sig = lastSignal;

					} else {
						lastSignal = 0;
						sig = lastSignal;
					}

				} else {

					if (currentSignal >= baseline) {

						if (lastSignal == 0) {
							sig = 1;

						} else {
							sig = 0;
						}
						lastSignal = 1;

					} else {

						if (lastSignal == 1) {
							sig = 1;

						} else {
							sig = 0;
						}
						lastSignal = 0;
					}
				}

				if (j != 0) {
					signal = (byte) (signal << 1);
				}
				signal = (byte) (signal | sig);
			}

			signals[i] = signal;
		}

		byte[] toReturn = new byte[32];

		byte five;
		byte four = 0;
		byte b = 0;
		int j = 0;
		for (int i = 0; i < toReturn.length; i++) {
			b = 0;
			for (int k = 0; k < 2; k++, j++) {
				five = signals[j];

				if (fourBFiveB.containsKey(five)) {
					four = fourBFiveB.get(five);

					if (k == 0) {
						b = four;
					}
				} else {
					four = 0;
				}
			}
			b = (byte) (b << 4);
			b = (byte) (b | four);
			toReturn[i] = b;
		}

		String hexString = "";
		for (int i = 0; i < toReturn.length; i++) {
			hexString += Integer.toString((toReturn[i] & 0xff) + 0x100, 16).substring(1);
		}
		System.out.println("Received 32 bytes: " + hexString.toUpperCase());
		out.write(toReturn);
		int response = is.read();
		if (response == 1) {
			System.out.println("Response good.");
		} else {
			System.out.println("Response bad.");
		}

		System.out.println("Disconnected from server.");
	}
}
