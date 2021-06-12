import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import javax.imageio.ImageIO;
import javax.imageio.stream.ImageInputStream;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.LineBorder;

public class Game {
    public int turn = 0;
    public static int[] board = new int[9];
    public static int[][] winTypes = {{0, 1, 2}, {3, 4, 5}, {6, 7, 8},
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
            {0, 4, 8}, {2, 4, 6}};
    public static JPanel panel;
    public static boolean playing = true;

    public static void main(String[] args) {
        new Game();
    }

    public Game() {
        EventQueue.invokeLater(() -> {
            JFrame frame = new JFrame();

            panel = new JPanel(new GridLayout(3, 3));
            for (int i = 0; i < 9; i++) {
                panel.add(new GridPane(i / 3, i % 3));
            }

            frame.add(panel);
            try {
                frame.setIconImage(ImageIO.read(getClass().getResourceAsStream("unnamed.png")));
            } catch (IOException ignored) {}
            frame.pack();
            frame.setLocationRelativeTo(null);
            frame.setTitle("Unbeatable Tic Tac Toe");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setVisible(true);
        });
    }

    public int checkWin(int loc) {
        int i = 0;
        for(int[] winType : winTypes){
            int a = winType[0], b = winType[1], c = winType[2];

            if(a == loc || b == loc || c == loc){
                if(board[a] ==  board[b] && board[a] == board[c])
                    return i;
            }
            i++;
        }
        return -1;
    }

    public boolean checkWin(int loc, int[] board) {
        for(int[] winType : winTypes){
            int a = winType[0], b = winType[1], c = winType[2];

            if(a == loc || b == loc || c == loc){
                if(board[a] ==  board[b] && board[a] == board[c])
                    return true;
            }
        }
        return false;
    }

    public void showWin(int type){
        playing = false;

        for(int i : winTypes[type]) {
            GridPane pane = ((GridPane) panel.getComponent(i));
            pane.winPane();
        }

        for(Component c : panel.getComponents()){
            c.removeNotify();
            c.addNotify();
        }
    }

    public void checkDraw(){
        for(int i : board){
            if(i == 0)
                return;
        }

        playing = false;

        for(Component c : panel.getComponents()){
            ((GridPane)c).winPane();
            c.removeNotify();
            c.addNotify();
        }
    }

    public boolean checkDraw(int[] board){
        for(int i : board){
            if(i == 0)
                return false;
        }
        return true;
    }

    public void initNewGame(){
        turn = 0;
        playing = true;
        Arrays.fill(board, 0);
        for(Component c : panel.getComponents()) {
            c.setBackground(null);
            ((GridPane)c).chose = 0;
            c.removeNotify();
            c.addNotify();
            ((GridPane) c).setBorder(new LineBorder(Color.BLACK));
        }
    }

    public void playComputer(int loc){
        GridPane pane = (GridPane) panel.getComponent(loc);
        pane.setBackground(new Color(0xcbcdf7));
        board[loc] = pane.chose = 2;
        if(checkWin(loc) > -1) {
            showWin(checkWin(loc));
        }
    }

    public class GridPane extends JPanel {
        private int i;
        private int j;
        private int chose = 0;

        private MouseListener mouseHandler;

        public GridPane(int i, int j) {
            this.i = i;
            this.j = j;
            setBorder(new LineBorder(Color.BLACK));
        }

        public void winPane(){
            setBorder(new LineBorder(new Color(0x7376ec), 6));
        }

        @Override
        public Dimension getPreferredSize() {
            return new Dimension(200, 200);
        }

        @Override
        public void addNotify() {
            super.addNotify();

            if (mouseHandler != null) {
                removeMouseListener(mouseHandler);
            }

            if(playing){
                mouseHandler = new MouseAdapter() {
                    @Override
                    public void mouseEntered(MouseEvent e) {
                        if (board[i * 3 + j] != 0)
                            return;
                        if (chose != 0)
                            return;
                        setBackground(new Color(0x595959));
                    }

                    @Override
                    public void mouseExited(MouseEvent e) {
                        if (board[i * 3 + j] != 0)
                            return;
                        if (chose != 0)
                            return;
                        setBackground(null);
                    }

                    @Override
                    public void mouseClicked(MouseEvent e) {
                        if (board[i * 3 + j] != 0)
                            return;
                        switch (turn) {
                            case 0:
                                setBackground(new Color(0xffccc2));
                                //turn = turn == 0 ? 1 : 0;
                                board[i * 3 + j] = chose = 1;
                                if(checkWin(i*3 + j) > -1 || checkDraw(board))
                                    break;
                                Choice c = minimax(1, true, 1, board);
                                playComputer(c.loc);
                                break;
                            case 1:
                                setBackground(new Color(0xcbcdf7));
                                turn = turn == 0 ? 1 : 0;
                                board[i * 3 + j] = chose = 2;
                                break;
                        }
                        int wins = checkWin(i * 3 + j);
                        if (wins > -1) {
                            showWin(wins);
                        }
                        checkDraw();
                    }
                };
            } else {
                mouseHandler = new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        initNewGame();
                    }
                };
            }

            addMouseListener(mouseHandler);
        }

        @Override
        public void removeNotify() {
            super.removeNotify();
            if (mouseHandler != null) {
                removeMouseListener(mouseHandler);
            }
        }
    }

    public Choice minimax(int turn, boolean isMax, int depth, int[] board){
        ArrayList<Choice> res = new ArrayList<>();
        for(int i = 0; i < board.length; i++){
            if(board[i] == 0){
                int[] b = board.clone();
                b[i] = turn + 1;
                if(checkWin(i, b)){
                    res.add(new Choice(i, isMax ? 9 - depth : depth - 9));
                } else if(checkDraw(b)) {
                    res.add(new Choice(i, 0));
                } else {
                    int sc = minimax(turn == 1 ? 0 : 1, !isMax, depth + 1, b).score;
                    res.add(new Choice(i, sc));
                }
            }
        }
        int mn = 0;
        int mx = 0;
        for(int i = 1; i < res.size(); i++){
            if(res.get(mn).score > res.get(i).score){
                mn = i;
            }
            if(res.get(mx).score < res.get(i).score){
                mx = i;
            }
        }

        if(isMax)
            return res.get(mx);
        return res.get(mn);
    }
}