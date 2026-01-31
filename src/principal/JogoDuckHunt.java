package principal;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import jogo.GamePanel;
import jogo.MenuPanel;

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


    private void exibirMenu() {
        // funçãox para executar quando o botão PLAY for clicado
        MenuPanel menu = new MenuPanel(this::iniciarJogo);
        janela.setContentPane(menu);
        janela.revalidate();
    }

    // remoção do menu e iniciação do GamePanel
    private void iniciarJogo() {
        GamePanel gamePanel = new GamePanel();
        janela.setContentPane(gamePanel);
        janela.revalidate();

        //GamePanel recebe o foco para detectar o mouse e teclado
        gamePanel.requestFocusInWindow();

        // Ajusta o tamanho da janela para se adequar ao menu
        janela.pack();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(JogoDuckHunt::new); //inicia o swing na trhead
    }
}