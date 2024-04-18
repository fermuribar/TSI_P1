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

public class AgenteRTAStar extends AbstractPlayer{

	Vector2d fescala;
    Vector2d portal;
	int [][] mapa_h;

	Nodo estado_actual;

    int nodo_expandidos;
	int n_acciones;

    /**
	 * initialize all variables for the agent
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 */
	public AgenteRTAStar(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		ArrayList<Observation>[][] observaciones = stateObs.getObservationGrid();
		//Calculamos el factor de escala entre mundos (pixeles -> grid)
        fescala = new Vector2d(stateObs.getWorldDimension().width / observaciones.length , 
        		stateObs.getWorldDimension().height / observaciones[0].length);      

        //Se crea una lista de observaciones de portales, ordenada por cercania al avatar
        ArrayList<Observation>[] posiciones = stateObs.getPortalsPositions(stateObs.getAvatarPosition());
        //Seleccionamos el portal mas proximo
        portal = posiciones[0].get(0).position;
        portal.x = Math.floor(portal.x / fescala.x);
        portal.y = Math.floor(portal.y / fescala.y);

		mapa_h = new int[observaciones.length][observaciones[0].length];

        for(int x = 0; x < mapa_h.length; x++){
            for(int y = 0; y < mapa_h[0].length; y++){
				mapa_h[x][y] = Math.abs(x - (int) portal.x) + Math.abs(y - (int) portal.y);
            }
        }
		estado_actual = new Nodo();
		estado_actual.pos_jugador.x = stateObs.getAvatarPosition().x / fescala.x;
        estado_actual.pos_jugador.y = stateObs.getAvatarPosition().y / fescala.y;
        estado_actual.ori_jugador = stateObs.getAvatarOrientation().copy();


		repite = false;
		terminado = false;
		accion = Types.ACTIONS.ACTION_NIL;
		tiempoTotalms = 0;
		nodo_expandidos = 0;
		n_acciones = 0;

		/*for(int x = 0; x < mapa_h.length; x++){
			for(int y = 0; y < mapa_h[0].length; y++){
				System.out.printf("%d\t", mapa_h[x][y]);
			}
			System.out.printf("\n");
		}*/
	}


