package jogo;

import javax.sound.sampled.*;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Musica {

    private static final Map<String, Clip> clips = new HashMap<>();
    private static Clip musicaFundo;

    public static void carregar(String nome, String caminho) {
        try {
            AudioInputStream ais = AudioSystem.getAudioInputStream(
                    Musica.class.getResource(caminho)
            );

            Clip clip = AudioSystem.getClip();
            clip.open(ais);
            clips.put(nome, clip);

        } catch (UnsupportedAudioFileException |
                 IOException |
                 LineUnavailableException |
                 IllegalArgumentException e) {
            e.printStackTrace();
        }
    }

    // Efeitos sonoros
    public static void tocar(String nome) {
        Clip clip = clips.get(nome);
        if (clip == null) return;

        if (clip.isRunning()) {
            clip.stop();
        }
        clip.setFramePosition(0);
        clip.start();
    }

    // Música de fundo
    public static void tocarLoop(String nome) {
        Clip clip = clips.get(nome);
        if (clip == null) return;

        if (musicaFundo != null && musicaFundo.isRunning()) return;

        musicaFundo = clip;
        musicaFundo.setFramePosition(0);
        musicaFundo.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public static void pararMusicaFundo() {
        if (musicaFundo != null) {
            musicaFundo.stop();
            musicaFundo.setFramePosition(0);
        }
    }
}
