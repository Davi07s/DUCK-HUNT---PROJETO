package gerenciadores;

import java.util.Random;

public class DiretorJogo {
    private int roundAtual = 1;
    private int velocidadeBase = 4;
    private double intensidadeZigZag = 0;
    private int delaySpawnAtual = 600;
    private int patosMinimos = 4;
    private long ultimoSpawn = 0;
    private Random random = new Random();

    public boolean deveSpawnar(int quantidadeAtual, long agora) {
        return quantidadeAtual < patosMinimos && agora - ultimoSpawn > delaySpawnAtual;
    }

    public void registrarSpawn(long agora) { this.ultimoSpawn = agora; }

    public void subirRound() {
        roundAtual++;
        velocidadeBase += 1;
        intensidadeZigZag += 0.5;
        delaySpawnAtual = Math.max(300, delaySpawnAtual - 50);
        if (roundAtual % 2 == 0) patosMinimos++;
    }

    public void resetar() {
        roundAtual = 1;
        velocidadeBase = 4;
        intensidadeZigZag = 0;
        delaySpawnAtual = 600;
        patosMinimos = 4;
        ultimoSpawn = 0;
    }

    public int getRoundAtual() { return roundAtual; }
    public int getVelocidadeBase() { return velocidadeBase; }
    public double getIntensidadeZigZag() { return intensidadeZigZag; }
    public int sortearQuantidade() { return (roundAtual > 3 && random.nextBoolean()) ? 2 : 1; }
}