    /**
	 * return the best action to arrive faster to the closest portal
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 * @return best	ACTION to arrive faster to the closest portal
	 */
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		if(!repite){
			long tInicio = System.nanoTime();
			RTAStar(stateObs);
			long tFin = System.nanoTime();
			tiempoTotalms += (tFin - tInicio);
			
		}else{
			repite = false;
		}
		n_acciones ++;
		if(terminado && !repite) System.out.printf("(runtime %d; nodos_expand: %d; l_camino: %d)\n",tiempoTotalms/1000000, nodo_expandidos, n_acciones);
        return accion;
    }

	boolean repite;
	boolean terminado;
	ACTIONS accion;
	long tiempoTotalms;

	private void RTAStar(StateObservation stateObs){
		ArrayList<Observation>[][] observaciones = stateObs.getObservationGrid();
		int c_mas_h_arriba = 100000;
		int c_mas_h_abajo = 100000;
		int c_mas_h_izquierda = 100000;
		int c_mas_h_derecha = 100000;

		repite = false;

		nodo_expandidos++;

		//Calculo coste y h de arriba
		if(0 <= estado_actual.pos_jugador.y - 1 && estado_actual.pos_jugador.y - 1 < observaciones[0].length)
		if(observaciones[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y - 1].isEmpty() || 
			observaciones[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y - 1].get(0).category != 4)
			{
			c_mas_h_arriba = mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y - 1];
			if(estado_actual.ori_jugador.x == 0 && estado_actual.ori_jugador.y == -1){
				c_mas_h_arriba += 1;
			}else{
				c_mas_h_arriba += 2;
			}
		}

		//Calculo coste y h de abajo
		if(0 <= estado_actual.pos_jugador.y + 1 && estado_actual.pos_jugador.y + 1 < observaciones[0].length)
		if(observaciones[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y + 1].isEmpty() ||
			observaciones[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y + 1].get(0).category != 4){
			c_mas_h_abajo = mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y + 1];
			if(estado_actual.ori_jugador.x == 0 && estado_actual.ori_jugador.y == 1){
				c_mas_h_abajo += 1;
			}else{
				c_mas_h_abajo += 2;
			}
		}

		//Calculo coste y h de izq
		if(0 <= estado_actual.pos_jugador.x - 1 && estado_actual.pos_jugador.x - 1 < observaciones.length)
		if(observaciones[(int) estado_actual.pos_jugador.x - 1][(int) estado_actual.pos_jugador.y].isEmpty() ||
			observaciones[(int) estado_actual.pos_jugador.x - 1][(int) estado_actual.pos_jugador.y].get(0).category != 4){
			c_mas_h_izquierda = mapa_h[(int) estado_actual.pos_jugador.x - 1][(int) estado_actual.pos_jugador.y];
			if(estado_actual.ori_jugador.x == -1 && estado_actual.ori_jugador.y == 0){
				c_mas_h_izquierda += 1;
			}else{
				c_mas_h_izquierda += 2;
			}	
		}

		//Calculo coste y h de der
		if(0 <= estado_actual.pos_jugador.x + 1 && estado_actual.pos_jugador.x + 1 < observaciones.length)
		if(observaciones[(int) estado_actual.pos_jugador.x + 1][(int) estado_actual.pos_jugador.y].isEmpty() || 
			observaciones[(int) estado_actual.pos_jugador.x + 1][(int) estado_actual.pos_jugador.y].get(0).category != 4){
			c_mas_h_derecha = mapa_h[(int) estado_actual.pos_jugador.x + 1][(int) estado_actual.pos_jugador.y];
			if(estado_actual.ori_jugador.x == 1 && estado_actual.ori_jugador.y == 0){
				c_mas_h_derecha += 1;
			}else{
				c_mas_h_derecha += 2;
			}	
		}

		//cual minimos

		int minimo = Math.min(Math.min(c_mas_h_arriba, c_mas_h_abajo), Math.min(c_mas_h_izquierda, c_mas_h_derecha));
		int segundo_minimo = 0;
		//System.out.printf("(%d,%d) - ari: %d; aba: %d; izq: %d; der: %d; ->min %d\n", (int) estado_actual.pos_jugador.x, (int) estado_actual.pos_jugador.y, c_mas_h_arriba, c_mas_h_abajo, c_mas_h_izquierda, c_mas_h_derecha, minimo);
		
		
		if(c_mas_h_arriba == minimo){
			accion = Types.ACTIONS.ACTION_UP;
			segundo_minimo = Math.min(c_mas_h_abajo, Math.min(c_mas_h_izquierda, c_mas_h_derecha));
			if(segundo_minimo != 100000){
				mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y] = segundo_minimo;
			}else{
				mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y] = c_mas_h_arriba;
			}
			if(estado_actual.ori_jugador.y != -1) repite = true;
			estado_actual.pos_jugador.y -= 1;
			estado_actual.ori_jugador.x = 0;
			estado_actual.ori_jugador.y = -1;
		}else if(c_mas_h_abajo == minimo){
			accion = Types.ACTIONS.ACTION_DOWN;
			segundo_minimo = Math.min(c_mas_h_arriba, Math.min(c_mas_h_izquierda, c_mas_h_derecha));
			if(segundo_minimo != 100000){
				mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y] = segundo_minimo;
			}else{
				mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y] = c_mas_h_abajo;
			}
			if(estado_actual.ori_jugador.y != 1) repite = true;
			estado_actual.pos_jugador.y += 1;
			estado_actual.ori_jugador.x = 0;
			estado_actual.ori_jugador.y = 1;
		}else if(c_mas_h_izquierda == minimo){
			accion = Types.ACTIONS.ACTION_LEFT;
			segundo_minimo = Math.min(Math.min(c_mas_h_arriba, c_mas_h_abajo), c_mas_h_derecha);
			if(segundo_minimo != 100000){
				mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y] = segundo_minimo;
			}else{
				mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y] = c_mas_h_izquierda;
			}
			if(estado_actual.ori_jugador.x != -1) repite = true;
			estado_actual.pos_jugador.x -= 1;
			estado_actual.ori_jugador.x = -1;
			estado_actual.ori_jugador.y = 0;
		}else if(c_mas_h_derecha == minimo){
			accion = Types.ACTIONS.ACTION_RIGHT;
			segundo_minimo = Math.min(Math.min(c_mas_h_arriba, c_mas_h_abajo), c_mas_h_izquierda);
			if(segundo_minimo != 100000){
				mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y] = segundo_minimo;
			}else{
				mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y] = c_mas_h_derecha;
			}
			if(estado_actual.ori_jugador.x != 1) repite = true;
			estado_actual.pos_jugador.x += 1;
			estado_actual.ori_jugador.x = 1;
			estado_actual.ori_jugador.y = 0;
		}
		if(estado_actual.pos_jugador.equals(portal)) terminado = true;
	}

}
