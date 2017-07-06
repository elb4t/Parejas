package es.elb4t.parejas;

import android.widget.Button;

/**
 * Created by eloy on 6/7/17.
 */

public class Casilla {
    public int x;
    public int y;
    public Button boton;

    public Casilla(Button boton, int x, int y) {
        this.x = x;
        this.y = y;
        this.boton = boton;
    }
}
