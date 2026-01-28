package jogo;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

public class TextoFlutuante {

    private int x;
    private int y;
    private String texto;
    private Color cor;

    private int tempoVida = 60; // frames
    private int deslocamentoY = 0;

    public TextoFlutuante(int x, int y, String texto, Color cor) {
        this.x = x;
        this.y = y;
        this.texto = texto;
        this.cor = cor;
    }

    public void atualizar() {
        deslocamentoY--;
        tempoVida--;
    }

    public boolean terminou() {
        return tempoVida <= 0;
    }

    public void desenhar(Graphics g) {
        g.setColor(cor);

        // Fonte
        g.setFont(new Font("Monospaced", Font.BOLD, 18));

        g.drawString(texto, x, y + deslocamentoY);
    }
}
