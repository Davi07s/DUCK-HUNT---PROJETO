package jogo;

import entidades.*;
import entidades.PatoComum;
import entidades.PatoRaro;
import entidades.PatoFalsoRaro;
import entidades.PatoInocente;
import ranking.GerenciadorRanking;

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

    private int velocidadeBase = 4;
    private double intensidadeZigZag = 0;

    private boolean gameOver = false;

    // munição e recarga
    private int municao = 5;
    private final int MAX_MUNICAO = 5;
    private final int RESERVA_INICIAL = 15;
    private int reservaMunicao = RESERVA_INICIAL;
    private final int MAX_RESERVA = 40;
    private boolean recarregando = false;
    private long tempoInicioRecarga = 0;
    // Tempo reduzido para 1.5 segundos para melhorar a fluidez
    private final int DELAY_RECARGA = 1500;

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
    private int delaySpawnAtual = 600;

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
                if (gameOver || recarregando) return;
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
                if (!gameOver && e.getKeyCode() == KeyEvent.VK_R) {
                    recarregarMunicao();
                }
                if (gameOver && (e.getKeyCode() == KeyEvent.VK_R || e.getKeyCode() == KeyEvent.VK_ENTER)) {
                    reiniciarJogo();
                }
            }
        });
    }

    public void adicionarPontuacao(int pts) {
        this.pontuacao += pts;
    }

    public void reduzirFugas(int qtd) {
        this.patosFugidos = Math.max(0, this.patosFugidos - qtd);
    }

    public void penalizarFuga(int qtd) {
        this.patosFugidos += qtd;
    }

    public void adicionarTextoFlutuante(int x, int y, String t, Color c) {
        textos.add(new TextoFlutuante(x, y, t, c));
    }

    private void reiniciarJogo() {
        pontuacao = 0;
        patosFugidos = 0;
        roundAtual = 1;
        velocidadeBase = 4;
        intensidadeZigZag = 0;
        delaySpawnAtual = 600;
        PATOS_MIN = 4;
        municao = MAX_MUNICAO;
        reservaMunicao = RESERVA_INICIAL;
        recarregando = false;
        gameOver = false;

        animais.clear();
        textos.clear();

        barraTolerancia.setValor(0);
        dog = new entidades.Cachorro(-100, 380);
        dog.iniciarIntroducao();

        Musica.pararMusicaFundo();
        Musica.tocarLoop("intro");
        timer.start();
    }

    private void processarDisparo(Point pontoClique) {
        municao--;
        Musica.tocar("shoot");

        for (Animal a : animais) {
            if (a instanceof Pato p && !p.estaMorto() && p.getBounds().contains(pontoClique)) {
                p.aoSerAtingido(this);

                if (reservaMunicao < MAX_RESERVA) {
                    reservaMunicao++;
                }

                Musica.tocar("duckDead");
                verificarRound();
                break;
            }
        }
    }

    private void recarregarMunicao() {
        if (!recarregando && municao < MAX_MUNICAO && reservaMunicao > 0) {
            recarregando = true;
            tempoInicioRecarga = System.currentTimeMillis();
        }
    }

    private void verificarRound() {
        int novoRound = (pontuacao / PONTOS_POR_ROUND) + 1;
        if (novoRound > roundAtual) {
            roundAtual = novoRound;
            exibindoRound = true;
            tempoInicioRoundMsg = System.currentTimeMillis();

            reservaMunicao = RESERVA_INICIAL;
            adicionarTextoFlutuante(LARGURA/2, 100, "RESERVA REABASTECIDA!", Color.WHITE);

            velocidadeBase += 2;
            intensidadeZigZag += 1.5;
            delaySpawnAtual = Math.max(250, delaySpawnAtual - 50);
            PATOS_MIN++;
        }
    }

    private void gerarPatos(int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            boolean vemDaEsquerda = random.nextBoolean();
            int x = vemDaEsquerda ? -MARGEM : LARGURA + MARGEM;
            int y = random.nextInt(ALTURA_SOLO - 120) + 20;

            Pato p = sortearInstanciaPato(x, y, vemDaEsquerda);

            int vx = vemDaEsquerda ? velocidadeBase + random.nextInt(3) : -(velocidadeBase + random.nextInt(3));
            p.setVelocidadeX(vx);
            p.setDificuldade(intensidadeZigZag);
            animais.add(p);
        }
    }

    private Pato sortearInstanciaPato(int x, int y, boolean dir) {
        int r = random.nextInt(100);
        if (r < 20) return new PatoRaro(x, y, dir);
        if (r < 35) return new PatoFalsoRaro(x, y, dir);
        if (r < 50) return new PatoInocente(x, y, dir);
        return new PatoComum(x, y, dir);
    }

    private void atualizar() {
        long agora = System.currentTimeMillis();

        if (recarregando && agora - tempoInicioRecarga > DELAY_RECARGA) {
            int falta = MAX_MUNICAO - municao;
            if (reservaMunicao >= falta) {
                reservaMunicao -= falta;
                municao = MAX_MUNICAO;
            } else {
                municao += reservaMunicao;
                reservaMunicao = 0;
            }
            recarregando = false;
        }

        dog.atualizar();
        if (!dog.terminouIntroducao() && roundAtual == 1 && pontuacao == 0) return;

        Iterator<Animal> it = animais.iterator();
        while (it.hasNext()) {
            Animal a = it.next();
            a.atualizar();

            if (a instanceof Pato p) {
                p.fugirDoCursor(mouseX, mouseY);

                if (p.estaMorto() && p.queda(ALTURA_SOLO)) {
                    if (p instanceof PatoFalsoRaro) dog.rir(p.getX());
                    else if (!(p instanceof PatoInocente)) dog.celebrar(p.getX());

                    it.remove();
                    continue;
                }

                if (p.getY() < -MARGEM) {
                    if (p.contaFuga()) {
                        patosFugidos++;
                        dog.rir(p.getX());
                    }
                    it.remove();
                    continue;
                }
            }
        }

        if (municao == 0 && animais.isEmpty() && !recarregando && reservaMunicao > 0) {
            recarregarMunicao();
        }

        textos.removeIf(t -> { t.atualizar(); return t.terminou(); });

        if (animais.size() < PATOS_MIN && !gameOver && agora - ultimoSpawn > delaySpawnAtual) {
            int qtd = (roundAtual > 3 && random.nextBoolean()) ? 2 : 1;
            gerarPatos(qtd);
            ultimoSpawn = agora;
        }

        barraTolerancia.setValor(patosFugidos);
        barraTolerancia.atualizar();
        if (exibindoRound && agora - tempoInicioRoundMsg > TEMPO_MSG_ROUND) exibindoRound = false;

        if (patosFugidos >= LIMITE_FUGAS) {
            finalizarJogo();
        }
    }

    public void finalizarJogo() {
        gameOver = true;
        Musica.pararMusicaFundo();
        Musica.tocar("gameClear");
        GerenciadorRanking.salvarRecorde(this.pontuacao);
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

        if (gameOver) {
            desenharGameOver(g);
        } else if (!recarregando) {
            g.drawImage(mira, mouseX - mira.getWidth(null) / 2, mouseY - mira.getHeight(null) / 2, null);
        }
    }

    private void desenharGameOver(Graphics g) {
        if (gameOverImg != null) {
            int gx = (LARGURA - gameOverImg.getWidth(null)) / 2;
            int gy = (ALTURA - gameOverImg.getHeight(null)) / 2 - 50;
            g.drawImage(gameOverImg, gx, gy, null);

            g.setFont(new Font("Consolas", Font.BOLD, 28));
            FontMetrics fm = g.getFontMetrics();

            String parabens = "PARABÉNS!";
            String pontosStr = "PONTUAÇÃO FINAL: " + pontuacao;
            String resetStr = "Pressione [R] para reiniciar";

            g.setColor(Color.WHITE);
            g.drawString(parabens, (LARGURA - fm.stringWidth(parabens)) / 2, gy + gameOverImg.getHeight(null) + 40);
            g.drawString(pontosStr, (LARGURA - fm.stringWidth(pontosStr)) / 2, gy + gameOverImg.getHeight(null) + 80);

            g.setFont(new Font("Consolas", Font.ITALIC, 18));
            g.drawString(resetStr, (LARGURA - g.getFontMetrics().stringWidth(resetStr)) / 2, gy + gameOverImg.getHeight(null) + 120);
        }
    }

    private void desenharHUD(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString("Pontuação: " + pontuacao, 10, 20);
        g.drawString("Round: " + roundAtual, 10, 40);

        // Munições reservas em branco
        g.setColor(Color.WHITE);
        g.drawString("Munições reservas: " + reservaMunicao, 10, 75);

        // MENSAGEM DE RECARGA: Abaixo das munições reservas (Y=95), branca e tamanho 14
        if (recarregando) {
            g.setFont(new Font("Arial", Font.BOLD, 14));
            g.setColor(Color.WHITE);
            g.drawString("Recarregando...", 10, 95);
        }

        barraTolerancia.desenhar(g);

        if (bulletImg != null) {
            for (int i = 0; i < municao; i++) {
                g.drawImage(bulletImg, 180 + (i * 25), 10, 20, 30, null);
            }
        }

        if (exibindoRound) {
            g.setFont(new Font("Arial", Font.BOLD, 42));
            g.setColor(Color.WHITE);
            g.drawString("ROUND " + roundAtual, LARGURA / 2 - 110, ALTURA / 2);
        }
    }
}