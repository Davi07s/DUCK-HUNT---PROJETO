package principal;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import jogo.GamePanel;
import jogo.MenuPanel;
import jogo.RankingPanel;
import jogo.Musica;

public class JogoDuckHunt {

    private JFrame janela;

    public JogoDuckHunt() {

        Musica.carregar("menu", "/sons/gameClear.wav");

        janela = new JFrame("Duck Hunt");
        janela.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        janela.setResizable(false);

        exibirMenu();

        janela.pack();
        janela.setLocationRelativeTo(null);
        janela.setVisible(true);
    }

    public void exibirMenu() {
        // Criamos o menu passando a janela e as ações de navegação
        MenuPanel menu = new MenuPanel(janela, this::iniciarJogo, this::exibirRanking);

        janela.setContentPane(menu);
        janela.revalidate();
        janela.repaint();
        menu.requestFocusInWindow();
    }

    private void iniciarJogo() {

        GamePanel gamePanel = new GamePanel(this::exibirMenu);

        janela.setContentPane(gamePanel);
        janela.revalidate();


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