package net.eguiluz.aem.utils;

/** Clase que permite crear objetos temporizadores
 * @author eguiluz
 */
public class Temporizador {
	private Thread miThread = null;
	private long milisegundosFaltan;
	private long ultimaActualizacion;
	private boolean pausa = false;
	private Runnable miRunnable = null;
	private long milisegundosSgteConteo;
	private Runnable miTick = null;
	private long milisTick;
	public Temporizador() {
	}
	/** Inicia el temporizador
	 * @param milisegundos	milisegundos que cuenta el temporizador
	 * @param aEjecutarAlAcabar	objeto cuyo m�todo run() se lanza al acabar el temporizador 
	 */
	public void iniciar( long milisegundos, Runnable aEjecutarAlAcabar ) {
		iniciar( milisegundos, aEjecutarAlAcabar, false, 0, null, 0 );
	}
	/** Inicia el temporizador
	 * @param milisegundos	milisegundos que cuenta el temporizador
	 * @param aEjecutarAlAcabar	objeto cuyo m�todo run() se lanza al acabar el temporizador 
	 * @param empiezaConPausa	true si se inicia el temporizador en pausa, false en caso contrario 
	 * @param reinicioTrasFinal	si el valor es <= 0, no se reinicia tras acabar la temporizaci�n 
	 * (el temporizador se acaba). Si es >0, se reinicia el temporizador con ese n�mero de milisegundos
	 * @param aEjecutarPorTick	objeto cuyo m�todo run() se lanza en cada tick. Si es null no se lanza
	 * @param tiempoDeCadaTick	milisegundos de cada tick. Si <= 0 no se procesan los ticks.
	 */
	public void iniciar( long milisegundos, Runnable aEjecutarAlAcabar, boolean empiezaConPausa,
			long reinicioTrasFinal, Runnable aEjecutarPorTick, long tiempoDeCadaTick ) {
		miThread = new MiHilo();
		milisegundosFaltan = milisegundos;
		miRunnable = aEjecutarAlAcabar;
		ultimaActualizacion = System.currentTimeMillis();
		pausa = empiezaConPausa;
		milisegundosSgteConteo = reinicioTrasFinal;
		miTick = aEjecutarPorTick; if (tiempoDeCadaTick<=0) miTick = null;
		milisTick = tiempoDeCadaTick; if (miTick == null) milisTick = 0;
		miThread.start();
	}
	/** Reprograma el temporizador para que desde ahora empiece el conteo
	 * @param milisegundos	Milisegundos del conteo del temporizador
	 * Si es <= 0 o el temporizador ya se hab�a acabado, no se tiene en cuenta
	 */
	public void setNuevoTiempo( long milisegundos ) {
		actualizarTemp();
		if (milisegundos > 0 && milisegundosFaltan > 0) {
			milisegundosFaltan = milisegundos;
		}
	}
	/** Programa el temporizador para que despu�s de acabar empiece otra vez el conteo
	 * @param milisegundos	Milisegundos del siguiente conteo (con el que se reiniciar� el temporizador).
	 * Si es <= 0, es que no habr� conteo posterior.
	 */
	public void setSgteTiempo( long milisegundos ) {
		milisegundosSgteConteo = milisegundos;
	}
	/** Acaba el temporizador sin ejecutar ning�n m�todo que hubiera pendiente
	 */
	public void acabar() {
		miThread.interrupt();
	}
	/** Pausa el temporizador si estaba activo, o lo reactiva si est� pausado
	 */
	public void pausa() {
		actualizarTemp();
		pausa = !pausa;
	}
	/** Devuelve el tiempo que falta para que acabe el temporizador
	 * @return	milisegundos que faltan para que acabe el temporizador
	 */
	public long getTiempo() {
		actualizarTemp();
		return milisegundosFaltan;
	}
	/** Devuelve el tiempo que falta para que acabe el temporizador
	 * @param formato	Formato en el que se quiere recuperar el temporizador: "H" (h:mm:ss), "M" (m:ss), "S" (s)
	 * @return	tiempo que falta para que acabe el temporizador (formateado dependiendo del formato indicado)
	 */
	public String getTiempo( String formato ) {
		actualizarTemp();
		String ret = "";
		long segs = Math.round( milisegundosFaltan / 1000D );
		long mins = 0;
		long hors = 0;
		if ("M".equalsIgnoreCase(formato) || "H".equalsIgnoreCase(formato)) {
			mins = segs / 60;
			segs = segs % 60;
			if ("H".equalsIgnoreCase(formato))
				ret = String.format( "%1$02d", mins%60) + ":";
			else
				ret = mins + ":";
		}
		if ("H".equalsIgnoreCase(formato)) {
			hors = mins / 60;
			mins = mins % 60;
			ret = hors + ":" + ret;
		}
		if ("M".equalsIgnoreCase(formato) || "H".equalsIgnoreCase(formato))
			return ret + String.format( "%1$02d", segs );
		else
			return ret + segs;
				
	}
	/** Indica si el temporizador est� pausado
	 * @return	true si est� pausado, false si est� activo
	 */
	public boolean isEnPausa() {
		return pausa;
	}
	private void actualizarTemp() {
		if (!pausa) {
			long milisHanPasado = System.currentTimeMillis() - ultimaActualizacion;
			ultimaActualizacion = System.currentTimeMillis();
			milisegundosFaltan -= milisHanPasado;
		} else {
			ultimaActualizacion = System.currentTimeMillis();
		}
	}
	
