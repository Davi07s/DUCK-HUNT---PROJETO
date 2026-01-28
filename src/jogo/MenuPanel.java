package jogo;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import javax.imageio.ImageIO;
import java.io.IOException;

public class MenuPanel extends JPanel {
    private Image background;
    private Runnable onStartGame;

    private int opcaoSelecionada = 0;

    public MenuPanel(Runnable onStartGame) {
        this.onStartGame = onStartGame;
        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);

        try {

            background = ImageIO.read(getClass().getResource("/imagens/menuPanelBackground.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();

                if (key == KeyEvent.VK_UP) {
                    opcaoSelecionada = (opcaoSelecionada == 0) ? 2 : opcaoSelecionada - 1;
                }
                else if (key == KeyEvent.VK_DOWN) {
                    opcaoSelecionada = (opcaoSelecionada == 2) ? 0 : opcaoSelecionada + 1;
                }
                else if (key == KeyEvent.VK_ENTER) {
                    executarAcao();
                }
                repaint();
            }
        });
    }

    private void executarAcao() {
        if (opcaoSelecionada == 0) {
            onStartGame.run();
        } else if (opcaoSelecionada == 1) {
            JOptionPane.showMessageDialog(this, "Use as setas para navegar e Enter para selecionar.");
        } else if (opcaoSelecionada == 2) {
            System.exit(0);
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (background != null) {
            g.drawImage(background, 0, 0, 800, 600, null);
        }

        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(Color.WHITE);

        // aproxima a seta do texto centralizado
        int xSetaBase = 350;
        int ySeta = 0;

        // Mapeamento dos valores para posicionamento da seta
        switch (opcaoSelecionada) {
            case 0 -> ySeta = 165;
            case 1 -> ySeta = 255;
            case 2 -> ySeta = 345;
        }

        // Seta indicativa
        int[] xPontos = {xSetaBase, xSetaBase, xSetaBase + 15};
        int[] yPontos = {ySeta - 10, ySeta + 10, ySeta};
        g2.fillPolygon(xPontos, yPontos, 3);
    }
}