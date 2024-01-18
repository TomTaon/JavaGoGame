import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

class Game {

    private Player[][] board;
    private Player[][] oldBoard;
    private Player[][] oldOldBoard;
    private int[][] logicBoard;
    private Player currentPlayer;
    private int boardSize;
    private List<String> connections1 = new ArrayList<>();
    private List<String> connections2 = new ArrayList<>();
    private List<String> connections0 = new ArrayList<>();
    private int blackKills = 0;
    private int whiteKills = 0;
    private boolean lastMoveWasPass = false;
    private boolean botExists = false;


    private synchronized void move(int row, int col , Player player) {
        if (player != currentPlayer) {
            throw new IllegalStateException("Not your turn");
        } else if (player.opponent == null) {
            throw new IllegalStateException("You don't have an opponent yet");
        } else if (board[row][col] != null) {
            throw new IllegalStateException("Cross already occupied");
        }  else if (suicideMoveCheck(row, col)) {
            throw new IllegalStateException("Dont kill yourself");
        }  else if (koMoveCheck(row, col)) {
            throw new IllegalStateException("KO move");
        }

        board[row][col] = currentPlayer;
        killEnemies(false);
        killEnemies(true);
        createOldBoards();
        lastMoveWasPass = false;
        currentPlayer = currentPlayer.opponent;


    }

    private void createOldBoards(){
        for(int i=0 ; i<boardSize ; i++){
            for(int j=0 ; j<boardSize ; j++){
                oldOldBoard[i][j] = oldBoard[i][j];
                oldBoard[i][j] = board[i][j];
            }
        }
    }

    private boolean koMoveCheck(int row, int col) {

        board[row][col] = currentPlayer;

        killEnemies(true);

        for (int i = 0;i<boardSize;i++){
            for (int j = 0;j<boardSize;j++){
                if(oldOldBoard[i][j] != board[i][j]){
                    repairBoard();
                    return false;
                }
            }
        }

        repairBoard();
        return true;

    }

    private void repairBoard(){

        for (int i = 0;i<boardSize;i++){
            System.arraycopy(oldBoard[i], 0, board[i], 0, boardSize);
        }
    }


    private String countScore(){

        fillLogicBoard();

        fillConnectionsArraylists();

        int oneFinal = 0;
        int twoFinal = 0;
        int oneCount = 0;
        int twoCount = 0;

        for(String z : connections0){

            String[] cords = z.split(";");

            for (String cord : cords) {

                String[] xy = cord.split(",");

                int x = Integer.parseInt(xy[0]);
                int y = Integer.parseInt(xy[1]);


                if(logicBoard[x+1][y] == 1)
                    oneCount++;
                if(logicBoard[x-1][y] == 1)
                    oneCount++;
                if(logicBoard[x][y+1] == 1)
                    oneCount++;
                if(logicBoard[x][y-1] == 1)
                    oneCount++;
                if(logicBoard[x+1][y] == 2)
                    twoCount++;
                if(logicBoard[x-1][y] == 2)
                    twoCount++;
                if(logicBoard[x][y+1] == 2)
                    twoCount++;
                if(logicBoard[x][y-1] == 2)
                    twoCount++;

                if(oneCount > 0 && twoCount > 0) break;

            }

            if(oneCount == 0 && twoCount>0) twoFinal = twoFinal + cords.length;

            if(twoCount == 0 && oneCount>0) oneFinal = oneFinal + cords.length;

            oneCount = 0;
            twoCount = 0;

        }

        if(currentPlayer.pawn=='B')
            return ("Black scored: "+(oneFinal+blackKills)+ " White scored: "+(twoFinal+whiteKills));
        else
            return ("Black scored: "+(twoFinal+blackKills)+ " White scored: "+(oneFinal+whiteKills));

    }

    private void fillLogicBoard(){

        logicBoard = new int[boardSize+2][boardSize+2];

        for(int a=0;a<boardSize+2;a++){
            for(int b=0;b<boardSize+2;b++){
                if(a==0||a==boardSize+1||b==0||b==boardSize+1) logicBoard[a][b]=-1;
                else if(board[a-1][b-1]==currentPlayer) logicBoard[a][b]=1;
                else if(board[a-1][b-1]==currentPlayer.opponent) logicBoard[a][b]=2;
                else logicBoard[a][b]=0;
            }
        }

    }

