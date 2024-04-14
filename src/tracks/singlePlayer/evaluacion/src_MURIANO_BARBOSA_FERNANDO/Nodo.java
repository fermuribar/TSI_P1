package tracks.singlePlayer.evaluacion.src_MURIANO_BARBOSA_FERNANDO;
import java.util.ArrayList;
import java.util.Collections;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

import java.lang.Math;

public class Nodo implements Comparable<Nodo>{

    public Nodo padre;
    public ACTIONS accion_desde_padre;

    public ACTIONS accion_giro;

    public Vector2d pos_jugador;
    public Vector2d ori_jugador;

    public int coste;
    public int h;

    public Nodo(){
        padre = null;
        accion_desde_padre = null;
        accion_giro = null;
        pos_jugador = new Vector2d();
        ori_jugador = new Vector2d();
        coste = 0;
        h = 0;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Nodo o = (Nodo) obj;
        return 	pos_jugador.equals(o.pos_jugador) && 
        ori_jugador.equals(o.ori_jugador);
    }
    
    @Override
    public int hashCode() { //GPT
        int prime = 31;
        int result = 17;
        long temp;
        temp = Double.doubleToLongBits(pos_jugador.x);
        result = prime * result + (int) (temp ^ (temp >>> 32));

        temp = Double.doubleToLongBits(pos_jugador.y);
        result = prime * result + (int) (temp ^ (temp >>> 32));

        temp = Double.doubleToLongBits(ori_jugador.x);
        result = prime * result + (int) (temp ^ (temp >>> 32));

        temp = Double.doubleToLongBits(ori_jugador.y);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        return result;
    }

    @Override
    public int compareTo(Nodo o) {
        return Integer.compare(coste + h, o.coste + o.h);
    }

    public boolean jugador_en_meta(Vector2d meta){
        if(pos_jugador.equals(meta)){
            return true;
        }else{
            return false;
        }
    }

    public void calcula_h(Vector2d meta){
        h = (int) Math.abs(pos_jugador.x - meta.x) + (int) Math.abs(pos_jugador.y - meta.y);
    }
}
