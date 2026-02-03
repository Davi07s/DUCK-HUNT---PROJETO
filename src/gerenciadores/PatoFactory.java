package gerenciadores;

import entidades.*;
import java.util.Random;

public class PatoFactory {
    private Random random = new Random();

    public Pato gerarPato(int x, int y, boolean dir, double dificuldade) {
        Pato p = sortearInstancia(x, y, dir);
        p.setDificuldade(dificuldade);
        return p;
    }

    private Pato sortearInstancia(int x, int y, boolean dir) {
        int r = random.nextInt(100);
        if (r < 15) return new PatoRaro(x, y, dir);
        if (r < 25) return new PatoFalsoRaro(x, y, dir);
        if (r < 40) return new PatoInocente(x, y, dir);
        return new PatoComum(x, y, dir);
    }
}