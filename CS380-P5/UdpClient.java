import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.text.DecimalFormat;
import java.util.Random;

public class UdpClient {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub
		Socket socket = new Socket("www.codebank.xyz", 38005);
		OutputStream out = socket.getOutputStream();
		InputStream in = socket.getInputStream();
		double avgRTT = 0;

		// handshake
		byte[] handshakeData = { (byte) 0xDE, (byte) 0xAD, (byte) 0xBE, (byte) 0xEF };
		byte hspacket[] = createPacket(handshakeData);
		out.write(hspacket);
		int resp = 0;
		for (int i = 0; i < 4; i++) {
			resp ^= in.read();
			if (i != 3) {
				resp = resp << 8;
			}
		}
		System.out.println("Handshake response: 0x" + Integer.toHexString(resp).toUpperCase());
		int port = 0;
		for (int i = 0; i < 2; i++) {
			port ^= in.read();
			if (i != 1) {
				port = port << 8;
			}

		}
		System.out.println("Port number received: " + port);


		int data = 2;
		
		// rest of the stuff
		
		for (int i = 0; i < 12; i++) {
			byte[] udpdata = createUDP(port, data);
			byte[] packet = createPacket(udpdata);
			out.write(packet);
			double sendTime = System.currentTimeMillis();
			double receiveTime = 0, RTT = 0;
			resp = 0;
			for (int j = 0; j < 4; j++) {
				resp ^= in.read();
				if(j == 0)receiveTime = System.currentTimeMillis();
				if (j != 3) {
					resp = resp << 8;
				}
			}
			RTT= receiveTime-sendTime;
			avgRTT +=RTT;
			System.out.println("Sending packet with " + data + " bytes of data\nResponse: 0x" + Integer.toHexString(resp).toUpperCase() + "\nRTT: " + (int)RTT +  "ms\n");
			data *= 2;

		}
		avgRTT /= 12;
		DecimalFormat df = new DecimalFormat("#.00");
		System.out.println("Average RTT: " + df.format(avgRTT) + "ms");
		// close
		in.close();
		out.close();
		socket.close();
	}

	private static byte[] createPacket(byte[] data) {
		byte[] packet = new byte[20 + data.length];

		// version ihl
		packet[0] = 69;

		// tos
		packet[1] = 0;

		// length
		short length = (short) (20 + data.length);
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
		packet[9] = 17;

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

		for (int i = 0; i < data.length; i++) {
			packet[20 + i] = (byte) data[i];
		}

		return packet;

	}

	private static byte[] createUDP(int destPort, int size) {
		// source port number
		byte[] packet = new byte[8+ size];
		packet[0] = 0;
		packet[1] = 0;
		// destination port
		packet[2] = (byte) (destPort >> 8);
		packet[3] = (byte) destPort;
		// 16 bit udp length
		packet[4] = (byte) (size >> 8);
		packet[5] = (byte) size;

		// data
		Random rand = new Random();
		for (int i = 0; i < size; i++) {
			packet[i + 8] = (byte) rand.nextInt();
		}

		// udp checksum
		//// udp header + data + pseudo header
		//// pseudo header
		byte[] pseudoHeader = new byte[12];
		pseudoHeader[0] = (byte) 92;
		pseudoHeader[1] = (byte) 93;
		pseudoHeader[2] = (byte) 123;
		pseudoHeader[3] = (byte) 232;

		// dest addr
		pseudoHeader[4] = (byte) 52;
		pseudoHeader[5] = (byte) 37;
		pseudoHeader[6] = (byte) 88;
		pseudoHeader[7] = (byte) 154;

		// reserved
		pseudoHeader[8] = 0;

		// protocol
		pseudoHeader[9] = 17;
		pseudoHeader[10] = packet[4];
		pseudoHeader[11] = packet[5];

		// construct checksum byte array:
		byte[] toCheck = new byte[8 + size + 12];
		for (int i = 0, j = 0; i < toCheck.length; i++) {
			if (i < (8 + size)) {
				toCheck[i] = packet[i];
			} else {
				toCheck[i] = pseudoHeader[j];
				j++;
			}
		}

		short checksum = checksum(toCheck);
		packet[6] = (byte) ((checksum >> 8) & 0xff);
		packet[7] = (byte) (checksum & 0xff);

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
