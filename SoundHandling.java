import java.io.*;
import javax.sound.sampled.*;

// change mp3 files to .wav later
public class SoundHandling {

    // if want loop backround sound
    private static Clip loopclip;
    
    // separate clip for game music (music2.wav)
    private static Clip musicclip;

    // description: play a sound once
    // parms: filename (path to audio file)
    // returns: none
    public static void playSound(String filename) {
        try {
            File soundfile = new File(filename);
            AudioInputStream audioin = AudioSystem.getAudioInputStream(soundfile);
            Clip clip = AudioSystem.getClip();
            clip.open(audioin);
            clip.start();
        } catch (Exception e) {}
    }

    // description: start looping the in-game ambience
    // parms: none
    // returns: none
    public static void startBackgroundLoop() {
        startLoop("util/sounds/natureambiencesound.wav");
    }
    // description: stop looping ambience/music that uses loopclip
    // parms: none
    // returns: none
    public static void stopBackgroundLoop() {
        if (loopclip != null) {
            loopclip.stop();
            loopclip.close();
            loopclip = null;
        }
    }

    // description: start looping a sound file
    // parms: filename (path to audio file)
    // returns: none
    private static void startLoop(String filename) {
        stopBackgroundLoop();
        try {
            File soundfile = new File(filename);
            if (!soundfile.exists()) {
                System.out.println("sound file not found: " + filename);
                return;
            }
            
            // try to get audio input stream - this may fail if format is unsupported
            AudioInputStream audioin = AudioSystem.getAudioInputStream(soundfile);
            
            // check if we can get format info
            AudioFormat format = audioin.getFormat();
            if (format.getEncoding() == AudioFormat.Encoding.PCM_SIGNED || 
                format.getEncoding() == AudioFormat.Encoding.PCM_UNSIGNED) {
                // this is a standard PCM format, should work
            } else {
                // try to convert to PCM if it's not already
                AudioFormat targetFormat = new AudioFormat(
                    AudioFormat.Encoding.PCM_SIGNED,
                    format.getSampleRate(),
                    16,
                    format.getChannels(),
                    format.getChannels() * 2,
                    format.getSampleRate(),
                    false
                );
                audioin = AudioSystem.getAudioInputStream(targetFormat, audioin);
            }
            
            loopclip = AudioSystem.getClip();
            loopclip.open(audioin);
            loopclip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {}
    }

    // description: play startup sound
    // parms: none
    // returns: none
    public static void playstartupsound() {
        playSound("util/sounds/gamestart.wav");
    }

    // description: play explosion sound
    // parms: none
    // returns: none
    public static void playboom() {
        playSound("util/sounds/explosion.wav");
    }

    // description: play win sound
    // parms: none
    // returns: none
    public static void playwin() {
        playSound("util/sounds/victory.wav");
    }

    // description: play movement sound
    // parms: none
    // returns: none
    public static void playmove() {
        playSound("util/sounds/bushcut.wav");
    }

    // description: play heartbeat sound (low)
    // parms: none
    // returns: none
    public static void playheartbeat1() {
        playSound("util/sounds/2x heartbeat.wav");
    }

    // description: play heartbeat sound (high)
    // parms: none
    // returns: none
    public static void playheartbeat2() {
        playSound("util/sounds/3x heartbeat.wav");
    }

    // description: play breathing sound
    // parms: none
    // returns: none
    public static void playbreathing() {
        playSound("util/sounds/heavy breathing.wav");
    }

    // description: play page turn sound
    // parms: none
    // returns: none
    public static void playpageturn() {
        playSound("util/sounds/pageturn.wav");
    }

    // description: play sonar ping sound
    // parms: none
    // returns: none
    public static void playsonarping() {
        playSound("util/sounds/sonarping.wav");
    }

    // description: play button click sound
    // parms: none
    // returns: none
    public static void playbuttonclicked() {
        playSound("util/sounds/buttonclicked.wav");
    }

    // description: start looping menu music
    // parms: none
    // returns: none
    public static void startmenumusic() {
        startLoop("util/sounds/music.wav");
    }

    // description: start looping game music (optional)
    // parms: none
    // returns: none
    public static void startgamemusic() {
        stopgamemusic(); // stop if already playing
        try {
            File soundfile = new File("util/sounds/music2.wav");
            AudioInputStream audioin = AudioSystem.getAudioInputStream(soundfile);
            musicclip = AudioSystem.getClip();
            musicclip.open(audioin);
            
            // set volume to -15dB (lowered)
            if (musicclip.isControlSupported(FloatControl.Type.MASTER_GAIN)) {
                FloatControl gainControl = (FloatControl) musicclip.getControl(FloatControl.Type.MASTER_GAIN);
                gainControl.setValue(-15.0f); // lower volume
            }
            
            musicclip.loop(Clip.LOOP_CONTINUOUSLY);
        } catch (Exception e) {}
    }

    // description: stop game music
    // parms: none
    // returns: none
    public static void stopgamemusic() {
        if (musicclip != null) {
            musicclip.stop();
            musicclip.close();
            musicclip = null;
        }
    }
}
