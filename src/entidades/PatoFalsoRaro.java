package entidades;
import java.awt.Color;

public class PatoFalsoRaro extends Pato {
    public PatoFalsoRaro(int x, int y, boolean dir) { super(x, y, dir); this.arisco = true; }
    @Override protected String getPrefixoImagem() { return "duckFalse"; }
    @Override public boolean contaFuga() { return false; }
    @Override public void aoSerAtingido(jogo.GamePanel gp) {
        gp.penalizarFuga(4);
        gp.adicionarTextoFlutuante(x, y, "-4", Color.RED);
        this.morrer();
    }
    @Override public void reagirAoCair(Cachorro dog) { dog.rir(this.x); }
    @Override public void reagirAoFugir(Cachorro dog) { /* Fuga silenciosa */ }
}