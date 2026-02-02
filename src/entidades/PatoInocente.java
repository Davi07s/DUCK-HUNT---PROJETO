package entidades;

public class PatoInocente extends Pato {
    public PatoInocente(int x, int y, boolean dir) {
        super(x, y, dir);
    }

    @Override
    protected String getPrefixoImagem() { return "duckInocent"; }

    @Override
    public boolean contaFuga() { return false; }

    @Override
    public void aoSerAtingido(jogo.GamePanel gp) {
        gp.finalizarJogo();
    }
}