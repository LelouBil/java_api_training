package fr.lernejo.navy_battle;

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.SecureRandom;


class GameManagerTest {
    private final SecureRandom random = new SecureRandom();
    private final int portA = random.nextInt(3000) + 6000;
    private final int portB = random.nextInt(3000) + 6000;


    @Test
    public void mainWrongArgs(){
        Launcher.main(new String[]{});
    }
    @Test
    public void testGameEnd(){
        final ByteArrayOutputStream myOut = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;

        OutputStream duplicatingOutputStream = new OutputStream() {

            public void write(int b) {
                originalOut.write(b);
                myOut.write(b);
            }
        };
        PrintStream out = new PrintStream(duplicatingOutputStream);
        System.setOut(out);

        Launcher.main(new String[]{String.valueOf(portA)});
        Launcher.main(new String[]{String.valueOf(portB),"http://localhost:" + portA});
        //noinspection StatementWithEmptyBody
        while(!myOut.toString().contains("J'ai gagné")){

        }
    }
}
