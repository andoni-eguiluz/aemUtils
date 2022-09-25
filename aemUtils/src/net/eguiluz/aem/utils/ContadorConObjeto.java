package net.eguiluz.aem.utils;

/** Clase para gestionar contadores con objetos asociados
 * @author andoni.eguiluz @ ingenieria.deusto.es
 */
public class ContadorConObjeto {
	
	private Object objeto;
	private int cont = 0;  // Contador entero
	
	/** Inicializa un contador a cero
	 */
	public ContadorConObjeto( Object objeto ) {
		this.objeto = objeto;
	}
	/** Inicializa un contador con el valor indicado
	 * @param valor	Valor de inicio del contador
	 */
	public ContadorConObjeto( Object objeto, int valor ) {
		cont = valor;
		this.objeto = objeto;
	}
	/** Devuelve el valor del contador
	 * @return	Valor actual del contador
	 */
	public int get() { return cont; }
	/** Incrementa el contador
	 */
	public void inc() { cont++; }
	
	public void inc( int inc ) { cont += inc; }
	
	public Object getObjeto() { return objeto; }
	
	@Override
	public String toString() { return "" + cont + "(" + objeto + ")"; }
	
}
