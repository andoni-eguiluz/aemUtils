package net.eguiluz.aem.utils;

import java.util.ArrayList;

/** Clase que permite crear máquinas de estados
 * @author eguiluz
 */
public class MaquinaEstados {
	private ArrayList<String> estados;
	private ArrayList<Object> estadosContenidos;
	private ArrayList<TransicionEstado> transiciones;
	private ArrayList<String> condiciones;
	private String estadoActual;
	/** Construye una nueva máquina de estados, vacía
	 */
	public MaquinaEstados() {
		estados = new ArrayList<String>();
		estadosContenidos = new ArrayList<Object>();
		transiciones = new ArrayList<TransicionEstado>();
		condiciones = new ArrayList<String>();
		estadoActual = null;
	}
	/** Añade un estado nuevo a la máquina de estados
	 * @param estado	Estado a añadir. Si ya existe un estado con el mismo nombre, no se añade.
	 * @param contenido	Contenido del estado.
	 */
	public void anyadirEstado( String estado, Object contenido ) {
		if (!estados.contains(estado)) {
			estados.add( estado );
			estadosContenidos.add( contenido );
		}
	}
	/** Consulta si un estado está o no en la máquina de estados
	 * @param estado	Estado a consultar
	 * @return	true si está incluido, false en caso contrario
	 */
	public boolean isEstadoCorrecto( String estado ) {
		return (estados.contains(estado));
	}
	/** Añade una transición nueva a la máquina de estados. Si ya existía esa misma transición, no se añade. 
	 * Si alguno de los parámetros es null, no se añade.
	 * @param estadoDe	Estado de origen. Si no existe, se añade a los estados de la máquina (con contenido null).
	 * @param estadoA	Estado de destino. Si no existe, se añade a los estados de la máquina (con contenido null).
	 * @param condTransicion	Condición por la que se hace el tránsito entre los estados. Si no existía, 
	 * se añade la condición a las disponibles en la máquina. 
	 */
	public void anyadirTransicion( String estadoDe, String estadoA, String condTransicion ) {
		if (estadoDe==null || estadoA==null || condTransicion==null) return;
		if (!estados.contains(estadoDe)) { estados.add( estadoDe ); estadosContenidos.add( null ); }
		if (!estados.contains(estadoA)) { estados.add( estadoA ); estadosContenidos.add( null ); }
		if (!condiciones.contains(condTransicion)) condiciones.add( condTransicion );
		TransicionEstado te = new TransicionEstado( estadoDe, estadoA, condTransicion );
		if (!transiciones.contains(te)) transiciones.add( te );
	}
	/** Inicia la máquina de estados
	 * @param estado	Estado inicial. Si no existe, la máquina de estados no se inicia.
	 */
	public void iniciar( String estado ) {
		if (estados.contains(estado)) estadoActual = estado; else estadoActual = null;
	}
	/** Avanza la máquina de estados.
	 * @param condTransicion	Condición para que la máquina avance. Si no existe la transición, se mantiene como estaba.
	 * @return	true si la máquina ha avanzado por esa condición, false si no ha avanzado.
	 */
	public boolean avanzar( String condTransicion ) {
		for (TransicionEstado te : transiciones) {
			if (te.getEstadoInicial().equals(estadoActual)) {
				if (te.getCondicion().equals(condTransicion)) {
					estadoActual = te.getEstadoFinal();
					return true;
				}
			}
		}
		return false;
	}
	/** Devuelve el estado actual, null si la máquina no ha sido iniciada.
	 * @return	Estado en curso
	 */
	public String getEstadoActual() {
		return estadoActual;
	}
	/** Devuelve el contenido del estado actual, null si la máquina no ha sido iniciada.
	 * @return	Contenido del estado en curso
	 */
	public Object getContenidoDeEstadoActual() {
		if (estadoActual == null) return null;
		return estadosContenidos.get( estados.indexOf(estadoActual) );
	}
	/** Devuelve el ArrayList de estados de la máquina de estados
	 * @return	Lista de estados de la máquina
	 */
	public ArrayList<String> getEstados() {
		return estados;
	}
	/** Devuelve el ArrayList de transiciones de la máquina de estados
	 * @return	Lista de estados de la máquina
	 */
	public ArrayList<TransicionEstado> getTransiciones() {
		return transiciones;
	}
}

class TransicionEstado {
	private String estadoDesde;
	private String estadoHasta;
	private String condicion;
	public TransicionEstado( String desde, String hasta, String condicion ) {
		estadoDesde = desde;
		estadoHasta = hasta;
		this.condicion = condicion;
	}
	public String getCondicion() {
		return condicion;
	}
	public String getEstadoInicial() {
		return estadoDesde;
	}
	public String getEstadoFinal() {
		return estadoHasta;
	}
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof TransicionEstado) {
			TransicionEstado te2 = (TransicionEstado) obj;
			if (estadoDesde==null || estadoHasta==null || condicion==null) return false;
			return (estadoDesde.equals(te2.estadoDesde) && estadoHasta.equals(te2.estadoHasta) && condicion.equals(te2.condicion)); 
		} else {
			return false;
		}
	}
	@Override
	public String toString() {
		return "{" + estadoDesde + "->(" + condicion + ")->" + estadoHasta + "}";
	}
}
