package gerenciadores;

import jogo.GamePanel;
import java.awt.event.*;

public class InputHandler extends MouseAdapter implements KeyListener {
    private GamePanel painel;
    private Arsenal arsenal;
    private Runnable onVoltarMenu;

    public InputHandler(GamePanel painel, Arsenal arsenal, Runnable onVoltarMenu) {
        this.painel = painel;
        this.arsenal = arsenal;
        this.onVoltarMenu = onVoltarMenu;
    }

    public void setArsenal(Arsenal arsenal) { this.arsenal = arsenal; }

    @Override
    public void mousePressed(MouseEvent e) {
        painel.tentarDisparar(e.getPoint());
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        painel.atualizarMouse(e.getX(), e.getY());
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (!painel.isGameOver() && e.getKeyCode() == KeyEvent.VK_R) {
            arsenal.recarregar();
        }
        if (painel.isGameOver()) {
            if (e.getKeyCode() == KeyEvent.VK_R || e.getKeyCode() == KeyEvent.VK_ENTER) {
                painel.reiniciarJogo();
            }
            if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                onVoltarMenu.run();
            }
        }
    }

    @Override public void keyTyped(KeyEvent e) {}
    @Override public void keyReleased(KeyEvent e) {}
}