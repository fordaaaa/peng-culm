import java.io.*;
import javax.sound.sampled.*;

// change mp3 files to .wav later
public class SoundHandling {

    // if want loop backround sound
    private static Clip loopclip;

    //play sound once
    public static void playSound(String filename) {
        try {
            File soundfile = new File(filename);
            AudioInputStream audioin = AudioSystem.getAudioInputStream(soundfile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioin);
            clip.start();
        } catch (UnsupportedAudioFileException e) {
            System.out.println("unsupported sound file: " + filename + " -> " + e.getMessage());
        } catch (LineUnavailableException e) {
            System.out.println("sound line unavailable: " + filename + " -> " + e.getMessage());
        } catch (IOException e) {
            System.out.println("i/o error playing sound: " + filename + " -> " + e.getMessage());
        } catch (Exception e) {
            System.out.println("unexpected error playing sound: " + filename + " -> " + e.getMessage());
        }
    }

    //ambience loopps
    public static void startBackgroundLoop() {
        startLoop("util/sounds/natureambiencesound.wav");
    }
    // stop above code
    public static void stopBackgroundLoop() {
        if (loopclip != null) {
            loopclip.stop();
            loopclip.close();
            loopclip = null;
        }
    }

    private static void startLoop(String filename) { // debug for now
        stopBackgroundLoop();
        try {
            File soundfile = new File(filename);
            AudioInputStream audioin = AudioSystem.getAudioInputStream(soundfile);
            loopclip = AudioSystem.getClip();
            loopclip.open(audioin);
            loopclip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (UnsupportedAudioFileException e) {
            System.out.println("unsupported loop sound file: " + filename + " -> " + e.getMessage());
        } catch (LineUnavailableException e) {
            System.out.println("sound line unavailable (loop): " + filename + " -> " + e.getMessage());
        } catch (IOException e) {
            System.out.println("i/o error looping sound: " + filename + " -> " + e.getMessage());
        } catch (Exception e) {
            System.out.println("unexpected error looping sound: " + filename + " -> " + e.getMessage());
        }
    }

    // methods for specific game sounds 

    public static void playstartupsound() {
        playSound("util/sounds/gamestart.wav");
    }

    public static void playboom() {
        playSound("util/sounds/explosion.wav");
    }

    public static void playwin() {
        playSound("util/sounds/victory.wav");
    }
}
