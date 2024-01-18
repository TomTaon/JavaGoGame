import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;


class GUI {

    JFrame frame = new JFrame("GoGame");
    JLabel messageLabel = new JLabel("Enemy turn",SwingConstants.CENTER);

    Cross[][] board;
    Cross currentCross;

    GUI(int boardSize){

        messageLabel.setOpaque(true);
        messageLabel.setFont(new Font("Arial", Font.BOLD, 25));
        frame.getContentPane().add(messageLabel, BorderLayout.NORTH);

        setCrossPanel(boardSize);
        setMenu();

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setResizable(false);
        frame.setVisible(true);

    }

    private  void setCrossPanel(int boardSize){

        var crossPanel = new JPanel();
        crossPanel.setLayout(new GridLayout( boardSize , boardSize ));

        board = new Cross[boardSize][boardSize];
        for (int i = 0; i < boardSize; i++) {
            for (int z = 0; z < boardSize; z++) {
                final int j = i;
                final int x = z;
                board[i][z] = new Cross( i , z , boardSize);
                board[i][z].addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        currentCross = board[j][x];
                        GoGameClient.sendMessage("MOVE " + j + ";" + x);
                    }
                });
                crossPanel.add(board[i][z]);
            }
        }

        frame.getContentPane().add(crossPanel, BorderLayout.CENTER);
        frame.getContentPane().add(crossPanel, BorderLayout.CENTER);

    }

    private void setMenu() {

        JMenuBar mainMenuBar = new JMenuBar();

        String[] menuOptions = { "Options", "Pass", "Surrender","Bot"};
        String[] menuMessage = { "Options", "PASS", "SURRENDER","BOT"};


        JMenu menu = new JMenu(menuOptions[0]);
        mainMenuBar.add(menu);
        for (int j = 1; j < menuOptions.length; j++) {

            final int k = j;

            JMenuItem menuItem = new JMenuItem(menuOptions[j]);
            menuItem.addActionListener(e -> GoGameClient.sendMessage(menuMessage[k]));
            menu.add(menuItem);

        }

        frame.setJMenuBar(mainMenuBar);
    }

    static class Cross extends JPanel {

        JLabel label = new JLabel();

        String placement;

        Cross(int row, int col, int size) {


            setLayout(new GridBagLayout());

            if(row == 0 && col == 0) placement =".\\icon\\nwC.png";
            else if(row == 0 && col == size-1) placement =".\\icon\\neC.png";
            else if(row == size-1 && col == size-1) placement =".\\icon\\seC.png";
            else if(row == size-1 && col == 0) placement =".\\icon\\swC.png";
            else if(row == 0) placement =".\\icon\\nC.png";
            else if(row == size-1) placement =".\\icon\\sC.png";
            else if(col == 0) placement =".\\icon\\wC.png";
            else if(col == size-1) placement =".\\icon\\eC.png";
            else  placement =".\\icon\\cC.png";

            label.setIcon(new ImageIcon(placement));

            add(label);
        }

        void setPawn(char pawn) {

            if(placement.indexOf('C')>-1) placement = placement.replace("C", pawn + "");
            else if(placement.indexOf('B')>-1) placement = placement.replace("B", pawn + "");
            else placement = placement.replace("W", pawn + "");

            label.setIcon(new ImageIcon(placement));

        }
    }
}