    private void fillConnectionsArraylists(){

        connections1.clear();
        connections2.clear();
        connections0.clear();

        for(int a=1;a<boardSize+1;a++){
            for(int b=1;b<boardSize+1;b++){

                if(logicBoard[a][b] == logicBoard[a][b+1])
                    createHorizontalConnections(a,b,a,b+1,logicBoard[a][b]);

                else if(logicBoard[a][b] != logicBoard[a][b-1])
                    createHorizontalConnections(a,b,logicBoard[a][b]);

            }
        }

        for(int a=1;a<boardSize+1;a++){
            for(int b=1;b<boardSize+1;b++){

                if(logicBoard[a][b] == logicBoard[a+1][b]){
                    connectConnectionsVertically(a,b,a+1,b,logicBoard[a][b]);
                }

                if(logicBoard[a][b] == logicBoard[a-1][b]){
                    connectConnectionsVertically(a,b,a-1,b,logicBoard[a][b]);
                }

            }
        }

    }

    private boolean suicideMoveCheck(int row, int col) {

        boolean oneDies;
        boolean twoDies;


        //filling logic board

        fillLogicBoard();

        logicBoard[row+1][col+1] = 1;

        //making connection arraylists

        fillConnectionsArraylists();


        //check which connections will die after last move and handle cases

        oneDies = doesOneDie();
        twoDies = doesTwoDie();


        if(!twoDies)
            return oneDies;
        else
            return false;

    }

    private void drawC(){
        for (String s : connections1) {
            System.out.println(s);
        }
        System.out.println("------------------------------------------------------------------------------------");
        for (String s : connections2) {
            System.out.println(s);
        }
        System.out.println("+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++");
        for (String s : connections0) {
            System.out.println(s);
        }
        System.out.println("@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@@");


    }

    private void connectConnectionsVertically(int a, int b,int c, int d, int type){

        int first = 0;
        int second = 0;

        if(type==1){
            for (int i = 0; i < connections1.size(); i++){
                if (connections1.get(i).contains(a + "," + b)){
                    first = i;
                    break;
                }
            }
            for (int i = 0; i < connections1.size(); i++){
                if (connections1.get(i).contains(c + "," + d)){
                    second = i;
                    break;
                }
            }

            if(first != second){

                connections1.set(first, connections1.get(first) + ";" + connections1.get(second));
                connections1.remove(second);

            }

        }
        if(type==2){
            for (int i = 0; i < connections2.size(); i++){
                if (connections2.get(i).contains(a + "," + b)){
                    first = i;
                    break;
                }
            }
            for (int i = 0; i < connections2.size(); i++){
                if (connections2.get(i).contains(c + "," + d)){
                    second = i;
                    break;
                }
            }

            if(first != second){

                connections2.set(first, connections2.get(first) + ";" + connections2.get(second));
                connections2.remove(second);

            }

        }
        if(type==0){
            for (int i = 0; i < connections0.size(); i++){
                if (connections0.get(i).contains(a + "," + b)){
                    first = i;
                    break;
                }
            }
            for (int i = 0; i < connections0.size(); i++){
                if (connections0.get(i).contains(c + "," + d)){
                    second = i;
                    break;
                }
            }

            if(first != second){

                connections0.set(first, connections0.get(first) + ";" + connections0.get(second));
                connections0.remove(second);

            }

        }
    }

    private void createHorizontalConnections(int a, int b,int c, int d, int type){

        if(type==1){
            for (int i = 0; i < connections1.size(); i++){
                if (connections1.get(i).contains(a + "," + b) && !connections1.get(i).contains(c + "," + d)) {
                    connections1.set(i, connections1.get(i) + ";" + c + "," + d);
                    return;
                }
            }
            connections1.add(a + "," + b + ";" + c + "," + d);
        }

        if(type==2){
            for (int x = 0; x < connections2.size(); x++){
                if (connections2.get(x).contains(a + "," + b) && !connections2.get(x).contains(c + "," + d)) {
                    connections2.set(x, connections2.get(x) + ";" + c + "," + d);
                    return;
                }
            }
            connections2.add(a + "," + b + ";" + c + "," + d);
        }

        if(type==0){
            for (int x = 0; x < connections0.size(); x++){
                if (connections0.get(x).contains(a + "," + b) && !connections0.get(x).contains(c + "," + d)) {
                    connections0.set(x, connections0.get(x) + ";" + c + "," + d);
                    return;
                }
            }
            connections0.add(a + "," + b + ";" + c + "," + d);
        }

    }

    private void createHorizontalConnections(int a, int b, int type){
        if(type==1){
            connections1.add(a+","+b);
        }
        if(type==2){
            connections2.add(a+","+b);
        }
        if(type==0){
            connections0.add(a+","+b);
        }
    }

    private void setBoardSize(int size) {
        boardSize = size;
        board = new Player[size][size];
        oldBoard = new Player[size][size];
        oldOldBoard = new Player[size][size];
    }





