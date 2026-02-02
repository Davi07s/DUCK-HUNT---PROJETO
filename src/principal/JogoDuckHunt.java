package principal;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import jogo.GamePanel;
import jogo.MenuPanel;
import jogo.RankingPanel;

public class JogoDuckHunt {

    private JFrame janela;

    public JogoDuckHunt() {
        janela = new JFrame("Duck Hunt");
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setResizable(false);

        exibirMenu();

        janela.pack();
        janela.setLocationRelativeTo(null);
        janela.setVisible(true);
    }

    public void exibirMenu() {

        MenuPanel menu = new MenuPanel(janela, this::iniciarJogo, this::exibirRanking);

        janela.setContentPane(menu);
        janela.revalidate();
        janela.repaint(); //atualização visual
        menu.requestFocusInWindow();
    }


    private void iniciarJogo() {
        GamePanel gamePanel = new GamePanel();
        janela.setContentPane(gamePanel);
        janela.revalidate();

        //foco para detectar mouse e teclado
        gamePanel.requestFocusInWindow();

        // Ajusta o tamanho da janela
        janela.pack();
    }


    private void exibirRanking() {
        RankingPanel ranking = new RankingPanel(this);
        janela.setContentPane(ranking);
        janela.revalidate();

        ranking.requestFocusInWindow();
        janela.pack();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(JogoDuckHunt::new);
    }
}