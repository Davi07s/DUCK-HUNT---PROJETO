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
    private Runnable onShowRanking;
    private Runnable onShowInstructions;
    private JFrame frame;

    private int opcaoSelecionada = 0;

    public MenuPanel(JFrame frame, Runnable onStartGame, Runnable onShowRanking) {
        this.frame = frame;
        this.onStartGame = onStartGame;
        this.onShowRanking = onShowRanking;


        Musica.tocarLoop("menu");

        this.onShowInstructions = () -> {
            InstructionPanel instrucoes = new InstructionPanel(frame, this);
            frame.setContentPane(instrucoes);
            frame.revalidate();
            frame.repaint();
            instrucoes.requestFocusInWindow();
        };

        setPreferredSize(new Dimension(800, 600));
        setFocusable(true);

        try {
            background = ImageIO.read(getClass().getResource("/imagens/menuPanel.png"));
        } catch (IOException e) {
            e.printStackTrace();
        }

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int key = e.getKeyCode();

                if (key == KeyEvent.VK_UP) {
                    opcaoSelecionada = (opcaoSelecionada == 0) ? 3 : opcaoSelecionada - 1;
                }
                else if (key == KeyEvent.VK_DOWN) {
                    opcaoSelecionada = (opcaoSelecionada == 3) ? 0 : opcaoSelecionada + 1;
                }
                else if (key == KeyEvent.VK_ENTER) {
                    executarAcao();
                }
                repaint();
            }
        });
    }

    private void executarAcao() {
        switch (opcaoSelecionada) {
            case 0 -> {
                // finaliza a musica do menu
                Musica.pararMusicaFundo();
                onStartGame.run();
            }
            case 1 -> onShowInstructions.run();
            case 2 -> onShowRanking.run();
            case 3 -> System.exit(0);
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

        int xSetaBase = 300;
        int ySeta = 0;

        switch (opcaoSelecionada) {
            case 0 -> ySeta = 85;
            case 1 -> ySeta = 150;
            case 2 -> ySeta = 210;
            case 3 -> ySeta = 270;
        }

        int[] xPontos = {xSetaBase, xSetaBase, xSetaBase + 15};
        int[] yPontos = {ySeta - 10, ySeta + 10, ySeta};
        g2.fillPolygon(xPontos, yPontos, 3);
    }
}