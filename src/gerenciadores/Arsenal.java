package gerenciadores;

public class Arsenal {
    private int atual;
    private int reserva;
    private final int MAX_PENTE = 5;
    private final int MAX_RESERVA = 40;

    private boolean recarregando = false;
    private long inicioRecarga = 0;
    private final int TEMPO_RECARGA = 1500;

    private long ultimoEmergencia = 0;
    private final int DELAY_EMERGENCIA = 3000;

    public Arsenal(int inicial, int reservaInicial) {
        this.atual = inicial;
        this.reserva = reservaInicial;
    }

    public void atualizar() {
        long agora = System.currentTimeMillis();

        if (recarregando && agora - inicioRecarga > TEMPO_RECARGA) {
            int falta = MAX_PENTE - atual;
            int aRecarregar = Math.min(reserva, falta);
            atual += aRecarregar;
            reserva -= aRecarregar;
            recarregando = false;
        }

        if (reserva == 0 && atual < MAX_PENTE && !recarregando) {
            if (ultimoEmergencia == 0) ultimoEmergencia = agora;
            if (agora - ultimoEmergencia > DELAY_EMERGENCIA) {
                reserva = 2;
                ultimoEmergencia = agora;
            }
        } else {
            ultimoEmergencia = agora;
        }
    }

    public boolean podeAtirar() { return atual > 0 && !recarregando; }
    public void gastar() { if (atual > 0) atual--; }
    public void recarregar() {
        if (!recarregando && atual < MAX_PENTE && reserva > 0) {
            recarregando = true;
            inicioRecarga = System.currentTimeMillis();
        }
    }

    public void adicionarReserva(int qtd) { reserva = Math.min(MAX_RESERVA, reserva + qtd); }
    public int getAtual() { return atual; }
    public int getReserva() { return reserva; }
    public boolean estaRecarregando() { return recarregando; }
    public int getMaxPente() { return MAX_PENTE; }
    public long getTempoEmergencia() {
        return Math.max(0, (DELAY_EMERGENCIA - (System.currentTimeMillis() - ultimoEmergencia)) / 1000 + 1);
    }
}