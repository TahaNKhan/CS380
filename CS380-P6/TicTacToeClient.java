
import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class TicTacToeClient {

	private static Socket socket;
	private static InputStream is;
	private static OutputStream os;
	private static ObjectInputStream ois;
	private static ObjectOutputStream oos;
	private static BoardMessage.Status status;
	private static byte[][] board;

	public static void main(String args[]) throws Exception {

		socket = new Socket("codebank.xyz", 38006);

		is = socket.getInputStream();
		os = socket.getOutputStream();
		ois = new ObjectInputStream(is);
		oos = new ObjectOutputStream(os);

		Scanner k = new Scanner(System.in);

		System.out.print("Enter a username: ");
		String username = k.nextLine();
		ConnectMessage cm = new ConnectMessage(username);
		oos.writeObject(cm);
		System.out.println("Okay " + username + ", press any key to start a new game!");
		k.nextLine();
		oos.writeObject(new CommandMessage(CommandMessage.Command.NEW_GAME));
		BoardMessage bm = (BoardMessage) ois.readObject();
		board = bm.getBoard();
		status = bm.getStatus();

		printBoard();
		while (status == BoardMessage.Status.IN_PROGRESS) {
			System.out.println("Your turn " + username + ", where would you like to move? enter X and Y like so: 'X Y'. You are X on the board!");
			String input = k.nextLine();
			byte x = (byte) Integer.parseInt(input.split(" ")[0]);
			byte y = (byte) Integer.parseInt(input.split(" ")[1]);
			MoveMessage ms = new MoveMessage(x, y);
			oos.writeObject(ms);
			bm = (BoardMessage) ois.readObject();
			board = bm.getBoard();
			status = bm.getStatus();
			printBoard();
		}
		if(status == BoardMessage.Status.PLAYER1_VICTORY)System.out.println("You Won!");
		if(status == BoardMessage.Status.PLAYER2_VICTORY)System.out.println("You Lost!");
		if(status == BoardMessage.Status.STALEMATE)System.out.println("You both Lost :|");
		k.close();

	}

	public static void printBoard() {
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if (board[i][j] == 1)
					System.out.print("X ");
				else if (board[i][j] == 2)
					System.out.print("O ");
				else
					System.out.print("_ ");
			}
			System.out.println();
		}
	}

}