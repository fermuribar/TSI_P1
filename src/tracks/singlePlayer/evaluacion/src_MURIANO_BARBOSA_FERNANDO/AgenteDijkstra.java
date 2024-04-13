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

public class AgenteDijkstra extends AbstractPlayer {



	private class Nodo implements Comparable<Nodo>{
		public Queue<ACTIONS> act_al_nodo;
		public Vector2d pos_jugador;
		public Vector2d orientacion_jugador;

		public int coste;

		public Nodo(Queue<ACTIONS> lista_acciones, Vector2d pos, Vector2d  orientacion){
			act_al_nodo = new LinkedList<>(lista_acciones);
			pos_jugador = new Vector2d(pos);
			orientacion_jugador = new Vector2d(orientacion);
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj) return true;
			if (obj == null || getClass() != obj.getClass()) return false;
			Nodo o = (Nodo) obj;
			return pos_jugador == o.pos_jugador && orientacion_jugador == o.orientacion_jugador ;
	
		}
		
		@Override
		public int hashCode() {
			int prime = 31;
			int result = 17;
			long temp;
			temp = Double.doubleToLongBits(pos_jugador.x);
        	result = prime * result + (int) (temp ^ (temp >>> 32));

			temp = Double.doubleToLongBits(pos_jugador.y);
        	result = prime * result + (int) (temp ^ (temp >>> 32));

			temp = Double.doubleToLongBits(orientacion_jugador.x);
        	result = prime * result + (int) (temp ^ (temp >>> 32));

			temp = Double.doubleToLongBits(orientacion_jugador.y);
        	result = prime * result + (int) (temp ^ (temp >>> 32));
			return result;
		}

		@Override
		public int compareTo(Nodo o) {
			
			return Integer.compare(coste, o.coste);
		}
	}

	Vector2d fescala;
	Vector2d portal;
	Nodo nodo_en_objetivo;

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
	}


    /**
	 * return the best action to arrive faster to the closest portal
	 * @param stateObs Observation of the current state.
     * @param elapsedTimer Timer when the action returned is due.
	 * @return best	ACTION to arrive faster to the closest portal
	 */
	@Override
	public ACTIONS act(StateObservation stateObs, ElapsedCpuTimer elapsedTimer) {

        return Types.ACTIONS.ACTION_NIL;
    }
}
