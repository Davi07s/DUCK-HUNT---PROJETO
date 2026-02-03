package jogo;

import entidades.*;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.imageio.ImageIO;
import java.io.IOException;

public class InstructionPanel extends JPanel {
    private Image background;
    private JFrame frame;
    private JPanel menuPrincipal;

    // sprites
    private Image imgComum, imgRaro, imgFalsoRaro, imgInocente;

    public InstructionPanel(JFrame frame, JPanel menuPrincipal) {
        this.frame = frame;
        this.menuPrincipal = menuPrincipal;

        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);

        carregarRecursos();

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE || e.getKeyCode() == KeyEvent.VK_ENTER) {
                    voltarMenu();
                }
            }
        });
    }

    private void carregarRecursos() {
        try {
            background = ImageIO.read(getClass().getResource("/imagens/instructionPanel.png"));
            imgComum = ImageIO.read(getClass().getResource("/imagens/duckLeft1.png"));
            imgRaro = ImageIO.read(getClass().getResource("/imagens/duckGoldenLeft1.png"));
            imgFalsoRaro = ImageIO.read(getClass().getResource("/imagens/duckFalseLeft0.png"));
            imgInocente = ImageIO.read(getClass().getResource("/imagens/duckInocentLeft1.png"));
        } catch (IOException | NullPointerException e) {
            System.err.println("Erro ao carregar imagens das instruções: " + e.getMessage());
        }
    }

    private void voltarMenu() {
        frame.setContentPane(menuPrincipal);
        frame.revalidate();
        frame.repaint();

        // invokeLater
        SwingUtilities.invokeLater(() -> {
            menuPrincipal.requestFocusInWindow();
        });
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (background != null) {
            g2.drawImage(background, 0, 0, 800, 600, null);
        }

        g2.setFont(new Font("Arial", Font.BOLD, 38));
        g2.setColor(Color.WHITE);
        g2.drawString("INSTRUÇÕES", 280, 80);

        desenharTabelaPatos(g2);


        g2.setFont(new Font("Arial", Font.BOLD, 20));
        g2.setColor(Color.WHITE);
        g2.drawString("REGRAS:", 100, 380);

        g2.setFont(new Font("Arial", Font.PLAIN, 18));
        g2.drawString("Pressione R para recarregar manualmente.", 100, 415);
        g2.drawString("Cada acerto recupera 1 munição na reserva.", 100, 440);
        g2.drawString("Se a barra de tolerância for totalmente preenchida, será fim de jogo.", 100, 465);

        g2.setFont(new Font("Arial", Font.ITALIC, 16));
        g2.drawString("Pressione ESC ou ENTER para voltar", 260, 560);
    }

    private void desenharTabelaPatos(Graphics2D g2) {
        int y = 160;
        int xIni = 80;
        int espaco = 175;


        Object[][] dados = {
                {"COMUM", "+ 1 pts", imgComum},
                {"RARO", "+ 4 pts", imgRaro},
                {"FALSO RARO", "- 4 pts", imgFalsoRaro},
                {"INOCENTE", "Game over", imgInocente}
        };

        for (int i = 0; i < dados.length; i++) {
            int cx = xIni + (i * espaco);

            // Moldura translúcida
            g2.setColor(new Color(255, 255, 255, 60));
            g2.fillRoundRect(cx, y, 130, 130, 20, 20);

            //sprite do pato na moldura
            Image spritePato = (Image) dados[i][2];
            if (spritePato != null) {
                // Ajusta o tamanho da sprite
                g2.drawImage(spritePato, cx + 25, y + 15, 80, 80, null);
            }

            g2.setColor(Color.WHITE);
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString((String) dados[i][0], cx + 25, y + 110);

            g2.setColor(Color.YELLOW);
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString((String) dados[i][1], cx + 35, y + 128);
        }
    }
}