package jogo;

import entidades.Animal;
import entidades.Pato;

import javax.swing.JPanel;
import javax.swing.Timer;
import javax.imageio.ImageIO;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Color;
import java.awt.Image;
import java.awt.Cursor;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel {

    public static final int LARGURA = 800;
    public static final int ALTURA = 600;

    private final int ALTURA_SOLO = 380;
    private final int MARGEM = 60;
    private final int PATOS_MIN = 7;
    private final int LIMITE_FUGAS = 20;

    private List<Animal> animais = new ArrayList<>();
    private Random random = new Random();

    private Timer timer;

    private int pontuacao = 0;
    private int patosFugidos = 0;
    private int velocidadeBase = 2;
    private boolean gameOver = false;

    private Image background;
    private Image mira;
    private Image gameOverImg;

    private int mouseX;
    private int mouseY;

    // Barra de tolerância
    private float barraFugasAnimada = 0f;

    // Progressão
    private long tempoInicio;
    private int nivelDificuldade = 1;

    // Controle de spawn
    private long ultimoSpawn = 0;
    private final int DELAY_SPAWN = 800;

    public GamePanel() {
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setFocusable(true);

        carregarImagens();
        esconderCursor();
        carregarSons();

        tempoInicio = System.currentTimeMillis();

        // Spawn inicial reduzido
        int quantidadeInicial = 5 + random.nextInt(3);
        for (int i = 0; i < quantidadeInicial; i++) {
            gerarPatos(1);
            try {
                Thread.sleep(180);
            } catch (InterruptedException ignored) {}
        }

        Musica.tocarLoop("intro");

        timer = new Timer(16, e -> {
            atualizar();
            repaint();
        });
        timer.start();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (gameOver) return;

                Musica.tocar("shoot");

                for (Animal a : animais) {
                    if (a instanceof Pato p) {
                        if (!p.estaMorto() && p.getBounds().contains(e.getPoint())) {
                            p.morrer();
                            pontuacao++;
                            Musica.tocar("duckDead");
                            return;
                        }
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });
    }

    private void carregarSons() {
        Musica.carregar("intro", "/sons/gameIntro.wav");
        Musica.carregar("shoot", "/sons/shoot.wav");
        Musica.carregar("duckDead", "/sons/duckDead.wav");
        Musica.carregar("gameClear", "/sons/gameClear.wav");
        Musica.carregar("duckCall", "/sons/duckCall.wav");
        Musica.carregar("capturedDuck", "/sons/capturedDuck.wav");
    }

    private void carregarImagens() {
        try {
            background = ImageIO.read(getClass().getResource("/imagens/gameBackground.png"));
            mira = ImageIO.read(getClass().getResource("/imagens/gunsight.png"));
            gameOverImg = ImageIO.read(getClass().getResource("/imagens/gameover.png"));
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    private void esconderCursor() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image img = toolkit.createImage(new byte[0]);
        Cursor cursorInvisivel = toolkit.createCustomCursor(
                img, new Point(0, 0), "cursorInvisivel"
        );
        setCursor(cursorInvisivel);
    }

    private void atualizar() {
        Iterator<Animal> it = animais.iterator();

        while (it.hasNext()) {
            Animal a = it.next();
            a.atualizar();

            if (a instanceof Pato p) {
                p.fugirDoCursor(mouseX, mouseY);

                if (p.estaMorto() && p.queda(ALTURA_SOLO)) {
                    it.remove();
                    continue;
                }
            }

            if (a.getX() < -MARGEM || a.getX() > LARGURA + MARGEM) {
                it.remove();
                patosFugidos++;

                if (patosFugidos >= LIMITE_FUGAS) {
                    gameOver = true;
                    timer.stop();
                    Musica.pararMusicaFundo();
                    Musica.tocar("gameClear");
                    return;
                }
            }
        }

        // Spawn contínuo
        long agora = System.currentTimeMillis();
        if (animais.size() < PATOS_MIN && !gameOver) {
            if (agora - ultimoSpawn > DELAY_SPAWN) {
                gerarPatos(1);
                ultimoSpawn = agora;
            }
        }

        atualizarDificuldade();

        float progressoReal = (float) patosFugidos / LIMITE_FUGAS;
        barraFugasAnimada += (progressoReal - barraFugasAnimada) * 0.1f;
    }

    private void atualizarDificuldade() {
        long tempoJogo = (System.currentTimeMillis() - tempoInicio) / 1000;

        if (tempoJogo >= nivelDificuldade * 10) {
            nivelDificuldade++;
            velocidadeBase++;
        }
    }

    private void gerarPatos(int quantidade) {
        for (int i = 0; i < quantidade; i++) {

            boolean vemDaEsquerda = random.nextBoolean();

            int x;
            int vx;
            int y;
            int tentativas = 0;

            do {
                y = random.nextInt(ALTURA_SOLO - 100) + 20;
                tentativas++;
            } while (colideVerticalmente(y) && tentativas < 10);

            if (vemDaEsquerda) {
                x = -MARGEM;
                vx = velocidadeBase + random.nextInt(2);
            } else {
                x = LARGURA + MARGEM;
                vx = -(velocidadeBase + random.nextInt(2));
            }

            Pato p = new Pato(x, y, vemDaEsquerda);
            p.setVelocidadeX(vx);

            animais.add(p);
        }
    }

    private boolean colideVerticalmente(int yNovo) {
        for (Animal a : animais) {
            if (Math.abs(a.getY() - yNovo) < 50) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (background != null) {
            g.drawImage(background, 0, 0, LARGURA, ALTURA, null);
        }

        for (Animal a : animais) {
            a.desenhar(g);
        }

        // hud do jogo
        g.setColor(Color.BLACK);
        g.drawString("Pontuação: " + pontuacao, 10, 20);

        // Barra de tolerância
        int barraX = 10;
        int barraY = 30;
        int barraLargura = 200;
        int barraAltura = 12;

        g.setColor(new Color(0, 0, 0, 120));
        g.fillRoundRect(barraX, barraY, barraLargura, barraAltura, 10, 10);

        int larguraAtual = (int) (barraLargura * barraFugasAnimada);

        Color cor;
        if (barraFugasAnimada < 0.5f) {
            cor = new Color(0, 200, 0, 160);
        } else if (barraFugasAnimada < 0.8f) {
            cor = new Color(255, 200, 0, 180);
        } else {
            cor = new Color(220, 50, 50, 200);
        }

        g.setColor(cor);
        g.fillRoundRect(barraX, barraY, larguraAtual, barraAltura, 10, 10);

        // game over
        if (gameOver && gameOverImg != null) {
            int w = gameOverImg.getWidth(null);
            int h = gameOverImg.getHeight(null);
            int x = (LARGURA - w) / 2;
            int y = (ALTURA - h) / 2;
            g.drawImage(gameOverImg, x, y, null);
        }

        // mira - não aparece após o gamer over
        if (mira != null && !gameOver) {
            int w = mira.getWidth(null);
            int h = mira.getHeight(null);
            g.drawImage(mira, mouseX - w / 2, mouseY - h / 2, null);
        }
    }
}
