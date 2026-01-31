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
                // Recarga normal se não for game over
                if (!gameOver && e.getKeyCode() == KeyEvent.VK_R && municao == 0) {
                    recarregarMunicao();
                }
                // Reiniciar se for game over
                if (gameOver && (e.getKeyCode() == KeyEvent.VK_R || e.getKeyCode() == KeyEvent.VK_ENTER)) {
                    reiniciarJogo();
                }
            }
        });
    }

    private void reiniciarJogo() {
        // Reseta atributos para o estado inicial
        pontuacao = 0;
        patosFugidos = 0;
        roundAtual = 1;
        velocidadeBase = 4;
        intensidadeZigZag = 0;
        delaySpawnAtual = 600;
        PATOS_MIN = 4;
        municao = MAX_MUNICAO;
        gameOver = false;

        // Limpa listas
        animais.clear();
        textos.clear();

        // Reseta cachorro e barra
        barraTolerancia.setValor(0);
        dog = new entidades.Cachorro(-100, 380);
        dog.iniciarIntroducao();

        // Reinicia áudio e timer
        Musica.pararMusicaFundo();
        Musica.tocarLoop("intro");
        timer.start();
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

            velocidadeBase += 2;
            intensidadeZigZag += 1.5;
            delaySpawnAtual = Math.max(250, delaySpawnAtual - 50);
            PATOS_MIN++;
        }
    }

    private void gerarPatos(int quantidade) {
        for (int i = 0; i < quantidade; i++) {
            boolean vemDaEsquerda = random.nextBoolean();
            int x, vx, y;
            y = random.nextInt(ALTURA_SOLO - 120) + 20;

            if (vemDaEsquerda) {
                x = -MARGEM;
                vx = velocidadeBase + random.nextInt(3);
            } else {
                x = LARGURA + MARGEM;
                vx = -(velocidadeBase + random.nextInt(3));
            }

            Pato p = new Pato(x, y, vemDaEsquerda, sortearTipoPato());
            p.setVelocidadeX(vx);
            p.setDificuldade(intensidadeZigZag);
            animais.add(p);
        }
    }

    private TipoPato sortearTipoPato() {
        int r = random.nextInt(100);
        if (r < 10) return TipoPato.RARO;
        if (r < 35) return TipoPato.FALSO_RARO;
        if (r < 50) return TipoPato.INOCENTE;
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
                    continue;
                }

                if (p.getY() < -MARGEM) {
                    if (p.contaFuga() && p.getTipo() != TipoPato.FALSO_RARO) {
                        patosFugidos++;
                        dog.rir(p.getX());
                    }
                    it.remove();
                    continue;
                }
            }
        }

        if (municao == 0 && animais.isEmpty()) {
            recarregarMunicao();
        }

        textos.removeIf(t -> { t.atualizar(); return t.terminou(); });

        long agora = System.currentTimeMillis();
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
            if (gameOverImg != null) {
                // Desenha a imagem de Game Over centralizada
                int gx = (LARGURA - gameOverImg.getWidth(null)) / 2;
                int gy = (ALTURA - gameOverImg.getHeight(null)) / 2 - 50;
                g.drawImage(gameOverImg, gx, gy, null);

                // Configura fonte para mensagens finais
                g.setFont(new Font("Consolas", Font.BOLD, 28));
                FontMetrics fm = g.getFontMetrics();

                // Mensagem de Parabéns e Pontuação
                String parabens = "PARABÉNS!";
                String pontosStr = "PONTUAÇÃO FINAL: " + pontuacao;
                String resetStr = "Pressione [R] para reiniciar";

                g.setColor(Color.WHITE);
                g.drawString(parabens, (LARGURA - fm.stringWidth(parabens)) / 2, gy + gameOverImg.getHeight(null) + 40);

                g.setColor(Color.WHITE);
                g.drawString(pontosStr, (LARGURA - fm.stringWidth(pontosStr)) / 2, gy + gameOverImg.getHeight(null) + 80);

                g.setFont(new Font("Consolas", Font.ITALIC, 18));
                g.drawString(resetStr, (LARGURA - g.getFontMetrics().stringWidth(resetStr)) / 2, gy + gameOverImg.getHeight(null) + 120);
            }
        } else {
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