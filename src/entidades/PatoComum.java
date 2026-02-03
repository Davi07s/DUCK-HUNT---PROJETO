package entidades;
import java.awt.Color;

public class PatoComum extends Pato {
    public PatoComum(int x, int y, boolean dir) { super(x, y, dir); this.arisco = Math.random() < 0.3; }
    @Override protected String getPrefixoImagem() { return "duck"; }
    @Override public boolean contaFuga() { return true; } // Incrementa na barra de tolerância
    @Override public void aoSerAtingido(jogo.GamePanel gp) {
        gp.adicionarPontuacao(1);
        gp.adicionarTextoFlutuante(x, y, "+1", Color.WHITE);
        this.morrer();
    }
    @Override public void reagirAoCair(Cachorro dog) { dog.celebrar(this.x); } //animação do cachorro
    @Override public void reagirAoFugir(Cachorro dog) { dog.rir(this.x); }
}