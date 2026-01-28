package entidades;

import java.awt.Graphics;
import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.imageio.ImageIO;

public class Cachorro extends Animal {

    public enum Estado {
        ESCONDIDO, INTRO_ANDANDO, INTRO_PULANDO, RINDO, CELEBRANDO
    }

    private Estado estadoAtual = Estado.ESCONDIDO;
    private List<Image> walkFrames = new ArrayList<>();
    private List<Image> laughFrames = new ArrayList<>();
    private Image imgJump, imgHappy;

    private int frameIndex = 0;
    private long lastFrameTime = 0;
    private final int ANIM_DELAY = 120;
    private long displayStartTime = 0;
    private final int DISPLAY_DURATION = 1500;

    // Alturas
    private final int Y_CHAO_INTRO = 430;
    private final int Y_ESCONDIDO = 700;

    public Cachorro(int x, int y) {
        super(x, y);
        this.y = Y_ESCONDIDO;
        carregarImagens();
    }

    private void carregarImagens() {
        try {
            walkFrames.add(ImageIO.read(getClass().getResource("/imagens/dogRight0.png")));
            walkFrames.add(ImageIO.read(getClass().getResource("/imagens/dogRight1.png")));
            walkFrames.add(ImageIO.read(getClass().getResource("/imagens/dogRight2.png")));
            laughFrames.add(ImageIO.read(getClass().getResource("/imagens/dogLaugh0.png")));
            laughFrames.add(ImageIO.read(getClass().getResource("/imagens/dogLaugh1.png")));
            imgJump = ImageIO.read(getClass().getResource("/imagens/dogJump.png"));
            imgHappy = ImageIO.read(getClass().getResource("/imagens/dogHappy.png"));
        } catch (Exception e) {
            System.err.println("Erro ao carregar sprites: " + e.getMessage());
        }
    }

    public void iniciarIntroducao() {
        this.estadoAtual = Estado.INTRO_ANDANDO;
        this.frameIndex = 0;
        this.x = -80;
        this.y = Y_CHAO_INTRO;
    }

    public boolean terminouIntroducao() {
        return estadoAtual != Estado.INTRO_ANDANDO && estadoAtual != Estado.INTRO_PULANDO;
    }

    public void rir(int posX) {
        this.x = posX;
        this.y = 370;
        this.estadoAtual = Estado.RINDO;
        this.frameIndex = 0;
        this.displayStartTime = System.currentTimeMillis();
    }

    public void celebrar(int posX) {
        this.x = posX;
        this.y = 370;
        this.estadoAtual = Estado.CELEBRANDO;
        this.frameIndex = 0;
        this.displayStartTime = System.currentTimeMillis();
    }

    @Override
    public void atualizar() {
        long agora = System.currentTimeMillis();

        switch (estadoAtual) {
            case INTRO_ANDANDO -> {
                x += 2;
                if (agora - lastFrameTime > ANIM_DELAY) {
                    frameIndex = (frameIndex + 1) % walkFrames.size();
                    lastFrameTime = agora;
                }
                if (x >= 320) {
                    estadoAtual = Estado.INTRO_PULANDO;
                    frameIndex = 0;
                }
            }
            case INTRO_PULANDO -> {
                y -= 7;
                if (y < 200) {
                    estadoAtual = Estado.ESCONDIDO;
                    y = Y_ESCONDIDO;
                }
            }
            case RINDO, CELEBRANDO -> {
                if (agora - displayStartTime > DISPLAY_DURATION) {
                    estadoAtual = Estado.ESCONDIDO;
                    y = Y_ESCONDIDO;
                }
                if (estadoAtual == Estado.RINDO && agora - lastFrameTime > ANIM_DELAY) {
                    frameIndex = (frameIndex + 1) % laughFrames.size();
                    lastFrameTime = agora;
                }
            }
        }
    }

    @Override
    public void desenhar(Graphics g) {
        if (estadoAtual == Estado.ESCONDIDO) return;

        Image imgAtual = null;
        try {
            switch (estadoAtual) {
                case INTRO_ANDANDO -> imgAtual = walkFrames.get(frameIndex);
                case INTRO_PULANDO -> imgAtual = imgJump;
                case RINDO -> imgAtual = laughFrames.get(frameIndex);
                case CELEBRANDO -> imgAtual = imgHappy;
            }
        } catch (Exception e) {
            frameIndex = 0;
        }

        if (imgAtual != null) {
            g.drawImage(imgAtual, x, y, null);
        }
    }
}