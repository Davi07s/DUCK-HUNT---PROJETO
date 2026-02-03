package jogo;

import entidades.*;
import ranking.GerenciadorRanking;
import gerenciadores.*;

import javax.swing.JPanel;
import javax.swing.Timer;
import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class GamePanel extends JPanel {

    public static final int LARGURA = 800;
    public static final int ALTURA = 600;
    private final int ALTURA_SOLO = 380;
    private final int Y_MASCARA_GRAMA = 445;
    private final int MARGEM = 60;
    private final int LIMITE_FUGAS = 20;
    private final int PONTOS_POR_ROUND = 10;
    private final int TEMPO_MSG_ROUND = 2000;

    // Gerenciadores
    private Arsenal arsenal;
    private DiretorJogo diretor;
    private PatoFactory patoFactory;
    private InputHandler inputHandler;

    private List<Animal> animais = new ArrayList<>();
    private List<TextoFlutuante> textos = new ArrayList<>();
    private Timer timer;

    private int pontuacao = 0;
    private int patosFugidos = 0;
    private boolean gameOver = false;
    private boolean exibindoRound = false;
    private long tempoInicioRoundMsg = 0;

    private Image background, mira, gameOverImg, bulletImg;
    private int mouseX, mouseY;
    private BarraTolerancia barraTolerancia;
    private entidades.Cachorro dog;
    private Runnable onVoltarMenu;

    public GamePanel(Runnable onVoltarMenu) {
        this.onVoltarMenu = onVoltarMenu;

        setPreferredSize(new Dimension(LARGURA, ALTURA));
        setFocusable(true);
        requestFocusInWindow();

        // Inicialização dos Gerenciadores
        this.arsenal = new Arsenal(5, 15);
        this.diretor = new DiretorJogo();
        this.patoFactory = new PatoFactory();
        this.inputHandler = new InputHandler(this, arsenal, onVoltarMenu);

        // Registro dos Inputs
        addMouseListener(inputHandler);
        addMouseMotionListener(inputHandler);
        addKeyListener(inputHandler);

        carregarImagens();
        esconderCursor();
        carregarSons();

        barraTolerancia = new BarraTolerancia(10, 50, 200, 12, LIMITE_FUGAS);
        dog = new entidades.Cachorro(-100, 380);
        dog.iniciarIntroducao();

        Musica.tocarLoop("intro");

        timer = new Timer(16, e -> {
            atualizar();
            repaint();
        });
        timer.start();
    }


    public void tentarDisparar(Point ponto) {
        if (gameOver || (!dog.terminouIntroducao() && diretor.getRoundAtual() == 1)) return;
        if (arsenal.podeAtirar()) processarDisparo(ponto);
    }

    public void atualizarMouse(int x, int y) {
        this.mouseX = x;
        this.mouseY = y;
    }

    public boolean isGameOver() { return gameOver; }


    private void processarDisparo(Point pontoClique) {
        arsenal.gastar();
        Musica.tocar("shoot");
        for (Animal a : animais) {
            if (a instanceof Pato p && !p.estaMorto() && p.getBounds().contains(pontoClique)) {
                p.aoSerAtingido(this);
                arsenal.adicionarReserva(1);
                Musica.tocar("duckDead");
                verificarProgresso();
                break;
            }
        }
    }

    private void verificarProgresso() {
        int novoRound = (pontuacao / PONTOS_POR_ROUND) + 1;
        if (novoRound > diretor.getRoundAtual()) {
            diretor.subirRound();
            exibindoRound = true;
            tempoInicioRoundMsg = System.currentTimeMillis();
            arsenal.adicionarReserva(10);
            adicionarTextoFlutuante(LARGURA/2, 100, "ROUND UP!", Color.WHITE);
        }
    }

    private void atualizar() {
        long agora = System.currentTimeMillis();
        arsenal.atualizar();
        dog.atualizar();

        if (!dog.terminouIntroducao() && diretor.getRoundAtual() == 1 && pontuacao == 0) return;

        // Atualização de spawm
        Iterator<Animal> it = animais.iterator();
        while (it.hasNext()) {
            Animal a = it.next();
            a.atualizar();
            if (a instanceof Pato p) {
                p.fugirDoCursor(mouseX, mouseY);
                if (p.estaMorto() && p.queda(ALTURA_SOLO)) {
                    p.reagirAoCair(dog);
                    it.remove();
                } else if (p.getY() < -MARGEM) {
                    if (p.contaFuga()) patosFugidos++;
                    p.reagirAoFugir(dog);
                    it.remove();
                }
            }
        }

        // Spawn Gerenciado pelo Diretor e Factory
        if (!gameOver && diretor.deveSpawnar(animais.size(), agora)) {
            int qtd = diretor.sortearQuantidade();
            for (int i = 0; i < qtd; i++) {
                boolean dir = new java.util.Random().nextBoolean();
                int x = dir ? -MARGEM : LARGURA + MARGEM;
                int y = new java.util.Random().nextInt(ALTURA_SOLO - 120) + 20;

                Pato p = patoFactory.gerarPato(x, y, dir, diretor.getIntensidadeZigZag());
                int vx = dir ? diretor.getVelocidadeBase() + 2 : -(diretor.getVelocidadeBase() + 2);
                p.setVelocidadeX(vx);
                animais.add(p);
            }
            diretor.registrarSpawn(agora);
        }

        // Recarga Automática
        if (arsenal.getAtual() == 0 && animais.isEmpty() && !arsenal.estaRecarregando() && arsenal.getReserva() > 0) {
            arsenal.recarregar();
        }

        textos.removeIf(t -> { t.atualizar(); return t.terminou(); });
        barraTolerancia.setValor(patosFugidos);
        barraTolerancia.atualizar();

        if (exibindoRound && agora - tempoInicioRoundMsg > TEMPO_MSG_ROUND) exibindoRound = false;
        if (patosFugidos >= LIMITE_FUGAS) finalizarJogo();
    }

    public void reiniciarJogo() {
        pontuacao = 0;
        patosFugidos = 0;
        gameOver = false;
        exibindoRound = false;
        diretor.resetar();
        arsenal = new Arsenal(5, 15);
        inputHandler.setArsenal(arsenal);
        animais.clear();
        textos.clear();
        barraTolerancia.setValor(0);
        dog = new entidades.Cachorro(-100, 380);
        dog.iniciarIntroducao();
        Musica.pararMusicaFundo();
        Musica.tocarLoop("intro");
        timer.start();
    }

    // --- MÉTODOS DE INTERFACE (CHAMADOS PELOS PATOS) ---
    public void adicionarPontuacao(int pts) { this.pontuacao += pts; }
    public void reduzirFugas(int qtd) { this.patosFugidos = Math.max(0, this.patosFugidos - qtd); }
    public void penalizarFuga(int qtd) { this.patosFugidos += qtd; }
    public void adicionarTextoFlutuante(int x, int y, String t, Color c) { textos.add(new TextoFlutuante(x, y, t, c)); }

    public void finalizarJogo() {
        gameOver = true;
        Musica.pararMusicaFundo();
        Musica.tocar("gameClear");
        GerenciadorRanking.salvarRecorde(this.pontuacao);
        timer.stop();
        repaint();
    }

    // --- RENDERIZAÇÃO E ASSETS ---
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
        setCursor(Toolkit.getDefaultToolkit().createCustomCursor(
                new java.awt.image.BufferedImage(16, 16, java.awt.image.BufferedImage.TYPE_INT_ARGB),
                new Point(0, 0), "invisible"));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, LARGURA, ALTURA, null);
        dog.desenhar(g);

        if (dog.terminouIntroducao()) {
            g.drawImage(background, 0, Y_MASCARA_GRAMA, LARGURA, ALTURA, 0, Y_MASCARA_GRAMA, LARGURA, ALTURA, null);
        }

        for (Animal a : animais) a.desenhar(g);
        for (TextoFlutuante t : textos) t.desenhar(g);

        desenharHUD(g);
        if (gameOver) desenharGameOver(g);
        else if (!arsenal.estaRecarregando()) {
            g.drawImage(mira, mouseX - mira.getWidth(null) / 2, mouseY - mira.getHeight(null) / 2, null);
        }
    }

    private void desenharHUD(Graphics g) {
        g.setFont(new Font("Arial", Font.BOLD, 14));
        g.setColor(Color.WHITE);
        g.drawString("Pontuação: " + pontuacao, 10, 20);
        g.drawString("Round: " + diretor.getRoundAtual(), 10, 40);
        g.drawString("Reservas: " + arsenal.getReserva(), 10, 75);

        if (arsenal.estaRecarregando()) g.drawString("Recarregando...", 10, 95);
        else if (arsenal.getReserva() == 0 && arsenal.getAtual() < arsenal.getMaxPente()) {
            g.drawString("Emergência: " + arsenal.getTempoEmergencia() + "s", 10, 95);
        }

        barraTolerancia.desenhar(g);
        if (bulletImg != null) {
            for (int i = 0; i < arsenal.getAtual(); i++) {
                g.drawImage(bulletImg, 180 + (i * 25), 10, 20, 30, null);
            }
        }

        if (exibindoRound) {
            String txt = "ROUND " + diretor.getRoundAtual();
            g.setFont(new Font("Arial", Font.BOLD, 52));
            g.setColor(Color.WHITE);
            FontMetrics fm = g.getFontMetrics();
            g.drawString(txt, (LARGURA - fm.stringWidth(txt)) / 2, ALTURA / 2 + fm.getAscent() / 2);
        }
    }

    private void desenharGameOver(Graphics g) {
        g.setColor(new Color(0, 0, 0, 180));
        g.fillRect(0, 0, LARGURA, ALTURA);
        if (gameOverImg != null) {
            int gx = (LARGURA - gameOverImg.getWidth(null)) / 2;
            int gy = (ALTURA - gameOverImg.getHeight(null)) / 2 - 80;
            g.drawImage(gameOverImg, gx, gy, null);
            g.setColor(Color.WHITE);
            g.setFont(new Font("Consolas", Font.BOLD, 24));
            String pontosStr = "PONTUAÇÃO FINAL: " + pontuacao;
            g.drawString(pontosStr, (LARGURA - g.getFontMetrics().stringWidth(pontosStr)) / 2, gy + gameOverImg.getHeight(null) + 90);
        }
    }
}