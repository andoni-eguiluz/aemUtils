package net.eguiluz.aem.utils;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;

/** Clase para reproducir sonidos
 * Funciona como un hilo, hay que construirlo, y luego se lanza con .start()
 * Si se acaban el resto de threads y se ha creado un objeto de esta clase, el thread pendiente no se acabará
 * hasta llegar al final del sonido, o indefinidamente si se ha dejado pausado. (en ese caso utilizar el método kill()).
 * @author eguiluz
 */
public class ReproductorAudio extends Thread {
	private URL urlFic;
	private Clip sonido;
	private boolean seAcabo = false;
	/**
	 * @param nombre del fichero de audio para poder reproducir el archivo (sobre sistema de ficheros) 
	 */
	public ReproductorAudio(String audio){
		try {
			urlFic = new URL( audio );
		} catch (MalformedURLException e) {
			File posibleFic = new File( audio );
			try {
				if (posibleFic.exists()) urlFic = posibleFic.toURI().toURL();
				else urlFic = null;
			} catch (MalformedURLException e2) {
				urlFic = null;
			}
		}
	}
	/**
	 * @param enlace al audio para poder reproducir el archivo (como recurso o sobre sistema de archivos)  
	 */
	public ReproductorAudio( URL url ){
		urlFic = url;
	}
	public void run() 
	{ 
		try {
			// Se obtiene un sonido
			sonido = AudioSystem.getClip();
			// Se carga con un fichero wav
			sonido.open(AudioSystem.getAudioInputStream( urlFic ));
			// Comienza la reproducción
			sonido.loop(0);
			sonido.start();
			Thread.sleep(500); // Espera medio segundo al principio
			// Espera mientras se esté reproduciendo.
			while (sonido.isActive() || pausa){
				Thread.sleep(1000); // Cada segundo comprueba que se haya acabado
				if (interrupted()) {
					sonido.stop();
					break; // Finaliza el thread
				}
			}
			// Se cierra el flujo del sonido
			seAcabo = true;
			sonido.stop();
			sonido.close();
		} catch(InterruptedException e){ // Cuando se interrumpe al hilo en el sleep
		} catch (Exception e) { //Si no encuentra el archivo a reproducir
			System.err.println( "Fichero no encontrado o no válido: " + urlFic );
			if (sonido!=null) sonido.close();
		}
	}
	
		private boolean pausa = false;
	/** Pausa o reactiva el sonido
	 */
	public void playPause() {
		if (sonido != null) {
			if (!pausa) {
				sonido.stop();
				pausa = true;
			} else {
				sonido.loop(0);
				sonido.start();
				pausa = false;
			}
		}
	}
	
	/** Informa de si el sonido está sonando o no
	 * @return	true si está sonando, false en caso contrario
	 */
	public boolean isPlaying() {
		return (!pausa && sonido!=null && sonido.isRunning());
	}
	
	/** Informa de si el sonido ha acabado de sonar
	 * @return	true si ha acabado de sonar, false en caso contrario
	 */
	public boolean isFinished() {
		if (sonido==null) return false;
		return (!sonido.isOpen() || seAcabo || !(sonido.isActive() || pausa));
	}
	
	/** Acaba el sonido y el thread que lo tocaba
	 */
	public void kill() {
		pausa = false;
		sonido.stop();
		sonido.close();
		seAcabo = true;
	}
	
	/** Prueba de audio
	 * @param s
	 */
	public static void main( String[] s ) {
		// Acceso como fichero directo
		ReproductorAudio ra = new ReproductorAudio( "src/net/eguiluz/aem/utils/recursos/musica.wav" );
		ra.start();
		try { Thread.sleep(3000); } catch (Exception e) {}
		ra.kill();
		// Acceso como fichero sintaxis URL
		ra = new ReproductorAudio( "file:///D:/desarrollo/Andoni/aemUtils/src/net/eguiluz/aem/utils/recursos/musica.wav" );
		ra.start();
		try { Thread.sleep(3000); } catch (Exception e) {}
		ra.kill();
		// Acceso como recurso URL
		ra = new ReproductorAudio( ReproductorAudio.class.getResource( "recursos/musica.wav" ) );
		ra.start();
		try { Thread.sleep(5000); } catch (Exception e) {}
		ra.kill();
	}
	
}