	/** M�todo de prueba
	 * @param s	no utilizado
	 */
	public static void main( String[] s ) {
		boolean testSencillo = false;
		if (testSencillo) {
			// Test sencillo
			Temporizador t = new Temporizador();
			t.iniciar( 10000, null );
			try {
				System.out.println( "Inicio con 10000 -> " + t.getTiempo() );
				Thread.sleep( 500 );
				System.out.println( "Valor actual -> " + t.getTiempo() );
				t.pausa();
				System.out.println( "Pausa! -> " + t.getTiempo() );
				Thread.sleep( 4000 );
				System.out.println( "Valor actual -> " + t.getTiempo() );
				t.pausa();
				System.out.println( "Quitar pausa! -> " + t.getTiempo() );
				Thread.sleep( 500 );
				System.out.println( "Valor actual -> " + t.getTiempo() );
				Thread.sleep( 500 );
				System.out.println( "Valor actual -> " + t.getTiempo() );
				Thread.sleep( 500 );
				System.out.println( "Valor actual -> " + t.getTiempo() );
				t.acabar();
				System.out.println( "Timer matado." );
			} catch (Exception e) {
			}
		} else {
			// Test m�s elaborado
			Temporizador t = new Temporizador();
			Runnable fin = new Runnable() {
				@Override
				public void run() {
					System.out.println( "FIN!!" );
				}
			};
			Runnable tick = new Runnable() {
				@Override
				public void run() {
					System.out.println( "tick " );
				}
			};
			t.iniciar( 8000, fin, true, 5000, tick, 1000 );  
			try {
				System.out.println( "Inicio con 8000 -> " + t.getTiempo("H") );
				Thread.sleep( 2000 );
				System.out.println( "Valor actual -> " + t.getTiempo() );
				t.pausa();
				System.out.println( "Quitamos pausa! -> " + t.getTiempo("M") );
				Thread.sleep( 1500 );
				System.out.println( "Valor actual -> " + t.getTiempo("S") );
				Thread.sleep( 2500 );
				System.out.println( "Valor actual -> " + t.getTiempo(null) );
				t.pausa();
				System.out.println( "Poner pausa! -> " + t.getTiempo("H") );
				Thread.sleep( 5000 );
				System.out.println( "Valor actual -> " + t.getTiempo("H") );
				t.pausa();
				System.out.println( "Quitar pausa! -> " + t.getTiempo("H") );
				Thread.sleep( 500 );
				System.out.println( "Valor actual -> " + t.getTiempo("H") );
				Thread.sleep( 500 );
				t.setNuevoTiempo( 7000 );
				System.out.println( "Reponer el temporizador a 7000 -> " + t.getTiempo("H") );
				Thread.sleep( 500 );
				System.out.println( "Valor actual -> " + t.getTiempo("H") );
				Thread.sleep( 500 );
				System.out.println( "Valor actual -> " + t.getTiempo("H") );
				System.out.println( "A ver qu� hace el timer..." );
				Thread.sleep(8000); // Con el segundo timer ponemos el siguiente a 2 segs
				t.setSgteTiempo( 2000 );
				// y ya no m�s
			} catch (Exception e) {
			}
		}
	}
	
	class MiHilo extends Thread {
		@Override
		public void run() {
			try {
				boolean seAcabo;
				do {
					seAcabo = true;
					while (milisegundosFaltan > 0) {
							if (miTick==null) {
								Thread.sleep( milisegundosFaltan ); // Espera al menos los milis que faltan
							} else {
								Thread.sleep( Math.min(milisegundosFaltan,milisTick) );
								if (milisTick < milisegundosFaltan) {
									miTick.run();  // Producir tick
								}
							}
						actualizarTemp();
					}
					if (milisegundosFaltan != Long.MIN_VALUE) {  // Si se ha interrumpido, entonces no se hace nada
						if (milisegundosSgteConteo > 0) {  // Reiniciar el conteo
							seAcabo = false;
							milisegundosFaltan = milisegundosSgteConteo;
							milisegundosSgteConteo = 0; // El siguiente no se reinicia por defecto
						}
						if (miRunnable!=null) miRunnable.run();  // Ejecutar el final del temporizador
					}
				} while (!seAcabo);
			} catch (InterruptedException e) {
				milisegundosFaltan = Long.MIN_VALUE;  // Corta
			}
		}
	}
}