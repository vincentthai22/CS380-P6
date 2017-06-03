import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.util.Scanner;

/**
 * Created by Vincent on 5/29/2017.
 */
public class TicTacToeClient {
    private final static String SERVER_NAME = "codebank.xyz";
    private final static int PORT_NUMBER = 38006;
    private final String COMPUTER = "TIC-TAC-TOE-MACHINE 0x10101010";
    String serverName;
    int portNumber;
    byte[][] board;

    public TicTacToeClient(String serverName, int portNumber) {
        this.serverName = serverName;
        this.portNumber = portNumber;

        callServer();

    }

    private void callServer() {

        String username;
        int userInput = 0;
        BoardMessage.Status boardStatus;

        boolean isDone = false;


        try (Socket socket = new Socket(serverName, portNumber)) {

            //System.out.println("Connected to server");

            InputStream is = socket.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, "UTF-8");
            OutputStream os = socket.getOutputStream();
            ObjectInputStream objectInputStream = new ObjectInputStream(is);
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(os);
            BoardMessage boardMessage;
            MoveMessage moveMessage;

            Scanner kbd = new Scanner(System.in);
            System.out.println("\tHello! Welcome to Tic-Tac-Toe On-A-Server-Edition!\n\tPlease Enter a username: ");
            username = kbd.nextLine();

            while (userInput != -1) {

                System.out.println("\tMain Menu:\n\tEnter '1' to begin playing.\n\tEnter '-1' to Quit.");
                userInput = kbd.nextInt();

                switch (userInput) {

                    case 1:
                        ConnectMessage cm = new ConnectMessage(username);
                        objectOutputStream.writeObject(cm);
                        objectOutputStream.writeObject(new CommandMessage(CommandMessage.Command.NEW_GAME));
                        boardMessage = (BoardMessage) objectInputStream.readObject();
                        board = boardMessage.getBoard();
                        boardStatus = boardMessage.getStatus();
                        System.out.println("\tEnter the number respective to the interface to make your move.");
                        while (boardStatus == BoardMessage.Status.IN_PROGRESS) {

                            displayBoard(board.length);
                            System.out.println("Your move:");
                            userInput = kbd.nextInt();

                            moveMessage = new MoveMessage((byte) (userInput / board.length), (byte) (userInput % board.length));
                            objectOutputStream.writeObject(moveMessage);
                            boardMessage = (BoardMessage) objectInputStream.readObject();
                            board = boardMessage.getBoard();
                            boardStatus = boardMessage.getStatus();

                        }
                        displayBoard(board.length);
                        if (boardStatus == BoardMessage.Status.PLAYER1_VICTORY)
                            System.out.println("The winner is... " + username + "!");

                        if (boardStatus == BoardMessage.Status.PLAYER2_VICTORY)
                            System.out.println("The winner is... " + COMPUTER + "!");

                        if (boardStatus == BoardMessage.Status.STALEMATE)
                            System.out.println("The winner is... NOBODY!");
                        isDone = true;
                        break;
                    case 2:
                        socket.close();
                        break;
                }
            }

        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (ClassCastException e) {
            if (isDone)
                System.out.println("Please enter 25c into your AUX");
            else
                System.out.println("Did you just enter the same position twice? Auto-lose. CHEATER");
        }
    }

    private void displayBoard(int size) {
        String singleLine;
        for (int i = 0; i < size; i++) {
            singleLine = "  ";
            for (int j = 0; j < size; j++) {
                switch (board[i][j]) {
                    case 1:
                        singleLine += 'X';
                        break;
                    case 2:
                        singleLine += 'O';
                        break;
                    default:
                        singleLine += String.valueOf(j + (i * size));
                        break;
                }
                if (j < size - 1)
                    singleLine += " | ";
            }
            System.out.println(singleLine + (i < size - 1 ? "\n-------------" : ""));
        }
    }

    public static void main(String[] args) {
        new TicTacToeClient(SERVER_NAME, PORT_NUMBER);
    }
}
