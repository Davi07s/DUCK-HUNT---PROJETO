package jogo;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class BarraTolerancia {

    private int x, y;
    private int largura, altura;

    private int valorAtual;   // valor real
    private float valorVisual; // valor suavizado
    private int valorMaximo;

    private final float VELOCIDADE_SUAVIZACAO = 0.08f;

    public BarraTolerancia(int x, int y, int largura, int altura, int valorMaximo) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.valorMaximo = valorMaximo;
        this.valorAtual = 0;
        this.valorVisual = 0;
    }

    public void setValor(int valor) {
        this.valorAtual = Math.min(valor, valorMaximo);
    }

    public void atualizar() {
        valorVisual += (valorAtual - valorVisual) * VELOCIDADE_SUAVIZACAO;
    }

    public void desenhar(Graphics g) {

        Graphics2D g2 = (Graphics2D) g;

        g2.setComposite(AlphaComposite.getInstance(
                AlphaComposite.SRC_OVER, 0.65f
        ));

        // fundo
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(x, y, largura, altura);

        double proporcao = valorVisual / valorMaximo;
        int larguraAtual = (int) (largura * proporcao);

        if (proporcao < 0.4) {
            g2.setColor(new Color(0, 180, 0));
        } else if (proporcao < 0.7) {
            g2.setColor(new Color(230, 160, 0));
        } else {
            g2.setColor(new Color(200, 0, 0));
        }

        g2.fillRect(x, y, larguraAtual, altura);

        g2.setComposite(AlphaComposite.SrcOver);
        g2.setColor(Color.BLACK);
        g2.drawRect(x, y, largura, altura);
    }
}
