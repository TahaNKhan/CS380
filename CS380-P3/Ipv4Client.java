
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Random;


public class Ipv4Client {

	@SuppressWarnings("resource")
	public static void main(String[] args) throws Exception {
		
		Socket socket = new Socket("www.codebank.xyz", 38003);
		OutputStream out = socket.getOutputStream();
		BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
		while (true) {

			int data = 2;

			for (int i = 0; i < 12; i++) {
				byte[] packet = createPacket(data);
				out.write(packet);
				String response = in.readLine();
				System.out.println("data length: " + data + "\n" + response);
				if (i == 11) {
					out.close();
					in.close();
					socket.close();
					break;
				}
				if (response.compareTo("bad") == 0) {
					socket.close();
					socket = new Socket("www.codeback.xyz", 38003);
					out = socket.getOutputStream();
					in = new BufferedReader(new InputStreamReader(socket.getInputStream(), "UTF-8"));
					data = 2;
				}
				data *= 2;

			}
			
			break;

		}
		
	}

	private static byte[] createPacket(int data) {
		// TODO Auto-generated method stub
		byte[] packet = new byte[20 + data];

		// version ihl
		packet[0] = 69;

		// tos
		packet[1] = 0;

		// length
		short length = (short) (20 + data);
		packet[2] = (byte) ((length >> 8) & 0xff);
		packet[3] = (byte) (length & 0xff);

		// id
		packet[4] = 0;
		packet[5] = 0;

		// flags

		packet[6] = 1;
		packet[6] = (byte) (packet[6] << 6);

		// offset
		packet[7] = 0;

		// ttl
		packet[8] = 50;

		// protocol
		packet[9] = 6;

		// source addr
		packet[12] = (byte) 92;
		packet[13] = (byte) 93;
		packet[14] = (byte) 123;
		packet[15] = (byte) 232;

		// dest addr
		packet[16] = (byte) 52;
		packet[17] = (byte) 37;
		packet[18] = (byte) 88;
		packet[19] = (byte) 154;

		// checksum
		short packetChecksum = checksum(packet);
		packet[10] = (byte) ((packetChecksum >> 8) & 0xff);
		packet[11] = (byte) (packetChecksum & 0xff);

		// data
		Random random = new Random();
		for (int i = 20; i < packet.length; i++) {
			packet[i] = (byte)random.nextInt();
		}

		return packet;
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
			if (i < b.length) {
				bytes[1] = (int) ((b[i] << 24) >>> 24);
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