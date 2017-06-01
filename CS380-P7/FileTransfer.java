import java.security.*;
import javax.crypto.*;
import java.io.*;
import java.util.Scanner;
import java.util.zip.CRC32;
import java.net.ServerSocket;
import java.net.Socket;


public class FileTransfer {

	public static void main(String[] args) throws Exception {

		Cipher cipher = null;
		Scanner kb = new Scanner(System.in);


		if (args[0].compareTo("makekeys") == 0) {

			try {
				KeyPairGenerator gen = KeyPairGenerator.getInstance("RSA");
				gen.initialize(4096); // you can use 2048 for faster key
										// generation
				KeyPair keyPair = gen.genKeyPair();
				PrivateKey privateKey = keyPair.getPrivate();

				PublicKey publicKey = keyPair.getPublic();
				try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("public.bin")))) {
					oos.writeObject(publicKey);
				}
				try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(new File("private.bin")))) {
					oos.writeObject(privateKey);
				}
			} catch (NoSuchAlgorithmException | IOException e) {
				e.printStackTrace(System.err);
			}

		}


		if (args[0].compareTo("server") == 0) {

			if (args.length < 3) {

				System.out.println("Not enough arguments");
				System.exit(0);

			} else {

				try {


					int port = Integer.parseInt(args[2]);


					ServerSocket serverSocket = new ServerSocket(port);

					System.out.println("Waiting for user to connect...");


					while (true) {


						Socket socket = serverSocket.accept();

						System.out.println("Client Connected!");

						InputStream is = socket.getInputStream();
						ObjectInputStream ois = new ObjectInputStream(is);

						OutputStream os = socket.getOutputStream();
						ObjectOutputStream oos = new ObjectOutputStream(os);

						Message message = (Message) ois.readObject();

						MessageType type = message.getType();


						if (type == MessageType.DISCONNECT) {

							socket.close();

						} else if (type == MessageType.START) {

							PrivateKey key = null;

							System.out.println("File Transfer Started!");


							try (ObjectInputStream ks = new ObjectInputStream(new FileInputStream(new File(args[1])))) {

								key = (PrivateKey) ks.readObject();

							} catch (IOException e) {

								e.printStackTrace(System.err);
								AckMessage ack = new AckMessage(-1);
								oos.writeObject(ack);
								System.exit(0);

							}

							AckMessage ack = new AckMessage(0);
							oos.writeObject(ack);

							StartMessage sm = (StartMessage) message;


							cipher = Cipher.getInstance("RSA");
							cipher.init(Cipher.UNWRAP_MODE, key);
							Key session = cipher.unwrap(sm.getEncryptedKey(), "AES", Cipher.SECRET_KEY);

							long size = sm.getSize();
							int chunkSize = sm.getChunkSize();
							long numOfChunks = size / chunkSize;
							if (size % chunkSize > 0) {

								numOfChunks++;

							}
							byte[] data = new byte[(int) size];
							int index = 0;

							int exp = 0;

							while (exp < numOfChunks) {

								message = (Message) ois.readObject();
								type = message.getType();


								if (type == MessageType.STOP) {


									ack = new AckMessage(-1);
									oos.writeObject(ack);

									break;

								} else if (type == MessageType.CHUNK) {

									Chunk chunk = (Chunk) message;

									int seq = chunk.getSeq();


									if (seq == exp) {

										byte[] current = chunk.getData();


										cipher = Cipher.getInstance("AES");
										cipher.init(Cipher.DECRYPT_MODE, session);

										byte[] decrypted = cipher.doFinal(current);


										CRC32 hash = new CRC32();
										hash.update(decrypted);
										int hashValue = (int) hash.getValue();

										if (hashValue == chunk.getCrc()) {

											exp++;

											for (int i = 0; i < decrypted.length; i++) {

												if (index < data.length) {

													data[index] = decrypted[i];

												}

												index++;

											}


											ack = new AckMessage(exp);

											System.out.println("Chunk received [" + exp + "/" + numOfChunks + "].");

										} else {

											ack = new AckMessage(seq);

										}


										oos.writeObject(ack);

									}

								}

								if (exp == numOfChunks) {


									String output = sm.getFile();
									output = output.replaceAll(".txt", "_copy.txt");


									FileOutputStream writer = new FileOutputStream(new File(output));
									writer.write(data);
									writer.close();


									System.out.println("Transfer complete");
									System.out.println("Output path: " + output + "\n");


									break;

								}

							}


							socket.close();

						}

					}

				} catch (Exception e) {

					e.printStackTrace(System.err);

				}

			}


		} else if (args[0].compareTo("client") == 0) {

			boolean transfer = true;

			while (transfer) {


				System.out.println("Generating key...\n");
				KeyGenerator keyGen = KeyGenerator.getInstance("AES");
				keyGen.init(128);
				Key sessionKey = keyGen.generateKey();


				if (args.length == 4) {


					Socket socket = new Socket(args[2], Integer.parseInt(args[3]));


					OutputStream os = socket.getOutputStream();
					ObjectOutputStream oos = new ObjectOutputStream(os);
					oos.flush();

					InputStream is = socket.getInputStream();
					ObjectInputStream ois = new ObjectInputStream(is);

					PublicKey key = null;

					try (ObjectInputStream ks = new ObjectInputStream(new FileInputStream(new File(args[1])))) {

						key = (PublicKey) ks.readObject();

					} catch (IOException e) {

						e.printStackTrace(System.err);
						System.exit(0);

					}


					cipher = Cipher.getInstance("RSA");
					cipher.init(Cipher.WRAP_MODE, key);
					byte[] session = cipher.wrap(sessionKey);


					boolean valid = false;
					File file = null;
					String path;

					do {


						System.out.print("Enter path: ");
						path = kb.nextLine();
						file = new File(path);

						if (!file.exists()) {

							System.out.println("Invalid file; please try again.");

						} else {

							valid = true;

						}

					} while (!valid);

					valid = false;
					int chunkSize = 0;

					while (!valid) {


						System.out.print("Enter chunk size (default = 1024) : ");
						String in = kb.nextLine();


						if (in.compareTo("") == 0) {

							chunkSize = 1024;
							valid = true;

						} else {

							try {


								chunkSize = Integer.parseInt(in);
								valid = true;

							} catch (Exception e) {


								System.out.println("Invalid input; try again");

							}

						}

					}


					StartMessage sm = new StartMessage(path, session, chunkSize);
					oos.writeObject(sm);


					long length = file.length();
					long numOfChunks = length / chunkSize;
					if (length % chunkSize > 0) {

						numOfChunks++;

					}
					int seq = 0;


					InputStream fileReader = new FileInputStream(path);

					System.out.println("Sending: " + path + ". File size (byte): " + length);
					System.out.println("Sending " + numOfChunks);

					while (seq < numOfChunks) {


						AckMessage ack = (AckMessage) ois.readObject();



						if (ack.getSeq() == -1) {

							System.out.println("Transfer stopped");


						} else {

							if (ack.getSeq() == seq) {


								byte[] data = new byte[chunkSize];
								for (int i = 0; i < chunkSize; i++) {

									data[i] = (byte) fileReader.read();

								}

								CRC32 hash = new CRC32();
								hash.update(data);
								int hashValue = (int) hash.getValue();

							
								cipher = Cipher.getInstance("AES");
								cipher.init(Cipher.ENCRYPT_MODE, sessionKey);
								data = cipher.doFinal(data);

							
								Chunk chunk = new Chunk(seq, data, hashValue);

								oos.writeObject(chunk);

								seq++;

								System.out.println("Chunks completed [" + seq + "/" + numOfChunks + "].");

							} else {

								System.out.println("invalid acknowledgement");
								System.exit(0);

							}

						}

					}

					System.out.println("Finished transfer.\n");

					valid = false;


					do {

						try {
							System.out.print("Would you like to transfer another file? (y or n) ");
							char cont = kb.nextLine().charAt(0);

							if (cont == 'n' || cont == 'N') {

								transfer = false;
								valid = true;

							} else if (cont != 'y' && cont != 'Y') {

								System.out.println("Invalid input; please try again");

							}
						} catch (Exception e) {

							System.out.println("Invalid input; try again");

						}

					} while (!valid);

				}

			}

		} else {

			System.out.println("Not enough arguments");
			System.exit(0);

		}

		kb.close();

	}

}