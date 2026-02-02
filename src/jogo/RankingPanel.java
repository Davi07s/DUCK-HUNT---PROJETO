package jogo;

import ranking.GerenciadorRanking;
import principal.JogoDuckHunt;
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.util.List;

public class RankingPanel extends JPanel {
    private Image background;
    private JogoDuckHunt principal;

    public RankingPanel(JogoDuckHunt principal) {
        this.principal = principal;
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);

        try {
            background = ImageIO.read(getClass().getResource("/imagens/menuRanking.png"));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Erro: Imagem de fundo do ranking não encontrada.");
        }

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    principal.exibirMenu();
                }
            }
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (background != null) {
            g.drawImage(background, 0, 0, 800, 600, null);
        } else {
            g.setColor(Color.BLACK);
            g.fillRect(0, 0, 800, 600);
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<Integer> scores = GerenciadorRanking.lerRecordes();

        Color[] coresPodio = {
                new Color(255, 215, 0),  // P1: Dourado
                new Color(192, 192, 192), // P2: Prata
                new Color(205, 127, 50)   // P3: Bronze
        };

        // Alinhamento centralizado e espaçamento
        int xTexto = 180;
        int yBase = 240;
        int espacamento = 90;


        Font fonteRanking = new Font("Consolas", Font.BOLD, 50);
        g2.setFont(fonteRanking);

        for (int i = 0; i < 3; i++) {
            int yAtual = yBase + (i * espacamento);
            String prefixo = "P" + (i + 1);

            // 1. Desenhar Prefixo (P1, P2 ou P3)
            g2.setColor(coresPodio[i]);
            g2.drawString(prefixo, xTexto, yAtual + 15);

            // 2. Seta Branca
            int larguraPrefixo = g2.getFontMetrics().stringWidth(prefixo);
            int xSeta = xTexto + larguraPrefixo + 15;

            g2.setColor(Color.WHITE);
            int[] xPontos = {xSeta, xSeta, xSeta + 15};
            int[] yPontos = {yAtual - 10, yAtual + 10, yAtual};
            g2.fillPolygon(xPontos, yPontos, 3);

            // 3. Valor e "PONTOS" na mesma cor e fonte
            int xValor = xSeta + 35;
            String valorTexto = "---";

            if (i < scores.size()) {
                int s = scores.get(i);
                valorTexto = (s < 10) ? String.format("%02d", s) : String.valueOf(s);
            }

            String textoCompleto = valorTexto + " PONTOS";
            g2.setColor(coresPodio[i]);
            g2.drawString(textoCompleto, xValor, yAtual + 15);
        }


        g2.setFont(new Font("Consolas", Font.PLAIN, 18));
        g2.setColor(Color.WHITE);
        g2.drawString("Pressione ENTER para retornar", 20, 570);
    }
}