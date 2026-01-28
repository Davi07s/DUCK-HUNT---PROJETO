package jogo;

import entidades.Animal;
import entidades.Pato;
import entidades.Pato.TipoPato;

import javax.swing.JPanel;
import javax.swing.Timer;
import javax.imageio.ImageIO;

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GamePanel extends JPanel {

    public static final int LARGURA = 800;
    public static final int ALTURA = 600;

    private final int ALTURA_SOLO = 380;
    private final int Y_MASCARA_GRAMA = 445;
    private final int MARGEM = 60;
    private int PATOS_MIN = 4;
    private final int LIMITE_FUGAS = 20;

    private List<Animal> animais = new ArrayList<>();
    private Random random = new Random();
    private Timer timer;

    private int pontuacao = 0;
    private int patosFugidos = 0;

    //  Dificuldades
    private int velocidadeBase = 4;
    private double intensidadeZigZag = 0;

    private boolean gameOver = false;

    private int municao = 3;
    private final int MAX_MUNICAO = 3;
    private Image bulletImg;

    private int roundAtual = 1;
    private final int PONTOS_POR_ROUND = 10;
    private boolean exibindoRound = false;
    private long tempoInicioRoundMsg = 0;
    private final int TEMPO_MSG_ROUND = 2000;

    private Image background, mira, gameOverImg;
    private int mouseX, mouseY;
    private BarraTolerancia barraTolerancia;
    private long ultimoSpawn = 0;
    private final int DELAY_SPAWN = 800;

    private List<TextoFlutuante> textos = new ArrayList<>();
    private entidades.Cachorro dog;

    public GamePanel() {
        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setFocusable(true);
        requestFocusInWindow();

        carregarImagens();
        esconderCursor();
        carregarSons();

        barraTolerancia = new BarraTolerancia(10, 50, 190, 12, LIMITE_FUGAS);
        dog = new entidades.Cachorro(-100, 380);
        dog.iniciarIntroducao();

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
                if (!dog.terminouIntroducao() && roundAtual == 1) return;
                if (municao > 0) processarDisparo(e.getPoint());
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                mouseX = e.getX();
                mouseY = e.getY();
            }
        });

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_R) recarregarMunicao();
            }
        });
    }

    private void processarDisparo(Point pontoClique) {
        municao--;
        Musica.tocar("shoot");

        for (Animal a : animais) {
            if (a instanceof Pato p) {
                if (!p.estaMorto() && p.getBounds().contains(pontoClique)) {
                    if (p.getTipo() == TipoPato.INOCENTE) {
                        finalizarJogo();
                        return;
                    }
                    p.morrer();
                    ganharPontos(p);
                    Musica.tocar("duckDead");
                    verificarRound();
                    break;
                }
            }
        }
    }

    private void ganharPontos(Pato p) {
        if (p.getTipo() == TipoPato.RARO) {
            pontuacao += 4;
            patosFugidos = Math.max(0, patosFugidos - 4);
            textos.add(new TextoFlutuante(p.getX(), p.getY(), "+4 pts", new Color(255, 215, 0)));
        } else if (p.getTipo() == TipoPato.FALSO_RARO) {
            patosFugidos += 4;
            textos.add(new TextoFlutuante(p.getX(), p.getY(), "-4", Color.RED));
        } else {
            pontuacao += 1;
            textos.add(new TextoFlutuante(p.getX(), p.getY(), "+1", Color.WHITE));
        }
    }

    private void recarregarMunicao() {
        municao = MAX_MUNICAO;
    }

    private void verificarRound() {
        int novoRound = (pontuacao / PONTOS_POR_ROUND) + 1;
        if (novoRound > roundAtual) {
            roundAtual = novoRound;
            exibindoRound = true;
            tempoInicioRoundMsg = System.currentTimeMillis();

            // Escalonamento de dificuldade
            velocidadeBase += 2;
            intensidadeZigZag += 1.5;

            if (roundAtual % 2 == 0) PATOS_MIN++;
        }
    }

    private void gerarPatos(int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            boolean vemDaEsquerda = random.nextBoolean();
            int x, vx, y, tentativas = 0;
            do {
                y = random.nextInt(ALTURA_SOLO - 120) + 20;
                tentativas++;
            } while (colideVerticalmente(y) && tentativas < 10);

            if (vemDaEsquerda) {
                x = -MARGEM;
                vx = velocidadeBase + random.nextInt(3);
            } else {
                x = LARGURA + MARGEM;
                vx = -(velocidadeBase + random.nextInt(3));
            }

            Pato p = new Pato(x, y, vemDaEsquerda, sortearTipoPato());

            // métodos
            p.setVelocidadeX(vx);
            p.setDificuldade(intensidadeZigZag);

            animais.add(p);
        }
    }

    private TipoPato sortearTipoPato() {
        int r = random.nextInt(100);
        if (r < 5) return TipoPato.RARO;
        if (r < 20) return TipoPato.FALSO_RARO;
        if (r < 35) return TipoPato.INOCENTE;
        return TipoPato.NORMAL;
    }

    private void atualizar() {
        dog.atualizar();
        if (!dog.terminouIntroducao() && roundAtual == 1 && pontuacao == 0) return;

        Iterator<Animal> it = animais.iterator();
        while (it.hasNext()) {
            Animal a = it.next();
            a.atualizar();

            if (a instanceof Pato p) {
                p.fugirDoCursor(mouseX, mouseY);
                if (p.estaMorto() && p.queda(ALTURA_SOLO)) {
                    if (p.getTipo() == TipoPato.FALSO_RARO) dog.rir(p.getX());
                    else if (p.getTipo() != TipoPato.INOCENTE) dog.celebrar(p.getX());
                    it.remove();
                    recarregarMunicao();
                    continue;
                }
            }

            if (a.getX() < -MARGEM || a.getX() > LARGURA + MARGEM) {
                if (a instanceof Pato p) {
                    if (p.contaFuga() && p.getTipo() != TipoPato.FALSO_RARO) {
                        patosFugidos++;
                        dog.rir(p.getX());
                    }
                }
                it.remove();
                recarregarMunicao();

                if (patosFugidos >= LIMITE_FUGAS) {
                    finalizarJogo();
                    return;
                }
            }
        }

        textos.removeIf(t -> { t.atualizar(); return t.terminou(); });

        long agora = System.currentTimeMillis();
        if (animais.size() < PATOS_MIN && !gameOver && agora - ultimoSpawn > DELAY_SPAWN) {
            gerarPatos(1);
            ultimoSpawn = agora;
        }

        barraTolerancia.setValor(patosFugidos);
        barraTolerancia.atualizar();
        if (exibindoRound && agora - tempoInicioRoundMsg > TEMPO_MSG_ROUND) exibindoRound = false;
    }

    private void finalizarJogo() {
        gameOver = true;
        Musica.pararMusicaFundo();
        Musica.tocar("gameClear");
        timer.stop();
        repaint();
    }

    private void carregarImagens() {
        try {
            background = ImageIO.read(getClass().getResource("/imagens/gameBackground.png"));
            mira = ImageIO.read(getClass().getResource("/imagens/gunsight.png"));
            gameOverImg = ImageIO.read(getClass().getResource("/imagens/gameover.png"));
            bulletImg = ImageIO.read(getClass().getResource("/imagens/bullet.png"));
        } catch (IOException | IllegalArgumentException e) {
            System.err.println("Erro ao carregar imagens: " + e.getMessage());
        }
    }

    private void carregarSons() {
        Musica.carregar("intro", "/sons/gameIntro.wav");
        Musica.carregar("shoot", "/sons/shoot.wav");
        Musica.carregar("duckDead", "/sons/duckDead.wav");
        Musica.carregar("gameClear", "/sons/gameClear.wav");
    }

    private void esconderCursor() {
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        Image img = toolkit.createImage(new byte[0]);
        Cursor cursorInvisivel = toolkit.createCustomCursor(img, new Point(0, 0), "cursorInvisivel");
        setCursor(cursorInvisivel);
    }

    private boolean colideVerticalmente(int yNovo) {
        for (Animal a : animais) if (Math.abs(a.getY() - yNovo) < 50) return true;
        return false;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, LARGURA, ALTURA, null);
        dog.desenhar(g);

        if (dog.terminouIntroducao()) {
            g.drawImage(background, 0, Y_MASCARA_GRAMA, LARGURA, ALTURA,
                    0, Y_MASCARA_GRAMA, LARGURA, ALTURA, null);
        }

        for (Animal a : animais) a.desenhar(g);
        for (TextoFlutuante t : textos) t.desenhar(g);

        desenharHUD(g);

        if (gameOver && gameOverImg != null) {
            g.drawImage(gameOverImg, (LARGURA - gameOverImg.getWidth(null)) / 2, (ALTURA - gameOverImg.getHeight(null)) / 2, null);
        }
        if (!gameOver) {
            g.drawImage(mira, mouseX - mira.getWidth(null) / 2, mouseY - mira.getHeight(null) / 2, null);
        }
    }

    private void desenharHUD(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString("Pontuação: " + pontuacao, 10, 20);
        g.drawString("Round: " + roundAtual, 10, 40);
        barraTolerancia.desenhar(g);

        if (bulletImg != null) {
            for (int i = 0; i < municao; i++) {
                g.drawImage(bulletImg, 180 + (i * 25), 10, 20, 30, null);
            }
        }

        if (exibindoRound) {
            g.setFont(new Font("Arial", Font.BOLD, 42));
            g.drawString("ROUND " + roundAtual, LARGURA / 2 - 110, ALTURA / 2);
        }
    }
}