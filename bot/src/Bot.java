import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

class Bot {

    private Scanner in;
    private static PrintWriter out;
    private int GameSize;
    private int[][] board;

    Bot(String serverAddress, int boardSize) throws Exception {

        Socket socket = new Socket(serverAddress, 58901);
        in = new Scanner(socket.getInputStream());
        out = new PrintWriter(socket.getOutputStream(), true);
        GameSize = boardSize;

        setupBoard();
        play();

    }

    private void setupBoard() {

        board = new int[GameSize][GameSize];

        for(int i = 0 ; i<GameSize ; i++){
            for(int j = 0 ; j<GameSize ; j++){

                board[i][j] = 0;

            }
        }

    }

    private void play() {

        var response = in.nextLine();

        while (in.hasNextLine()) {

            response = in.nextLine();

            System.out.println(response);


            if (response.startsWith("OPPONENT_MOVED")){

                String[] cords = response.substring(15).split(";");

                board[Integer.parseInt(cords[0])][Integer.parseInt(cords[1])] = 1;

                botMove();

            }

            else if (response.equals("MESSAGE Opponent passed, your turn")) {

                botMove();

            }

            else if (response.startsWith("MOVE")) {

                String[] cords = response.substring(5).split(";");

                board[Integer.parseInt(cords[0])][Integer.parseInt(cords[1])] = 0;

            }

            else if (response.startsWith("OTHER_PLAYER_LEFT")) {

                break;
            }

            else if (response.startsWith("END")) {
                break;
            }
        }

    }

    private void botMove() {

        int failure = 0 ;
        String response;

        outerLoop:
        while(true) {
            for (int i = 0; i < GameSize; i++) {
                for (int j = 0; j < GameSize; j++) {

                    if (board[i][j] == 0) {

                        out.println("MOVE " + i + ";" + j);
                        response = in.nextLine();
                        if (response.equals("VALID_MOVE")) {
                            board[i][j] = 2;
                            break outerLoop;
                        }
                        if (response.startsWith("MOVE")) {

                            String[] cords = response.substring(5).split(";");
                            board[Integer.parseInt(cords[0])][Integer.parseInt(cords[1])] = 0;

                            board[i][j] = 2;
                            break outerLoop;
                        }

                        failure++;

                        if (failure > 50){

                            out.println("PASS");
                            break outerLoop;

                        }


                    }
                }
            }
        }
    }


    public static void main(String[] args) {

        try{
            Bot bot = new Bot(args[0], Integer.parseInt(args[1]));
        }
        catch (Exception e){
            System.err.println("SERVER NOT RESPONDING");
        }


    }

}