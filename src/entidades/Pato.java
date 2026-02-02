package entidades;

import java.awt.*;
import javax.imageio.ImageIO;

public abstract class Pato extends Animal {
    protected enum Estado { VOANDO, MORTO, CAINDO }
    protected Estado estado = Estado.VOANDO;

    protected Image[] voo, queda;
    protected int frameAtual = 0, contadorFrames = 0, frameQueda = 0, contadorQueda = 0;
    protected final int TROCA_FRAME = 10, TROCA_FRAME_QUEDA = 6, GRAVIDADE = 1;

    protected double angulo = 0, velocidadeAngular;
    protected int yBase, amplitude;
    protected boolean arisco, indoParaDireita, entrouNaTela = false;
    protected int colisoesBorda = 0;
    protected final int MAX_COLISOES = 4;
    protected boolean fugindoParaSempre = false;
    protected final int LIMITE_INFERIOR = 340;

    public Pato(int x, int y, boolean vemDaEsquerda) {
        super(x, y);
        this.yBase = y;
        this.indoParaDireita = vemDaEsquerda;
        this.amplitude = 20 + (int) (Math.random() * 20);
        this.velocidadeAngular = 0.05 + Math.random() * 0.05;
        carregarImagens(vemDaEsquerda);
    }

    // Métodos que as subclasses devem implementar
    public abstract void aoSerAtingido(jogo.GamePanel gp);
    public abstract boolean contaFuga();
    protected abstract String getPrefixoImagem();

    protected void carregarImagens(boolean vemDaEsquerda) {
        try {
            voo = new Image[2];
            queda = new Image[4];
            String dir = vemDaEsquerda ? "Right" : "Left";
            voo[0] = ImageIO.read(getClass().getResource("/imagens/" + getPrefixoImagem() + dir + "0.png"));
            voo[1] = ImageIO.read(getClass().getResource("/imagens/" + getPrefixoImagem() + dir + "1.png"));
            for (int i = 0; i < 4; i++) {
                queda[i] = ImageIO.read(getClass().getResource("/imagens/duckPrecipitate" + i + ".png"));
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @Override
    public void atualizar() {
        switch (estado) {
            case VOANDO -> {
                x += vx;
                if (fugindoParaSempre) yBase -= 4;
                if (!entrouNaTela && x > 0 && x + largura < 800) entrouNaTela = true;
                if (entrouNaTela && (x < 0 || x + largura > 800)) inverterDirecao();

                double amplitudeAtual = amplitude + (intensidadeZigZag * 5);
                angulo += velocidadeAngular + (intensidadeZigZag * 0.01);
                y = yBase + (int) (Math.sin(angulo) * amplitudeAtual);

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

    private void inverterDirecao() {
        if (fugindoParaSempre) return;
        colisoesBorda++;
        if (colisoesBorda >= MAX_COLISOES) { fugindoParaSempre = true; return; }
        vx *= -1;
        indoParaDireita = vx > 0;
        carregarImagens(indoParaDireita);
    }

    public void fugirDoCursor(int mX, int mY) {
        if (!arisco || estado != Estado.VOANDO || fugindoParaSempre) return;
        double dist = Math.sqrt(Math.pow((x + largura/2) - mX, 2) + Math.pow((y + altura/2) - mY, 2));
        if (dist < 120) {
            if ((mX < x && vx < 0) || (mX > x && vx > 0)) inverterDirecao();
            yBase -= 2;
        }
    }

    private void animarVoo() {
        if (++contadorFrames >= TROCA_FRAME) { frameAtual = (frameAtual + 1) % voo.length; contadorFrames = 0; }
    }

    private void animarQueda() {
        if (++contadorQueda >= TROCA_FRAME_QUEDA) { frameQueda = Math.min(frameQueda + 1, queda.length - 1); contadorQueda = 0; }
    }

    public void morrer() { if (estado == Estado.VOANDO) { estado = Estado.MORTO; vx = 0; } }
    public boolean estaMorto() { return estado != Estado.VOANDO; }
    public boolean queda(int ySolo) { return y + altura >= ySolo; }

    @Override
    public void desenhar(Graphics g) {
        Image img = (estado == Estado.VOANDO) ? voo[frameAtual] : queda[frameQueda];
        if (img != null) g.drawImage(img, x, y, largura, altura, null);
    }
}