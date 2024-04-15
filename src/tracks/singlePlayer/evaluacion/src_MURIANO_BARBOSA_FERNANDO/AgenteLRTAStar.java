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

public class AgenteLRTAStar extends AbstractPlayer{

	Vector2d fescala;
    Vector2d portal;
    boolean [][] mapa_visitables;
	int [][] mapa_h;

	Nodo estado_actual;

    int nodo_expandidos;

    /**
	 * initialize all variables for the agent
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 */
	public AgenteLRTAStar(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		//Calculamos el factor de escala entre mundos (pixeles -> grid)
        fescala = new Vector2d(stateObs.getWorldDimension().width / stateObs.getObservationGrid().length , 
        		stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length);      

        //Se crea una lista de observaciones de portales, ordenada por cercania al avatar
        ArrayList<Observation>[] posiciones = stateObs.getPortalsPositions(stateObs.getAvatarPosition());
        //Seleccionamos el portal mas proximo
        portal = posiciones[0].get(0).position;
        portal.x = Math.floor(portal.x / fescala.x);
        portal.y = Math.floor(portal.y / fescala.y);

        mapa_visitables = new boolean[stateObs.getObservationGrid().length][stateObs.getObservationGrid()[0].length];
		mapa_h = new int[stateObs.getObservationGrid().length][stateObs.getObservationGrid()[0].length];

        ArrayList<Observation>[] lista_no_pasar = stateObs.getImmovablePositions();
        for(int x = 0; x < mapa_visitables.length; x++){
            for(int y = 0; y < mapa_visitables[0].length; y++){
                mapa_visitables[x][y] = true;
				mapa_h[x][y] = Math.abs(x - (int) portal.x) + Math.abs(y - (int) portal.y);
            }
        }

        for(int i = 0; i < lista_no_pasar[0].size();i++){   //murons
            mapa_visitables[(int) (lista_no_pasar[0].get(i).position.x / fescala.x)] [(int) (lista_no_pasar[0].get(i).position.y/fescala.x)] = false;
        }
        for(int i = 0; i < lista_no_pasar[1].size();i++){   //trampas
            mapa_visitables[(int) (lista_no_pasar[1].get(i).position.x / fescala.x)] [(int) (lista_no_pasar[1].get(i).position.y/fescala.x)] = false;
        }

		/*
		for(int x = 0; x < mapa_visitables.length; x++){
            for(int y = 0; y < mapa_visitables[0].length; y++){
                System.out.printf("%d\t", mapa_h[x][y] );
            }
			System.out.printf("\n");
        }
		*/
		estado_actual = new Nodo();
		estado_actual.pos_jugador.x = stateObs.getAvatarPosition().x / fescala.x;
        estado_actual.pos_jugador.y = stateObs.getAvatarPosition().y / fescala.y;
        estado_actual.ori_jugador = stateObs.getAvatarOrientation().copy();


		repite = false;
		accion = Types.ACTIONS.ACTION_NIL;
		tiempoTotalms = 0;
		nodo_expandidos = 0;
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
			tiempoTotalms += (tFin - tInicio)/1000000;
			System.out.printf("(runtime %d; nodos_expand: %d)\n",tiempoTotalms, nodo_expandidos);
		}else{
			repite = false;
		}
		//System.err.print(accion + "\n");
        return accion;
    }

	boolean repite;
	ACTIONS accion;
	long tiempoTotalms;

	private void RTAStar(StateObservation stateObs){
		ArrayList<Observation>[] lista_no_pasar = stateObs.getImmovablePositions();
		for(int i = 0; i < lista_no_pasar[0].size();i++){   //murons
            mapa_visitables[(int) (lista_no_pasar[0].get(i).position.x / fescala.x)] [(int) (lista_no_pasar[0].get(i).position.y/fescala.x)] = false;
        }
        for(int i = 0; i < lista_no_pasar[1].size();i++){   //trampas
            mapa_visitables[(int) (lista_no_pasar[1].get(i).position.x / fescala.x)] [(int) (lista_no_pasar[1].get(i).position.y/fescala.x)] = false;
        }
		int c_mas_h_arriba = 100000;
		int c_mas_h_abajo = 100000;
		int c_mas_h_izquierda = 100000;
		int c_mas_h_derecha = 100000;

		repite = false;

		nodo_expandidos++;

		//Calculo coste y h de arriba
		if(mapa_visitables[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y - 1]){
			c_mas_h_arriba = mapa_h[(int) estado_actual.pos_jugador.x][(int) (estado_actual.pos_jugador.y - 1.0)];
			if(estado_actual.ori_jugador.x == 0 && estado_actual.ori_jugador.y == -1){
				c_mas_h_arriba += 1;
			}else{
				c_mas_h_arriba += 2;
			}
		}

		//Calculo coste y h de abajo
		if(mapa_visitables[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y + 1]){
			c_mas_h_abajo = mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y + 1];
			if(estado_actual.ori_jugador.x == 0 && estado_actual.ori_jugador.y == 1){
				c_mas_h_abajo += 1;
			}else{
				c_mas_h_abajo += 2;
			}
			
		}

		//Calculo coste y h de izq
		if(mapa_visitables[(int) estado_actual.pos_jugador.x - 1][(int) estado_actual.pos_jugador.y]){
			c_mas_h_izquierda = mapa_h[(int) estado_actual.pos_jugador.x - 1][(int) estado_actual.pos_jugador.y];
			if(estado_actual.ori_jugador.x == -1 && estado_actual.ori_jugador.y == 0){
				c_mas_h_izquierda += 1;
			}else{
				c_mas_h_izquierda += 2;
			}
			
		}

		//Calculo coste y h de der
		if(mapa_visitables[(int) estado_actual.pos_jugador.x + 1][(int) estado_actual.pos_jugador.y]){
			c_mas_h_derecha = mapa_h[(int) estado_actual.pos_jugador.x + 1][(int) estado_actual.pos_jugador.y];
			if(estado_actual.ori_jugador.x == 1 && estado_actual.ori_jugador.y == 0){
				c_mas_h_derecha += 1;
			}else{
				c_mas_h_derecha += 2;
			}
			
		}

		//cual maximo

		int minimo = Math.min(Math.min(c_mas_h_arriba, c_mas_h_abajo), Math.min(c_mas_h_izquierda, c_mas_h_derecha));
		//System.out.printf("(%d,%d) - ari: %d; aba: %d; izq: %d; der: %d; ->min %d\n", (int) estado_actual.pos_jugador.x, (int) estado_actual.pos_jugador.y, c_mas_h_arriba, c_mas_h_abajo, c_mas_h_izquierda, c_mas_h_derecha, minimo);
		

		if(c_mas_h_arriba == minimo){
			accion = Types.ACTIONS.ACTION_UP;
				mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y] = c_mas_h_arriba;
			if(estado_actual.ori_jugador.y != -1) repite = true;
			estado_actual.pos_jugador.y -= 1;
			estado_actual.ori_jugador.x = 0;
			estado_actual.ori_jugador.y = -1;
		}else if(c_mas_h_abajo == minimo){
			accion = Types.ACTIONS.ACTION_DOWN;
				mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y] = c_mas_h_abajo;
			if(estado_actual.ori_jugador.y != 1) repite = true;
			estado_actual.pos_jugador.y += 1;
			estado_actual.ori_jugador.x = 0;
			estado_actual.ori_jugador.y = 1;
		}else if(c_mas_h_izquierda == minimo){
			accion = Types.ACTIONS.ACTION_LEFT;
				mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y] = c_mas_h_izquierda;
			if(estado_actual.ori_jugador.x != -1) repite = true;
			estado_actual.pos_jugador.x -= 1;
			estado_actual.ori_jugador.x = -1;
			estado_actual.ori_jugador.y = 0;
		}else if(c_mas_h_derecha == minimo){
			accion = Types.ACTIONS.ACTION_RIGHT;
				mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y] = c_mas_h_derecha;
			if(estado_actual.ori_jugador.x != 1) repite = true;
			estado_actual.pos_jugador.x += 1;
			estado_actual.ori_jugador.x = 1;
			estado_actual.ori_jugador.y = 0;
		}
	}

}
