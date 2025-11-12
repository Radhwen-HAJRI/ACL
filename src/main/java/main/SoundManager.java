package main;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;

public class SoundManager {
    private Clip[] clips;
    private String[] soundNames = {"move", "hit", "win", "lose"}; 

    public SoundManager() {
        clips = new Clip[soundNames.length];
        loadSounds();
    }

    public void loadSounds() {
        try {
            for (int i = 0; i < soundNames.length; i++) {
                InputStream audioSrc = getClass().getResourceAsStream("/sounds/" + soundNames[i] + ".wav");
                if (audioSrc == null) {
                    System.out.println("Son manquant: " + soundNames[i] + ".wav");
                    continue;
                }
                InputStream bufferedIn = new BufferedInputStream(audioSrc);
                AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
                clips[i] = AudioSystem.getClip();
                clips[i].open(audioStream);
                System.out.println("Son chargé: " + soundNames[i]);
            }
        } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
            e.printStackTrace();
            System.out.println("Erreur chargement sons");
        }
    }

    public void play(int soundIndex) {
        if (soundIndex < 0 || soundIndex >= clips.length || clips[soundIndex] == null) {
            return; 
        }
        Clip clip = clips[soundIndex];
        if (clip.isRunning()) {
            clip.stop();  
        }
        clip.setFramePosition(0);
        clip.start();  
    }

    // Méthodes pratiques par nom
    public void playMove() { play(0); }    // Index 0 = move
    public void playHit() { play(1); }     // 1 = hit
    public void playWin() { play(2); }     // 2 = win
    public void playLose() { play(3); }    // 3 = lose
}