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

import java.util.Stack;

import java.util.PriorityQueue;

import java.util.HashSet;
import java.util.Set;

public class AgenteDijkstra extends AbstractPlayer{

    Vector2d fescala;
    Vector2d portal;
    boolean [][] mapa_visitables;
    Stack<ACTIONS> acts;

    int nodo_expandidos;

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

        mapa_visitables = new boolean[stateObs.getObservationGrid().length][stateObs.getObservationGrid()[0].length];

        for(int x = 0; x < mapa_visitables.length; x++){
            for(int y = 0; y < mapa_visitables[0].length; y++){
                mapa_visitables[x][y] = true;
            }
        }

        ArrayList<Observation>[] lista_no_pasar = stateObs.getImmovablePositions();
        if(lista_no_pasar.length > 0)
        for(int i = 0; i < lista_no_pasar[0].size();i++){   //murons
            mapa_visitables[(int) (lista_no_pasar[0].get(i).position.x / fescala.x)] [(int) (lista_no_pasar[0].get(i).position.y/fescala.x)] = false;
        }
        if(lista_no_pasar.length > 1)
        for(int i = 0; i < lista_no_pasar[1].size();i++){   //trampas
            mapa_visitables[(int) (lista_no_pasar[1].get(i).position.x / fescala.x)] [(int) (lista_no_pasar[1].get(i).position.y/fescala.x)] = false;
        }

        acts = new Stack<>();
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
        if(acts.isEmpty()){
            long tInicio = System.nanoTime();
            Dijkstra(stateObs);
            long tFin = System.nanoTime();
            long tiempoTotalms = (tFin - tInicio)/1000000;
            System.out.printf("(runtime %d; nodos_expand: %d; l_camino: %d)",tiempoTotalms, nodo_expandidos, acts.size());
        }

