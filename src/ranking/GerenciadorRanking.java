package ranking;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GerenciadorRanking {
    private static final String ARQUIVO_RANKING = "highscore.dat";


    public static void salvarRecorde(int novaPontuacao) {
        List<Integer> scores = lerRecordes();
        scores.add(novaPontuacao);

        // Ordena do maior para o menor
        Collections.sort(scores, Collections.reverseOrder());

        // Mantém apenas os 3 melhores
        if (scores.size() > 3) {
            scores = scores.subList(0, 3);
        }

        try (DataOutputStream dos = new DataOutputStream(new FileOutputStream(ARQUIVO_RANKING))) {
            dos.writeInt(scores.size());
            for (int s : scores) {
                dos.writeInt(s);
            }
        } catch (IOException e) {
            System.err.println("Erro ao salvar arquivo de ranking.");
        }
    }


      //Lê a lista de recordes do arquivo binário.

    public static List<Integer> lerRecordes() {
        List<Integer> scores = new ArrayList<>();
        File arquivo = new File(ARQUIVO_RANKING);

        if (!arquivo.exists()) return scores;

        try (DataInputStream dis = new DataInputStream(new FileInputStream(arquivo))) {
            int qtd = dis.readInt();
            for (int i = 0; i < qtd; i++) {
                scores.add(dis.readInt());
            }
        } catch (IOException e) {
        }
        return scores;
    }
}