package entidades;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.Rectangle;
import javax.imageio.ImageIO;

public class Pato extends Animal {

    //  TIPOS DE PATO
    public enum TipoPato {
        NORMAL,
        INOCENTE,
        RARO,
        FALSO_RARO
    }

    private TipoPato tipo;

    private enum Estado {
        VOANDO, MORTO, CAINDO
    }

    private Estado estado = Estado.VOANDO;

    private Image[] voo;
    private Image[] queda;
    private Image morto;

    private int frameAtual = 0;
    private int contadorFrames = 0;

    private int frameQueda = 0;
    private int contadorQueda = 0;

    private final int TROCA_FRAME = 10;
    private final int TROCA_FRAME_QUEDA = 6;
    private final int GRAVIDADE = 1;

    // movimento
    private double angulo = 0;
    private double velocidadeAngular;
    private int yBase;
    private int amplitude;

    // comportamento
    private boolean arisco;
    private final int RAIO_FUGA = 120;
    private final int LIMITE_SUPERIOR = 20;
    private final int LIMITE_INFERIOR = 340;

    private boolean indoParaDireita;
    private boolean deveSairDireita;
    private boolean entrouNaTela = false;

    public Pato(int x, int y, boolean vemDaEsquerda, TipoPato tipo) {
        super(x, y);

        this.tipo = tipo;

        this.yBase = y;
        this.amplitude = 20 + (int) (Math.random() * 20);
        this.velocidadeAngular = 0.05 + Math.random() * 0.05;

        // raros são mais ariscos
        this.arisco = (tipo == TipoPato.RARO || tipo == TipoPato.FALSO_RARO)
                || Math.random() < 0.3;

        this.indoParaDireita = vemDaEsquerda;
        this.deveSairDireita = vemDaEsquerda;

        carregarImagens(vemDaEsquerda);
    }

    //  define os danos da fuga
    public boolean contaFuga() {
        return tipo != TipoPato.INOCENTE;
    }

    public TipoPato getTipo() {
        return tipo;
    }

    private void carregarImagens(boolean vemDaEsquerda) {
        try {
            voo = new Image[2];
            queda = new Image[4];

            String prefixo;

            switch (tipo) {
                case INOCENTE -> prefixo = "duckInocent";
                case RARO -> prefixo = "duckGolden";
                case FALSO_RARO -> prefixo = "duckFalse";
                default -> prefixo = "duck";
            }

            if (vemDaEsquerda) {
                voo[0] = ImageIO.read(getClass().getResource("/imagens/" + prefixo + "Right0.png"));
                voo[1] = ImageIO.read(getClass().getResource("/imagens/" + prefixo + "Right1.png"));
            } else {
                voo[0] = ImageIO.read(getClass().getResource("/imagens/" + prefixo + "Left0.png"));
                voo[1] = ImageIO.read(getClass().getResource("/imagens/" + prefixo + "Left1.png"));
            }

            queda[0] = ImageIO.read(getClass().getResource("/imagens/duckPrecipitate0.png"));
            queda[1] = ImageIO.read(getClass().getResource("/imagens/duckPrecipitate1.png"));
            queda[2] = ImageIO.read(getClass().getResource("/imagens/duckPrecipitate2.png"));
            queda[3] = ImageIO.read(getClass().getResource("/imagens/duckPrecipitate3.png"));

            morto = queda[0];

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void morrer() {
        if (estado != Estado.VOANDO) return;

        estado = Estado.MORTO;
        vx = 0;
        vy = 0;

        frameQueda = 0;
        contadorQueda = 0;
    }

    public void fugirDoCursor(int mouseX, int mouseY) {
        if (!arisco || estado != Estado.VOANDO) return;

        int centroX = x + largura / 2;
        int centroY = y + altura / 2;

        double dx = centroX - mouseX;
        double dy = centroY - mouseY;

        double distancia = Math.sqrt(dx * dx + dy * dy);

        if (distancia < RAIO_FUGA) {

            if (dx < 0 && vx > 0 || dx > 0 && vx < 0) {
                inverterDirecao();
            }

            if (dy > 0) yBase += 2;
            else yBase -= 2;

            if (yBase < LIMITE_SUPERIOR) yBase = LIMITE_SUPERIOR;
            if (yBase + altura > LIMITE_INFERIOR)
                yBase = LIMITE_INFERIOR - altura;

            angulo += 0.15;
        }
    }

    private void inverterDirecao() {
        vx *= -1;
        indoParaDireita = vx > 0;
        carregarImagens(indoParaDireita);
    }

    @Override
    public void atualizar() {

        switch (estado) {
            case VOANDO -> {
                x += vx;

                if (x > 0 && x + largura < 800) entrouNaTela = true;

                if (entrouNaTela) {
                    if (x < 0 && deveSairDireita) inverterDirecao();
                    if (x + largura > 800 && !deveSairDireita) inverterDirecao();
                }

                angulo += velocidadeAngular;
                y = yBase + (int) (Math.sin(angulo) * amplitude);

                animarVoo();
            }

            case MORTO -> {
                estado = Estado.CAINDO;
                vy = 2;
            }

            case CAINDO -> {
                y += vy;
                vy += GRAVIDADE;
                animarQueda();
            }
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

    public boolean queda(int yChao) {
        return y + altura >= yChao;
    }

    public boolean estaMorto() {
        return estado != Estado.VOANDO;
    }

    @Override
    public void desenhar(Graphics g) {
        if (estado == Estado.VOANDO) {
            g.drawImage(voo[frameAtual], x, y, largura, altura, null);
        } else {
            g.drawImage(queda[frameQueda], x, y, largura, altura, null);
        }
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, largura, altura);
    }
}
