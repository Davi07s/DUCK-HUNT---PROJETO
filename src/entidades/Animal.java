package entidades;

import java.awt.Graphics;
import java.awt.Rectangle;

public abstract class Animal {

    protected int x;
    protected int y;
    protected int largura = 60;
    protected int altura = 40;

    protected int vx;
    protected int vy;

    // Controla a intensidade do movimento senoidal
    protected double intensidadeZigZag = 0;

    public Animal(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void atualizar() {
        x += vx;
        y += vy;
    }

    public Rectangle getBounds() {
        return new Rectangle(x, y, largura, altura);
    }

    public abstract void desenhar(Graphics g);



    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public void setVelocidadeX(int vx) {
        this.vx = vx;
    }

    public void setVelocidadeY(int vy) {
        this.vy = vy;
    }

    /**
     * Define a intensidade do zigue-zague (oscilação vertical).
     * @param intensidade Valor que aumenta a cada round no GamePanel.
     */
    public void setDificuldade(double intensidade) {
        this.intensidadeZigZag = intensidade;
    }
}