        return acts.pop();
    }

    private void Dijkstra(StateObservation stateObs){
        Set<Nodo> cerrados = new HashSet<>();
		PriorityQueue<Nodo> abiertos = new PriorityQueue<>();

        Nodo nodo_actual = new Nodo();
        nodo_actual.pos_jugador.x = stateObs.getAvatarPosition().x / fescala.x;
        nodo_actual.pos_jugador.y = stateObs.getAvatarPosition().y / fescala.y;
        nodo_actual.ori_jugador = stateObs.getAvatarOrientation().copy();

        boolean Solucion_encontrada = nodo_actual.jugador_en_meta(portal);

        boolean expande;

        abiertos.add(nodo_actual);

        while (!Solucion_encontrada && !abiertos.isEmpty()) {
            abiertos.poll();
            cerrados.add(nodo_actual);

            nodo_expandidos++;

            if(nodo_actual.jugador_en_meta(portal)){
                Solucion_encontrada = true;
                break;
            }

            //expande arriba
            Nodo hijo_arriba = new Nodo();
            hijo_arriba.padre = nodo_actual;
            hijo_arriba.accion_desde_padre = Types.ACTIONS.ACTION_UP;
            hijo_arriba.coste = nodo_actual.coste + 1;
            hijo_arriba.ori_jugador.x = 0;
            hijo_arriba.ori_jugador.y = -1;
            hijo_arriba.pos_jugador.x = nodo_actual.pos_jugador.x;
            hijo_arriba.pos_jugador.y = nodo_actual.pos_jugador.y - 1;
            expande = true;
            
            if(0 <= hijo_arriba.pos_jugador.x && hijo_arriba.pos_jugador.x < mapa_visitables.length 
            && 0 <= hijo_arriba.pos_jugador.y && hijo_arriba.pos_jugador.y < mapa_visitables[0].length)
            if(!mapa_visitables[(int) hijo_arriba.pos_jugador.x][(int) hijo_arriba.pos_jugador.y]){
                expande = false;
            }else if(!hijo_arriba.ori_jugador.equals(nodo_actual.ori_jugador)){
                hijo_arriba.accion_giro = Types.ACTIONS.ACTION_UP;
                hijo_arriba.coste += 1;
            }

            if(!cerrados.contains(hijo_arriba) && expande){
                abiertos.add(hijo_arriba);
                //System.out.printf("(%d,%d) (%d,%d)-> c: %d; h: %d = %d\n", (int) hijo_arriba.pos_jugador.x, (int) hijo_arriba.pos_jugador.y, (int) hijo_arriba.ori_jugador.x, (int) hijo_arriba.ori_jugador.y, hijo_arriba.coste, hijo_arriba.h, hijo_arriba.coste + hijo_arriba.h);
            }

            //expande abajo
            Nodo hijo_abajo = new Nodo();
            hijo_abajo.padre = nodo_actual;
            hijo_abajo.accion_desde_padre = Types.ACTIONS.ACTION_DOWN;
            hijo_abajo.coste = nodo_actual.coste + 1;
            hijo_abajo.ori_jugador.x = 0;
            hijo_abajo.ori_jugador.y = 1;
            hijo_abajo.pos_jugador.x = nodo_actual.pos_jugador.x;
            hijo_abajo.pos_jugador.y = nodo_actual.pos_jugador.y + 1;
            expande = true;

            if(0 <= hijo_abajo.pos_jugador.x && hijo_abajo.pos_jugador.x < mapa_visitables.length 
            && 0 <= hijo_abajo.pos_jugador.y && hijo_abajo.pos_jugador.y < mapa_visitables[0].length)
            if(!mapa_visitables[(int) hijo_abajo.pos_jugador.x][(int) hijo_abajo.pos_jugador.y]){
                expande = false;
            }else if(!hijo_abajo.ori_jugador.equals(nodo_actual.ori_jugador)){
                hijo_abajo.accion_giro = Types.ACTIONS.ACTION_DOWN;
                hijo_abajo.coste += 1;
            }

            if(!cerrados.contains(hijo_abajo) && expande){
                abiertos.add(hijo_abajo);
                //System.out.printf("(%d,%d) (%d,%d)-> c: %d; h: %d = %d\n", (int) hijo_abajo.pos_jugador.x, (int) hijo_abajo.pos_jugador.y, (int) hijo_abajo.ori_jugador.x, (int) hijo_abajo.ori_jugador.y, hijo_abajo.coste, hijo_abajo.h, hijo_abajo.coste + hijo_abajo.h);
            }

            //expande izq
            Nodo hijo_izq = new Nodo();
            hijo_izq.padre = nodo_actual;
            hijo_izq.accion_desde_padre = Types.ACTIONS.ACTION_LEFT;
            hijo_izq.coste = nodo_actual.coste + 1;
            hijo_izq.ori_jugador.x = -1;
            hijo_izq.ori_jugador.y = 0;
            hijo_izq.pos_jugador.x = nodo_actual.pos_jugador.x - 1;
            hijo_izq.pos_jugador.y = nodo_actual.pos_jugador.y;
            expande = true;

            if(0 <= hijo_izq.pos_jugador.x && hijo_izq.pos_jugador.x < mapa_visitables.length 
            && 0 <= hijo_izq.pos_jugador.y && hijo_izq.pos_jugador.y < mapa_visitables[0].length)
            if(!mapa_visitables[(int) hijo_izq.pos_jugador.x][(int) hijo_izq.pos_jugador.y]){
                expande = false;
            }else if(!hijo_izq.ori_jugador.equals(nodo_actual.ori_jugador)){
                hijo_izq.accion_giro = Types.ACTIONS.ACTION_LEFT;
                hijo_izq.coste += 1;
            }

            if(!cerrados.contains(hijo_izq) && expande){
                abiertos.add(hijo_izq);
                //System.out.printf("(%d,%d) (%d,%d)-> c: %d; h: %d = %d\n", (int) hijo_izq.pos_jugador.x, (int) hijo_izq.pos_jugador.y, (int) hijo_izq.ori_jugador.x, (int) hijo_izq.ori_jugador.y, hijo_izq.coste, hijo_izq.h, hijo_izq.coste + hijo_izq.h);
            }

            //expande der
            Nodo hijo_der = new Nodo();
            hijo_der.padre = nodo_actual;
            hijo_der.accion_desde_padre = Types.ACTIONS.ACTION_RIGHT;
            hijo_der.coste = nodo_actual.coste + 1;
            hijo_der.ori_jugador.x = 1;
            hijo_der.ori_jugador.y = 0;
            hijo_der.pos_jugador.x = nodo_actual.pos_jugador.x + 1;
            hijo_der.pos_jugador.y = nodo_actual.pos_jugador.y;
            expande = true;

            if(0 <= hijo_der.pos_jugador.x && hijo_der.pos_jugador.x < mapa_visitables.length 
            && 0 <= hijo_der.pos_jugador.y && hijo_der.pos_jugador.y < mapa_visitables[0].length)
            if(!mapa_visitables[(int) hijo_der.pos_jugador.x][(int) hijo_der.pos_jugador.y]){
                expande = false;
            }else if(!hijo_der.ori_jugador.equals(nodo_actual.ori_jugador)){
                hijo_der.accion_giro = Types.ACTIONS.ACTION_RIGHT;
                hijo_der.coste += 1;
            }

            if(!cerrados.contains(hijo_der) && expande){
                abiertos.add(hijo_der);
                //System.out.printf("(%d,%d) (%d,%d)-> c: %d; h: %d = %d\n", (int) hijo_der.pos_jugador.x, (int) hijo_der.pos_jugador.y, (int) hijo_der.ori_jugador.x, (int) hijo_der.ori_jugador.y, hijo_der.coste, hijo_der.h, hijo_der.coste + hijo_der.h);
            }

            if(!abiertos.isEmpty()){
                nodo_actual = abiertos.peek();
                while (cerrados.contains(nodo_actual) && !abiertos.isEmpty()){
                    abiertos.poll();
                    if(!abiertos.isEmpty()) nodo_actual = abiertos.peek();
                    
                }
            }
            
        }

        if(Solucion_encontrada){
            while(nodo_actual.padre != null){
                if(nodo_actual.accion_giro != null) acts.push(nodo_actual.accion_giro);
                acts.push(nodo_actual.accion_desde_padre);
                nodo_actual = nodo_actual.padre;
            }
        }
    }
    
}
