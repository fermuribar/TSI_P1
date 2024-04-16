package tracks.singlePlayer.evaluacion.src_MURIANO_BARBOSA_FERNANDO;
import java.util.ArrayList;

import core.game.Observation;
import core.game.StateObservation;
import core.player.AbstractPlayer;
import ontology.Types;
import ontology.Types.ACTIONS;
import tools.ElapsedCpuTimer;
import tools.Vector2d;

public class AgenteCompeticion extends AbstractPlayer{

	Vector2d fescala;
    Vector2d portal;
	int [][][] mapa_h;

	Nodo estado_actual;

    int nodo_expandidos;

	int gemas_obt;

    /**
	 * initialize all variables for the agent
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 */
	public AgenteCompeticion(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
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

		ArrayList<Observation>[] gemas = stateObs.getResourcesPositions(stateObs.getAvatarPosition());
		mapa_h = new int[observaciones.length][observaciones[0].length][10];	//9 para almacenar las 8 gemas mas cercanas y el portal

        for(int x = 0; x < mapa_h.length; x++){
            for(int y = 0; y < mapa_h[0].length; y++){
				mapa_h[x][y][0] = Math.abs(x - (int) Math.floor(gemas[0].get(0).position.x / fescala.x)) + Math.abs(y - (int) Math.floor(gemas[0].get(0).position.y / fescala.y));
				mapa_h[x][y][1] = Math.abs(x - (int) Math.floor(gemas[0].get(1).position.x / fescala.x)) + Math.abs(y - (int) Math.floor(gemas[0].get(1).position.y / fescala.y));
				mapa_h[x][y][2] = Math.abs(x - (int) Math.floor(gemas[0].get(2).position.x / fescala.x)) + Math.abs(y - (int) Math.floor(gemas[0].get(2).position.y / fescala.y));
				mapa_h[x][y][3] = Math.abs(x - (int) Math.floor(gemas[0].get(3).position.x / fescala.x)) + Math.abs(y - (int) Math.floor(gemas[0].get(3).position.y / fescala.y));
				mapa_h[x][y][4] = Math.abs(x - (int) Math.floor(gemas[0].get(4).position.x / fescala.x)) + Math.abs(y - (int) Math.floor(gemas[0].get(4).position.y / fescala.y));
				mapa_h[x][y][5] = Math.abs(x - (int) Math.floor(gemas[0].get(5).position.x / fescala.x)) + Math.abs(y - (int) Math.floor(gemas[0].get(5).position.y / fescala.y));
				mapa_h[x][y][6] = Math.abs(x - (int) Math.floor(gemas[0].get(6).position.x / fescala.x)) + Math.abs(y - (int) Math.floor(gemas[0].get(6).position.y / fescala.y));
				mapa_h[x][y][7] = Math.abs(x - (int) Math.floor(gemas[0].get(7).position.x / fescala.x)) + Math.abs(y - (int) Math.floor(gemas[0].get(7).position.y / fescala.y));
				mapa_h[x][y][8] = Math.abs(x - (int) Math.floor(gemas[0].get(8).position.x / fescala.x)) + Math.abs(y - (int) Math.floor(gemas[0].get(8).position.y / fescala.y));
				mapa_h[x][y][9] = Math.abs(x - (int) portal.x) + Math.abs(y - (int) portal.y);
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
		gemas_obt = 0;
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
			
		}else{
			repite = false;
		}
		//if(terminado) System.out.printf("(runtime %d; nodos_expand: %d)\n",tiempoTotalms, nodo_expandidos);
        return accion;
    }

	boolean repite;
	boolean terminado;
	ACTIONS accion;
	long tiempoTotalms;

	private void RTAStar(StateObservation stateObs){
		//ArrayList<Observation>[][] observaciones = stateObs.getObservationGrid();
		int c_mas_h_arriba = 100000;
		int c_mas_h_abajo = 100000;
		int c_mas_h_izquierda = 100000;
		int c_mas_h_derecha = 100000;

		repite = false;

		nodo_expandidos++;

		//Calculo coste y h de arriba
		
		if(act_posible(stateObs,Types.ACTIONS.ACTION_UP)){
			c_mas_h_arriba = mapa_h[(int) estado_actual.pos_jugador.x][(int) (estado_actual.pos_jugador.y - 1.0)][gemas_obt];
			if(estado_actual.ori_jugador.x == 0 && estado_actual.ori_jugador.y == -1){
				c_mas_h_arriba += 1;
			}else{
				c_mas_h_arriba += 2;
			}
		}

		//Calculo coste y h de abajo
		if(act_posible(stateObs,Types.ACTIONS.ACTION_DOWN)){
			c_mas_h_abajo = mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y + 1][gemas_obt];
			if(estado_actual.ori_jugador.x == 0 && estado_actual.ori_jugador.y == 1){
				c_mas_h_abajo += 1;
			}else{
				c_mas_h_abajo += 2;
			}
		}

		//Calculo coste y h de izq
		if(act_posible(stateObs,Types.ACTIONS.ACTION_LEFT)){
			c_mas_h_izquierda = mapa_h[(int) estado_actual.pos_jugador.x - 1][(int) estado_actual.pos_jugador.y][gemas_obt];
			if(estado_actual.ori_jugador.x == -1 && estado_actual.ori_jugador.y == 0){
				c_mas_h_izquierda += 1;
			}else{
				c_mas_h_izquierda += 2;
			}	
		}

		//Calculo coste y h de der
		if(act_posible(stateObs,Types.ACTIONS.ACTION_RIGHT)){
			c_mas_h_derecha = mapa_h[(int) estado_actual.pos_jugador.x + 1][(int) estado_actual.pos_jugador.y][gemas_obt];
			if(estado_actual.ori_jugador.x == 1 && estado_actual.ori_jugador.y == 0){
				c_mas_h_derecha += 1;
			}else{
				c_mas_h_derecha += 2;
			}	
		}

		//cual minimos

		int minimo = Math.min(Math.min(c_mas_h_arriba, c_mas_h_abajo), Math.min(c_mas_h_izquierda, c_mas_h_derecha));
		int segundo_minimo = 0;
		System.out.printf("(%d,%d) - ari: %d; aba: %d; izq: %d; der: %d; ->min %d\n", (int) estado_actual.pos_jugador.x, (int) estado_actual.pos_jugador.y, c_mas_h_arriba, c_mas_h_abajo, c_mas_h_izquierda, c_mas_h_derecha, minimo);
		

		if(c_mas_h_arriba == minimo){
			accion = Types.ACTIONS.ACTION_UP;
			segundo_minimo = Math.min(c_mas_h_abajo, Math.min(c_mas_h_izquierda, c_mas_h_derecha));
			if(segundo_minimo != 100000){
				mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y][gemas_obt] = segundo_minimo;
			}else{
				mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y][gemas_obt] = c_mas_h_arriba;
			}
			if(estado_actual.ori_jugador.y != -1) repite = true;
			estado_actual.pos_jugador.y -= 1;
			estado_actual.ori_jugador.x = 0;
			estado_actual.ori_jugador.y = -1;
		}else if(c_mas_h_abajo == minimo){
			accion = Types.ACTIONS.ACTION_DOWN;
			segundo_minimo = Math.min(c_mas_h_arriba, Math.min(c_mas_h_izquierda, c_mas_h_derecha));
			if(segundo_minimo != 100000){
				mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y][gemas_obt] = segundo_minimo;
			}else{
				mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y][gemas_obt] = c_mas_h_abajo;
			}
			if(estado_actual.ori_jugador.y != 1) repite = true;
			estado_actual.pos_jugador.y += 1;
			estado_actual.ori_jugador.x = 0;
			estado_actual.ori_jugador.y = 1;
		}else if(c_mas_h_izquierda == minimo){
			accion = Types.ACTIONS.ACTION_LEFT;
			segundo_minimo = Math.min(Math.min(c_mas_h_arriba, c_mas_h_abajo), c_mas_h_derecha);
			if(segundo_minimo != 100000){
				mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y][gemas_obt] = segundo_minimo;
			}else{
				mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y][gemas_obt] = c_mas_h_izquierda;
			}
			if(estado_actual.ori_jugador.x != -1) repite = true;
			estado_actual.pos_jugador.x -= 1;
			estado_actual.ori_jugador.x = -1;
			estado_actual.ori_jugador.y = 0;
		}else if(c_mas_h_derecha == minimo){
			accion = Types.ACTIONS.ACTION_RIGHT;
			segundo_minimo = Math.min(Math.min(c_mas_h_arriba, c_mas_h_abajo), c_mas_h_izquierda);
			if(segundo_minimo != 100000){
				mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y][gemas_obt] = segundo_minimo;
			}else{
				mapa_h[(int) estado_actual.pos_jugador.x][(int) estado_actual.pos_jugador.y][gemas_obt] = c_mas_h_derecha;
			}
			if(estado_actual.ori_jugador.x != 1) repite = true;
			estado_actual.pos_jugador.x += 1;
			estado_actual.ori_jugador.x = 1;
			estado_actual.ori_jugador.y = 0;
		}
		if(!stateObs.getAvatarResources().isEmpty())
			gemas_obt = (stateObs.getAvatarResources().get(6) > 9)? 9 : stateObs.getAvatarResources().get(6);
		System.out.print(gemas_obt);
		if(estado_actual.pos_jugador.equals(portal) && gemas_obt == 8) terminado = true;
	}

	private boolean act_posible(StateObservation stateObs, ACTIONS act){
		ArrayList<Observation>[][] observaciones = stateObs.getObservationGrid();
		ArrayList<Observation>[] posiciones_npc = stateObs.getNPCPositions(stateObs.getAvatarPosition());
        //Seleccionamos el  mas proximo
        Vector2d NPC = posiciones_npc[0].get(0).position;
        NPC.x = Math.floor(NPC.x / fescala.x);
        NPC.y = Math.floor(NPC.y / fescala.y);
		
		int x_max = observaciones.length;
		int y_max = observaciones[0].length;
		int x = (int) estado_actual .pos_jugador.x;
		int y = (int) estado_actual .pos_jugador.y;
		boolean posible = false;
		switch (act) {
			case ACTION_UP:
				System.out.println("arriba");
				if(
					( observaciones[x][y - 1].isEmpty() || (observaciones[x][y - 1].get(0).category != 4) ) 
					&& (!(NPC.x == x && NPC.y == y-1) && !(NPC.x == x-1 && NPC.y == y-1) && !(NPC.x == x+1 && NPC.y == y-1) && !(NPC.x == x-2 && NPC.y == y-1) && !(NPC.x == x+2 && NPC.y == y-1))
					&& (!(NPC.x == x && NPC.y == y-2) && !(NPC.x == x-1 && NPC.y == y-2) && !(NPC.x == x+1 && NPC.y == y-2))
					&& (!(NPC.x == x && NPC.y == y-3))
				)
				posible = true;
				System.out.println(posible);
				break;
			case ACTION_DOWN:
				System.out.println("abajo");
				if(
					( observaciones[x][y + 1].isEmpty() || (observaciones[x][y + 1].get(0).category != 4) ) 
					&& (!(NPC.x == x && NPC.y == y+1) && !(NPC.x == x-1 && NPC.y == y+1) && !(NPC.x == x+1 && NPC.y == y+1) && !(NPC.x == x-2 && NPC.y == y+1) && !(NPC.x == x+2 && NPC.y == y+1))
					&& (!(NPC.x == x && NPC.y == y+2) && !(NPC.x == x-1 && NPC.y == y+2) && !(NPC.x == x+1 && NPC.y == y+2))
					&& (!(NPC.x == x && NPC.y == y+3))
				)
				posible = true;
				System.out.println(posible);
				break;
			case ACTION_LEFT:
				System.out.println("L");
				if(
					( observaciones[x - 1][y].isEmpty() || (observaciones[x - 1][y].get(0).category != 4) ) 
					&& (!(NPC.y == y && NPC.x == x-1) && !(NPC.y == y-1 && NPC.x == x-1) && !(NPC.y == y+1 && NPC.x == x-1) && !(NPC.y == y-2 && NPC.x == x-1) && !(NPC.y == y+2 && NPC.x == x-1))
					&& (!(NPC.y == y && NPC.x == x-2) && !(NPC.y == y-1 && NPC.x == x-2) && !(NPC.y == y+1 && NPC.x == x-2))
					&& (!(NPC.y == y && NPC.x == x-3))
				)
				posible = true;
				System.out.println(posible);
				break;
			case ACTION_RIGHT:
				System.out.println("R");
				if(
					( observaciones[x + 1][y].isEmpty() || (observaciones[x + 1][y].get(0).category != 4) )
					&& (!(NPC.y == y && NPC.x == x+1) && !(NPC.y == y-1 && NPC.x == x+1) && !(NPC.y == y+1 && NPC.x == x+1) && !(NPC.y == y-2 && NPC.x == x+1) && !(NPC.y == y+2 && NPC.x == x+1))
					&& (!(NPC.y == y && NPC.x == x+2) && !(NPC.y == y-1 && NPC.x == x+2) && !(NPC.y == y+1 && NPC.x == x+2))
					&& (!(NPC.y == y && NPC.x == x+3))
				)
				posible = true;
				System.out.println(posible);
				break;
			default:
				break;
		}

		return posible;
	}

}
