package entidades;

import java.awt.Color;

public class PatoRaro extends Pato {
    public PatoRaro(int x, int y, boolean dir) {
        super(x, y, dir);
        this.arisco = true;
    }

    @Override
    protected String getPrefixoImagem() { return "duckGolden"; }

    @Override
    public boolean contaFuga() { return true; }

    @Override
    public void aoSerAtingido(jogo.GamePanel gp) {
        gp.adicionarPontuacao(4);
        gp.reduzirFugas(4);
        gp.adicionarTextoFlutuante(x, y, "+4 pts", new Color(255, 215, 0));
        this.morrer();
    }
}