import java.awt.*;
import java.util.Scanner;
import java.io.PrintWriter;
import java.net.Socket;
import javax.swing.*;


public class GoGameClient {

    private Socket socket;
    private Scanner in;
    private static PrintWriter out;
    private int GameSize;
    private GUI gui ;

    private GoGameClient(String serverAddress, int boardSize) throws Exception {

        socket = new Socket(serverAddress, 58901);
        in = new Scanner(socket.getInputStream());
        out = new PrintWriter(socket.getOutputStream(), true);
        GameSize = boardSize;
        gui = new GUI(GameSize);

    }

    static void sendMessage(String message){
        out.println(message);
    }

    private void play() throws Exception {
        try {
            var response = in.nextLine();
            var pawn = response.charAt(8);
            var opponentPawn = pawn == 'B' ? 'W' : 'B';

            if(pawn == 'B') out.println("SIZE " + GameSize);

            while (in.hasNextLine()) {
                response = in.nextLine();
                if (response.startsWith("VALID_MOVE")) {

                    gui.currentCross.setPawn(pawn);
                    gui.currentCross.repaint();
                    gui.messageLabel.setText("Enemy turn");
                    gui.messageLabel.setBackground(Color.lightGray);

                }

                else if (response.startsWith("OPPONENT_MOVED")) {

                    String[] cords = response.substring(15).split(";");

                    gui.board[Integer.parseInt(cords[0])][Integer.parseInt(cords[1])].setPawn(opponentPawn);
                    gui.board[Integer.parseInt(cords[0])][Integer.parseInt(cords[1])].repaint();
                    gui.messageLabel.setText("Opponent moved, your turn");
                    gui.messageLabel.setBackground(Color.GREEN);

                }

                else if (response.startsWith("MOVE")) {

                    String[] cords = response.substring(5).split(";");

                    gui.board[Integer.parseInt(cords[0])][Integer.parseInt(cords[1])].setPawn('C');
                    gui.board[Integer.parseInt(cords[0])][Integer.parseInt(cords[1])].repaint();

                }

                else if (response.startsWith("PASSED")) {

                    gui.messageLabel.setText("You passed, enemy turn");
                    gui.messageLabel.setBackground(Color.lightGray);

                }

                else if (response.startsWith("MESSAGE")) {

                    response = response.substring(8);

                    if(response.equals("Your turn") || response.equals("Opponent passed, your turn"))
                        gui.messageLabel.setBackground(Color.GREEN);
                    else
                        gui.messageLabel.setBackground(Color.RED);

                    gui.messageLabel.setText(response);


                }

                else if (response.startsWith("OTHER_PLAYER_LEFT")) {
                    JOptionPane.showMessageDialog(gui.frame, "Other player left", "GAME ENDS" , JOptionPane.INFORMATION_MESSAGE);
                    break;
                }

                else if (response.startsWith("END")) {
                    JOptionPane.showMessageDialog(gui.frame, response.substring(4), "GAME ENDS" , JOptionPane.INFORMATION_MESSAGE);
                    break;
                }
            }
            out.println("QUIT");
        } catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            socket.close();
            gui.frame.dispose();
        }
    }

    public static void main(String[] args) {

        if (args.length != 2) {
            System.err.println("Pass the server IP and board size");
            return;
        }

        try{
            GoGameClient client = new GoGameClient(args[0],Integer.parseInt(args[1]));
            client.play();
        }
        catch (Exception e){
            System.err.println("SERVER NOT RESPONDING");
        }

    }
}