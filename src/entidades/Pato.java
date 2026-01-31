package entidades;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import javax.imageio.ImageIO;

public class Pato extends Animal {

    public enum TipoPato { NORMAL, INOCENTE, RARO, FALSO_RARO }

    private TipoPato tipo;
    private enum Estado { VOANDO, MORTO, CAINDO }
    private Estado estado = Estado.VOANDO;

    private Image[] voo;
    private Image[] queda;
    private int frameAtual = 0;
    private int contadorFrames = 0;
    private int frameQueda = 0;
    private int contadorQueda = 0;

    private final int TROCA_FRAME = 10;
    private final int TROCA_FRAME_QUEDA = 6;
    private final int GRAVIDADE = 1;

    private double angulo = 0;
    private double velocidadeAngular;
    private int yBase;
    private int amplitude;

    private boolean arisco;
    private final int RAIO_FUGA = 120;
    private final int LIMITE_SUPERIOR = -100;
    private final int LIMITE_INFERIOR = 340;

    private boolean indoParaDireita;
    private boolean entrouNaTela = false;

    // Controle de Saída para permitir renovação de patos
    private int colisoesBorda = 0;
    private final int MAX_COLISOES = 4;
    private boolean fugindoParaSempre = false;

    public Pato(int x, int y, boolean vemDaEsquerda, TipoPato tipo) {
        super(x, y);
        this.tipo = tipo;
        this.yBase = y;
        this.amplitude = 20 + (int) (Math.random() * 20);
        this.velocidadeAngular = 0.05 + Math.random() * 0.05;
        this.arisco = (tipo == TipoPato.RARO || tipo == TipoPato.FALSO_RARO) || Math.random() < 0.3;
        this.indoParaDireita = vemDaEsquerda;
        carregarImagens(vemDaEsquerda);
    }

    private void carregarImagens(boolean vemDaEsquerda) {
        try {
            voo = new Image[2];
            queda = new Image[4];
            String prefixo = switch (tipo) {
                case INOCENTE -> "duckInocent";
                case RARO -> "duckGolden";
                case FALSO_RARO -> "duckFalse";
                default -> "duck";
            };
            String dir = vemDaEsquerda ? "Right" : "Left";
            voo[0] = ImageIO.read(getClass().getResource("/imagens/" + prefixo + dir + "0.png"));
            voo[1] = ImageIO.read(getClass().getResource("/imagens/" + prefixo + dir + "1.png"));
            for (int i = 0; i < 4; i++) {
                queda[i] = ImageIO.read(getClass().getResource("/imagens/duckPrecipitate" + i + ".png"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void inverterDirecao() {
        if (fugindoParaSempre) return;

        colisoesBorda++;
        // Se atingir o limite de quiques ou for inocente, ele começa a subir para sair
        if (colisoesBorda >= MAX_COLISOES || tipo == TipoPato.INOCENTE) {
            fugindoParaSempre = true;
            return;
        }

        vx *= -1; // Inverte a velocidade horizontal
        indoParaDireita = vx > 0;


        // garante que fique na esquerda ou fique na direita.
        if (indoParaDireita) {
            if (x < 0) x = 2;
        } else {
            if (x + largura > 800) x = 800 - largura - 2;
        }

        carregarImagens(indoParaDireita);
    }

    @Override
    public void atualizar() {
        switch (estado) {
            case VOANDO -> {
                x += vx;

                if (fugindoParaSempre) {
                    yBase -= 4; // pato sai da tela por cima
                }

                if (!entrouNaTela && x > 0 && x + largura < 800) entrouNaTela = true;

                // Rebote lateral
                if (entrouNaTela && (x < 0 || x + largura > 800)) {
                    inverterDirecao();
                }

                // Zigue-zague
                double amplitudeAtual = amplitude + (intensidadeZigZag * 5);
                double freqAngular = velocidadeAngular + (intensidadeZigZag * 0.01);
                angulo += freqAngular;
                y = yBase + (int) (Math.sin(angulo) * amplitudeAtual);

                // Travas verticais - garante que o pato não fique travado na borda da tela
                if (!fugindoParaSempre) {
                    if (y < 20) y = 20;
                    if (y > LIMITE_INFERIOR) y = LIMITE_INFERIOR;
                }

                animarVoo();
            }
            case MORTO -> { estado = Estado.CAINDO; vy = 2; }
            case CAINDO -> { y += vy; vy += GRAVIDADE; animarQueda(); }
        }
    }

    private void animarVoo() {
        contadorFrames++;
        if (contadorFrames >= TROCA_FRAME) {
            frameAtual = (frameAtual + 1) % voo.length;
            contadorFrames = 0;
        }
    }

    private void animarQueda() {
        contadorQueda++;
        if (contadorQueda >= TROCA_FRAME_QUEDA) {
            frameQueda = Math.min(frameQueda + 1, queda.length - 1);
            contadorQueda = 0;
        }
    }

    public void morrer() {
        if (estado == Estado.VOANDO) { estado = Estado.MORTO; vx = 0; }
    }

    public void fugirDoCursor(int mX, int mY) {
        if (!arisco || estado != Estado.VOANDO || fugindoParaSempre) return;
        double dist = Math.sqrt(Math.pow((x + largura/2) - mX, 2) + Math.pow((y + altura/2) - mY, 2));
        if (dist < RAIO_FUGA) {
            // Desvio do mouse
            if ((mX < x && vx < 0) || (mX > x && vx > 0)) inverterDirecao();
            yBase -= 2;
        }
    }

    public boolean contaFuga() { return tipo != TipoPato.INOCENTE; }
    public boolean estaMorto() { return estado != Estado.VOANDO; }
    public boolean queda(int ySolo) { return y + altura >= ySolo; }
    public TipoPato getTipo() { return tipo; }
    public int getY() { return y; }
    public Rectangle getBounds() { return new Rectangle(x, y, largura, altura); }

    @Override
    public void desenhar(Graphics g) {
        if (voo == null || queda == null) return;
        Image img = (estado == Estado.VOANDO) ? voo[frameAtual] : queda[frameQueda];
        if (img != null) g.drawImage(img, x, y, largura, altura, null);
    }
}