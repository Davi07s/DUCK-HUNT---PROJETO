package jogo;

import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;

public class BarraTolerancia {
    private int x, y, largura, altura, valorMaximo;
    private int valorAtual;
    private float valorVisual;
    private final float VELOCIDADE_SUAVIZACAO = 0.12f;

    public BarraTolerancia(int x, int y, int largura, int altura, int valorMaximo) {
        this.x = x;
        this.y = y;
        this.largura = largura;
        this.altura = altura;
        this.valorMaximo = valorMaximo;
    }

    public void setValor(int valor) { this.valorAtual = Math.min(valor, valorMaximo); }

    public void atualizar() {
        valorVisual += (valorAtual - valorVisual) * VELOCIDADE_SUAVIZACAO;
        if (Math.abs(valorAtual - valorVisual) < 0.01f) valorVisual = valorAtual;
    }

    public void desenhar(Graphics g) {
        Graphics2D g2 = (Graphics2D) g;
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, 0.65f));
        g2.setColor(Color.DARK_GRAY);
        g2.fillRect(x, y, largura, altura);


        int larguraAtual = (valorAtual >= valorMaximo) ? largura : (int)(largura * (valorVisual / valorMaximo));

        double prop = (double)valorVisual / valorMaximo;
        if (prop < 0.4) g2.setColor(new Color(0, 180, 0));
        else if (prop < 0.7) g2.setColor(new Color(230, 160, 0));
        else g2.setColor(new Color(200, 0, 0));

        if (larguraAtual > 0) g2.fillRect(x, y, larguraAtual, altura);

        g2.setComposite(AlphaComposite.SrcOver);
        g2.setColor(Color.BLACK);
        g2.drawRect(x, y, largura, altura);
    }
}