package principal;

import javax.swing.JFrame;
import jogo.GamePanel;

public class JogoDuckHunt {

    public static void main(String[] args) {

        JFrame janela = new JFrame("Duck Hunt");

        GamePanel gamePanel = new GamePanel();

        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setResizable(false);
        janela.add(gamePanel);
        janela.pack();
        janela.setLocationRelativeTo(null);
        janela.setVisible(true);
    }
}