    private void killEnemies(boolean soft){

        for (String s : connections2) {

            String[] cords = s.split(";");

            for (int j = 0; j < cords.length; j++) {
                String[] xy = cords[j].split(",");
                if (hasBreaths(Integer.parseInt(xy[0]), Integer.parseInt(xy[1]))) {
                    break;
                }
                if (j == cords.length - 1) {

                    for (String cord : cords) {

                        String[] ab = cord.split(",");

                        int x, y;
                        x = Integer.parseInt(ab[0]) - 1;
                        y = Integer.parseInt(ab[1]) - 1;

                        if (soft)

                            board[x][y] = null;

                        if (!soft){

                            currentPlayer.adjustPlayersBoards(x, y);

                            if(currentPlayer.pawn == 'B')
                                blackKills++;
                            else
                                whiteKills++;

                        }
                    }
                }
            }
        }
    }



    private boolean doesTwoDie(){

        for(String z : connections2){
            String[] cords = z.split(";");
            for(int i=0;i<cords.length;i++){
                String[] xy = cords[i].split(",");
                if(hasBreaths(Integer.parseInt(xy[0]),Integer.parseInt(xy[1]))){
                    break;
                }
                if(i+1==cords.length)
                    return true;
            }
        }
        return false;
    }

    private boolean doesOneDie(){

        for(String z : connections1){
            String[] cords = z.split(";");
            for(int i=0;i<cords.length;i++){
                String[] xy = cords[i].split(",");
                if(hasBreaths(Integer.parseInt(xy[0]),Integer.parseInt(xy[1]))){
                    break;
                }
                if(i+1==cords.length)
                    return true;
            }
        }
        return false;
    }

    private boolean hasBreaths(int a, int b){
        return (logicBoard[a - 1][b] == 0 || logicBoard[a + 1][b] == 0 || logicBoard[a][b - 1] == 0 || logicBoard[a][b + 1] == 0);
    }



    class Player implements Runnable {
        char pawn;
        Player opponent;
        Socket socket;
        Scanner input;
        PrintWriter output;

        Player(Socket socket, char pawn) {
            this.socket = socket;
            this.pawn = pawn;
        }

        @Override
        public void run() {
            try {
                setup();
                processCommands();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (opponent != null && opponent.output != null) {
                    opponent.output.println("OTHER_PLAYER_LEFT");
                }
                try {socket.close();} catch (IOException e) {e.printStackTrace();}
            }
        }

        private void setup() throws IOException {
            input = new Scanner(socket.getInputStream());
            output = new PrintWriter(socket.getOutputStream(), true);
            output.println("WELCOME " + pawn);
            if (pawn == 'B') {
                currentPlayer = this;
                output.println("MESSAGE Waiting for opponent to connect");
            } else {
                opponent = currentPlayer;
                opponent.opponent = this;
                opponent.output.println("MESSAGE Your turn");
            }
        }

        private void processCommands() throws Exception {
            while (input.hasNextLine()) {
                var command = input.nextLine();
                if (command.startsWith("QUIT")) {
                    return;
                } else if (command.startsWith("MOVE")) {
                    String[] cords = command.substring(5).split(";");
                    processMoveCommand(Integer.parseInt(cords[0]),Integer.parseInt(cords[1]));
                } else if (command.startsWith("SIZE")) {
                    setBoardSize(Integer.parseInt(command.substring(5)));
                } else if (command.startsWith("PASS")) {

                    if(passMove())
                        return;

                }
                else if (command.startsWith("SURRENDER")) {

                    output.println("END You surrendered");
                    opponent.output.println("END Enemy surrendered");
                    return;

                }
                else if (command.startsWith("BOT")) {

                    if(botExists){
                        output.println("MESSAGE Bot Egzists");
                    }
                    else{
                        ProcessBuilder builder = new ProcessBuilder("cmd.exe", "/c", "cd \"C:\\Users\\skorpi\\Desktop\\ProjeltTP\\bot\\out\\production\\bot\" && java Bot 127.0.0.1 "+boardSize);
                        builder.start();
                        botExists = true;
                    }


                }
            }
        }

        private boolean passMove(){

            if(currentPlayer == this){

                if(lastMoveWasPass && opponent != null){

                    output.println("END " + countScore());
                    opponent.output.println("END " + countScore());
                    return true;

                }

                lastMoveWasPass = true;
                output.println("PASSED");

                if (opponent != null && opponent.output != null) {
                    opponent.output.println("MESSAGE Opponent passed, your turn");
                    currentPlayer = currentPlayer.opponent;
                }

            }
            else{
                output.println("MESSAGE Not your turn");
            }

            return false;

        }

        private void processMoveCommand(int row, int col) {
            try {
                move(row, col, this);
                output.println("VALID_MOVE");
                opponent.output.println("OPPONENT_MOVED " + row + ";" + col);

            } catch (IllegalStateException e) {
                output.println("MESSAGE " + e.getMessage());
            }
        }

        private void adjustPlayersBoards(int row, int col){
            output.println("MOVE " + row + ";" + col);
            opponent.output.println("MOVE " + row + ";" + col);
        }

    }
}