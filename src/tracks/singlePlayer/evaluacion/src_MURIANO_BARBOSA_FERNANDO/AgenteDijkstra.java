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

import java.util.LinkedList;
import java.util.Queue;

import java.util.PriorityQueue;

import java.util.HashSet;
import java.util.Set;

import java.util.List;

public class AgenteDijkstra extends AbstractPlayer {



	private class Nodo implements Comparable<Nodo>{
		public Queue<ACTIONS> act_al_nodo;
		public StateObservation estado_general;
		public int coste;

		public Nodo(Queue<ACTIONS> lista_acciones, StateObservation estado, int c){
			act_al_nodo = new LinkedList<>(lista_acciones);
			estado_general = estado.copy();
			coste = c;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null || getClass() != obj.getClass()) return false;
			Nodo o = (Nodo) obj;
			return 	estado_general.getAvatarPosition().equals(o.estado_general.getAvatarPosition()) && 
					estado_general.getAvatarOrientation().equals(o.estado_general.getAvatarOrientation()) && estado_general.getGameWinner() == o.estado_general.getGameWinner() ;
		}
		
		@Override
		public int hashCode() {
			int prime = 31;
			int result = 17;
			long temp;
			temp = Double.doubleToLongBits(estado_general.getAvatarPosition().x);
        	result = prime * result + (int) (temp ^ (temp >>> 32));

			temp = Double.doubleToLongBits(estado_general.getAvatarPosition().y);
        	result = prime * result + (int) (temp ^ (temp >>> 32));

			temp = Double.doubleToLongBits(estado_general.getAvatarOrientation().x);
        	result = prime * result + (int) (temp ^ (temp >>> 32));

			temp = Double.doubleToLongBits(estado_general.getAvatarOrientation().y);
        	result = prime * result + (int) (temp ^ (temp >>> 32));

			if(estado_general.getGameWinner() == Types.WINNER.PLAYER_WINS){
				result = result + 1;
			}

			return result;
		}

		@Override
		public int compareTo(Nodo o) {
			return Integer.compare(coste, o.coste);
		}

		public boolean jugador_en_meta(){
			if(estado_general.getGameWinner() == Types.WINNER.PLAYER_WINS){
				return true;
			}else{
				return false;
			}
		}
	}

	Vector2d fescala;
	Vector2d portal;
	Queue<ACTIONS> lista_de_acciones;

    /**
	 * initialize all variables for the agent
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 */
	public AgenteDijkstra(StateObservation stateObs, ElapsedCpuTimer elapsedTimer){
		//Calculamos el factor de escala entre mundos (pixeles -> grid)
        fescala = new Vector2d(stateObs.getWorldDimension().width / stateObs.getObservationGrid().length , 
        		stateObs.getWorldDimension().height / stateObs.getObservationGrid()[0].length);      

        //Se crea una lista de observaciones de portales, ordenada por cercania al avatar
        ArrayList<Observation>[] posiciones = stateObs.getPortalsPositions(stateObs.getAvatarPosition());
        //Seleccionamos el portal mas proximo
        portal = posiciones[0].get(0).position;
        portal.x = Math.floor(portal.x / fescala.x);
        portal.y = Math.floor(portal.y / fescala.y);

		lista_de_acciones = new LinkedList<>();
	}


    /**
	 * return the best action to arrive faster to the closest portal
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 * @return best	ACTION to arrive faster to the closest portal
	 */
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {
		if(lista_de_acciones.isEmpty()){
			Dijkstra(stateObs);
		}
        return lista_de_acciones.poll();
    }


	private void Dijkstra(StateObservation stateObs){
		Set<Nodo> cerrados = new HashSet<>();
		//List<Nodo> cerrados = new ArrayList<>();
		PriorityQueue<Nodo> abierotos = new PriorityQueue<>();

		Nodo nodo_actual = new Nodo(new LinkedList<>(), stateObs, 0);

		abierotos.add(nodo_actual);

		boolean Solucion_encontrada = nodo_actual.jugador_en_meta();

		while (!Solucion_encontrada && !abierotos.isEmpty()) {
			abierotos.poll();
			cerrados.add(nodo_actual);
			//System.out.printf("(%f,%f)\t",nodo_actual.estado_general.getAvatarPosition().x / fescala.x, nodo_actual.estado_general.getAvatarPosition().y / fescala.y);
			//System.out.printf("(%f,%f)\n",nodo_actual.estado_general.getAvatarOrientation().x, nodo_actual.estado_general.getAvatarOrientation().y);

			if(nodo_actual.jugador_en_meta()){
				Solucion_encontrada = true;
			}else{
				ArrayList<ACTIONS> acciones_posibles =	nodo_actual.estado_general.getAvailableActions();

				if(acciones_posibles.contains(Types.ACTIONS.ACTION_UP)){
					//genera hijo arriba
					Nodo hijo_arriba = new Nodo(nodo_actual.act_al_nodo, nodo_actual.estado_general, nodo_actual.coste + 1);
					hijo_arriba.estado_general.advance(Types.ACTIONS.ACTION_UP);
					hijo_arriba.act_al_nodo.add(Types.ACTIONS.ACTION_UP);
					if(!cerrados.contains(hijo_arriba)){
						abierotos.add(hijo_arriba);
					}
				}
				
				if(acciones_posibles.contains(Types.ACTIONS.ACTION_DOWN)){
					//genera hijo abajo
					Nodo hijo_abajo = new Nodo(nodo_actual.act_al_nodo, nodo_actual.estado_general, nodo_actual.coste + 1);
					hijo_abajo.estado_general.advance(Types.ACTIONS.ACTION_DOWN);
					hijo_abajo.act_al_nodo.add(Types.ACTIONS.ACTION_DOWN);
					if(!cerrados.contains(hijo_abajo)){
						abierotos.add(hijo_abajo);
					}
				}

				if(acciones_posibles.contains(Types.ACTIONS.ACTION_LEFT)){
					//genera hijo izqu
					Nodo hijo_izqu = new Nodo(nodo_actual.act_al_nodo, nodo_actual.estado_general, nodo_actual.coste + 1);
					hijo_izqu.estado_general.advance(Types.ACTIONS.ACTION_LEFT);
					hijo_izqu.act_al_nodo.add(Types.ACTIONS.ACTION_LEFT);
					if(!cerrados.contains(hijo_izqu)){
						abierotos.add(hijo_izqu);
					}
				}
				
				if(acciones_posibles.contains(Types.ACTIONS.ACTION_RIGHT)){
					//genera hijo dere
					Nodo hijo_dere = new Nodo(nodo_actual.act_al_nodo, nodo_actual.estado_general, nodo_actual.coste + 1);
					hijo_dere.estado_general.advance(Types.ACTIONS.ACTION_RIGHT);
					hijo_dere.act_al_nodo.add(Types.ACTIONS.ACTION_RIGHT);
					if(!cerrados.contains(hijo_dere)){
						abierotos.add(hijo_dere);
					}
				}
			}

			if(!Solucion_encontrada && !abierotos.isEmpty()){
				nodo_actual = abierotos.peek();
				while(!abierotos.isEmpty() && cerrados.contains(nodo_actual)){
					abierotos.poll();
					if(!abierotos.isEmpty()){
						nodo_actual = abierotos.peek();
					}
				}
			}
		}
		if(Solucion_encontrada){
			lista_de_acciones = new LinkedList<>(nodo_actual.act_al_nodo);
		}
	}
}
