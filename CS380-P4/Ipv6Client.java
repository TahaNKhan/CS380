import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;

public class Ipv6Client {


	public static void main(String[] args) throws Exception {

		Socket socket = new Socket("www.codebank.xyz", 38004);
		OutputStream out = socket.getOutputStream();
		InputStream in = socket.getInputStream();

		int data = 2;

		for (int i = 0; i < 12; i++) {
			byte[] packet = createPacket(data);
			out.write(packet);

			System.out.println("data length: " + data);
			System.out.print("Response: 0x");
			byte[] response = new byte[4];
			for (int j = 0; j < 4; j++) {
				response[j] = (byte) in.read();
				System.out.print(Integer.toHexString(Byte.toUnsignedInt(response[j])).toUpperCase());
			}
			System.out.println("\n");
			data *= 2;
			if (i == 11) {
				out.close();
				in.close();
				socket.close();
			}
			

		}

	}

	private static byte[] createPacket(int data) {
		// TODO Auto-generated method stub
		byte[] packet = new byte[40 + data];

		// version
		packet[0] = (byte) 96;

		// traffic class + flow label
		packet[1] = (byte) 0;
		packet[2] = (byte) 0;
		packet[3] = (byte) 0;

		// payload
		packet[4] = (byte) (data >>> 8);
		packet[5] = (byte) data;

		// next header: udp
		packet[6] = (byte) 17;

		// hop limit

		packet[7] = (byte) 20;

		// source addr:

		packet[8] = (byte) 0;
		packet[9] = (byte) 0;
		packet[10] = (byte) 0;
		packet[11] = (byte) 0;
		packet[12] = (byte) 0;
		packet[13] = (byte) 0;
		packet[14] = (byte) 0;
		packet[15] = (byte) 0;
		packet[16] = (byte) 0;
		packet[17] = (byte) 0;
		packet[18] = (byte) 0xff;
		packet[19] = (byte) 0xff;
		packet[20] = (byte) 0xff;
		packet[21] = (byte) 0xff;
		packet[22] = (byte) 0xff;
		packet[23] = (byte) 0xff;

		// dest addr:

		packet[24] = (byte) 0;
		packet[25] = (byte) 0;
		packet[26] = (byte) 0;
		packet[27] = (byte) 0;
		packet[28] = (byte) 0;
		packet[29] = (byte) 0;
		packet[30] = (byte) 0;
		packet[31] = (byte) 0;
		packet[32] = (byte) 0;
		packet[33] = (byte) 0;
		packet[34] = (byte) 0xff;
		packet[35] = (byte) 0xff;
		packet[36] = (byte) 52;
		packet[37] = (byte) 37;
		packet[38] = (byte) 88;
		packet[39] = (byte) 154;

		// data

		for (int i = 40; i < packet.length; i++) {
			packet[i] = (byte) 0;
		}

		return packet;
	}

}