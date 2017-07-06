package es.elb4t.parejas;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import java.util.ArrayList;
import java.util.Random;

/**
 * Created by eloy on 6/7/17.
 */

public class Menu extends Activity {
    private Button btnJugar;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.menu);
        btnJugar = (Button) findViewById(R.id.btnJugar);
    }

    public void btnJugar_Click(View v) {
        Partida.tipoPartida = "LOCAL";
        nuevoJuego(4, 4);
        Intent intent = new Intent(this, Juego.class);
        startActivity(intent);
    }

    private void nuevoJuego(int col, int fil) {
        Partida.turno = 1;
        Partida.FILAS = fil;
        Partida.COLUMNAS = col;
        Partida.casillas = new int[Partida.COLUMNAS][Partida.FILAS];
        try {
            int size = Partida.FILAS * Partida.COLUMNAS;
            ArrayList<Integer> list = new ArrayList<Integer>();
            for (int i = 0; i < size; i++) {
                list.add(new Integer(i));
            }
            Random r = new Random();
            for (int i = size - 1; i >= 0; i--) {
                int t = 0;
                if (i > 0) {
                    t = r.nextInt(i);
                }
                t = list.remove(t).intValue();
                Partida.casillas[i % Partida.COLUMNAS][i / Partida.COLUMNAS] = 1 + (t % (size / 2));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
