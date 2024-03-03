import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Random;

public class SnakeGame extends JPanel implements ActionListener {

    private final int WINDOW_WIDTH = 900;
    private final int WINDOW_HEIGHT = 900;
    private final int DOT_SIZE = 150;
    private final int ALL_DOTS = 900;
//    private final int RANDOM_POSITION = (WINDOW_WIDTH / DOT_SIZE) - 1;
    private final int DELAY = 200;

    private final List<Point> snake = new ArrayList<>();
    private Point food;
    private Timer timer;
    private boolean inGame = true;
    private char direction = 'R'; // 'R'ight, 'L'eft, 'U'p, 'D'own

    private boolean isPaused = false;

    int boardRows = WINDOW_HEIGHT / DOT_SIZE;
    int boardCols = WINDOW_WIDTH / DOT_SIZE;
    boolean[][] board = new boolean[boardRows][boardCols]; // true表示格子被占据，false表示空闲



    public SnakeGame() {
        setPreferredSize(new Dimension(WINDOW_WIDTH, WINDOW_HEIGHT));
        setBackground(Color.BLACK);
        initGame();
        addKeyListener(new TAdapter());
        setFocusable(true);
    }

    private void initGame() {
        // Initialize the snake with 3 dots.
        for (int z = 0; z < 3; z++) {
            snake.add(new Point((2*DOT_SIZE - z * DOT_SIZE), 0));
        }

        updateBoard();
        locateFood();

        timer = new Timer(DELAY, this);
        timer.start();

        requestFocusInWindow(); // 请求焦点以接收键盘事件
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        doDrawing(g);

        // 在这里绘制蛇的长度
        drawSnakeLength(g);


        if (isPaused) {
            drawGamePause(g);
        }
    }

    private void doDrawing(Graphics g) {
        if (inGame) {
            g.setColor(Color.RED);
            g.fillRect(food.x, food.y, DOT_SIZE, DOT_SIZE);

            for (int i = 0; i < snake.size(); i++) {
                if(i<=20){
                    g.setColor(new Color(0, 150 + i*5, 0));
                }else {
                    g.setColor(new Color(0, 255, 0));
//                    g.setColor(Color.GREEN);
                }

                g.fillRect(snake.get(i).x, snake.get(i).y, DOT_SIZE, DOT_SIZE);
            }

            Toolkit.getDefaultToolkit().sync();
        } else {
            gameOver(g);
        }

    }

    private void drawSnakeLength(Graphics g) {
        String lengthMsg = "Length: " + snake.size();
        g.setColor(Color.WHITE); // 设置文字颜色
        g.setFont(new Font("Arial", Font.BOLD, 18)); // 设置文字字体和大小
        g.drawString(lengthMsg, 800, 20); // 在右上角绘制蛇的长度
    }

    private void drawGamePause(Graphics g){
        String msg = "Pause";
        Font small2 = new Font("Helvetica", Font.BOLD, 36);
        FontMetrics metr2 = getFontMetrics(small2);

        g.setColor(Color.white);
        g.setFont(small2);
        g.drawString(msg, (WINDOW_WIDTH - metr2.stringWidth(msg)) / 2, WINDOW_HEIGHT / 2);
    }

    private void gameOver(Graphics g) {
        String msg = "Game Over";
        Font small = new Font("Helvetica", Font.BOLD, 96);
        FontMetrics metr = getFontMetrics(small);

        g.setColor(Color.white);
        g.setFont(small);
        g.drawString(msg, (WINDOW_WIDTH - metr.stringWidth(msg)) / 2, WINDOW_HEIGHT / 2);
    }

    private void checkFood() {
        Point head = snake.get(0);
        if (head.equals(food)) {
            snake.add(new Point(-1, -1)); // Temporarily add at invalid position, will be set correctly in move()
            locateFood();
        }
    }

    // 初始化或更新游戏板状态的方法
    private void updateBoard() {
        // 先清空游戏板
        for (int i = 0; i < boardRows; i++) {
            Arrays.fill(board[i], false);
        }
        // 标记蛇占据的格子
        for (Point point : snake) {
            int row = point.y / DOT_SIZE;
            int col = point.x / DOT_SIZE;
            if (row >= 0 && row < boardRows && col >= 0 && col < board[0].length) {
                board[row][col] = true;
            }
        }
    }

    private void move() {
        for (int z = snake.size() - 1; z > 0; z--) {
            snake.get(z).setLocation(snake.get(z - 1));
        }

        Point head = snake.get(0);
        switch (direction) {
            case 'U':
                head.y -= DOT_SIZE;
                break;
            case 'D':
                head.y += DOT_SIZE;
                break;
            case 'L':
                head.x -= DOT_SIZE;
                break;
            case 'R':
                head.x += DOT_SIZE;
                break;
        }
    }

    private void checkCollision() {
        Point head = snake.get(0);
        for (int z = snake.size() - 1; z > 0; z--) {
            if (head.equals(snake.get(z))) {
                inGame = false;
                break;
            }
        }

        if (head.x >= WINDOW_WIDTH || head.x < 0 || head.y >= WINDOW_HEIGHT || head.y < 0) {
            inGame = false;
        }

        if (!inGame) {
            timer.stop();
        }
    }

    private void locateFood() {
        int rX, rY;
        do {
            rX = (int) (Math.random() * boardCols);
            rY = (int) (Math.random() * boardRows);
        } while (board[rY][rX]); // 如果这个格子已经被蛇占据，则重新选择
//        int x = (int) (Math.random() * (WINDOW_WIDTH / DOT_SIZE));
//        int y = (int) (Math.random() * (WINDOW_HEIGHT / DOT_SIZE));
        food = new Point(rX * DOT_SIZE, rY * DOT_SIZE);
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (inGame && !isPaused) {
            updateBoard();
            checkFood();
            checkCollision();
            move();
        }
        repaint();
    }

    private class TAdapter extends KeyAdapter {
        @Override
        public void keyPressed(KeyEvent e) {
            int key = e.getKeyCode();

            if (key == KeyEvent.VK_P) {
                isPaused = !isPaused; // 切换暂停状态
                if (isPaused) {
                    timer.stop();
                } else {
                    timer.start();
                }
                repaint();
            }

            if ((key == KeyEvent.VK_LEFT) && (direction != 'R')) {
                direction = 'L';
            }

            if ((key == KeyEvent.VK_RIGHT) && (direction != 'L')) {
                direction = 'R';
            }

            if ((key == KeyEvent.VK_UP) && (direction != 'D')) {
                direction = 'U';
            }

            if ((key == KeyEvent.VK_DOWN) && (direction != 'U')) {
                direction = 'D';
            }

        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Snake Game");
            frame.add(new SnakeGame());
            frame.pack();
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setLocationRelativeTo(null);
            frame.setVisible(true);
        });
    }
}
