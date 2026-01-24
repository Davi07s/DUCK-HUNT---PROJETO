package entidades;

import java.awt.*;

public abstract class Entidade {

    protected int x, y;
    protected int largura, altura;
    protected int velocidadeX, velocidadeY;
    protected boolean viva = true;

    public abstract void atualizar();
    public abstract void desenhar(Graphics g);

    public Rectangle getBounds() {
        return new Rectangle(x, y, largura, altura);
    }

    public boolean isViva() {
        return viva;
    }

    public int getX() {
        return x;
    }
}
