package net.eguiluz.aem.utils;

import java.io.*;
import java.awt.*;
import javax.swing.*;
import javax.swing.text.BadLocationException;

import java.awt.event.*;

/** Utilidad para sacar la consola al IU en una ventana Swing con doble cuadro de texto, para la consola de salida (izquierda) y la de error (derecha)
 * @author andoni.eguiluz @ ingenieria.deusto.es
 */
public class IUConsola {
	
	private static MiJTextArea taOut = new MiJTextArea( 800 );  // Textarea para el out
	private static MiJTextArea taErr = new MiJTextArea( 800 );  // Textarea para el err
	private static VentanaIUConsola ventana;  // Ventana para las textareas

		private static Runnable rLanzarIU = new Runnable() {
			@Override
			public void run() {
				ventana = new VentanaIUConsola();
				ventana.setVisible( true );
			}
		};

	/** Atrapa la salida a consola estándar del sistema (out y err) y lanza una ventana con doble panel para visualizarlas en la IU
	 * @param salidaOut	Stream de salida adicional al que redirigir out, por ejemplo un fichero (null si no se quiere utilizar)
	 * @param salidaErr	Stream de salida adicional al que redirigir err, por ejemplo un fichero (null si no se quiere utilizar)
	 * @param numLineas	(Opcional) número de líneas máximo de visualización en los cuadros de texto (debe ser mayor que 100)
	 * @return	true si el proceso se hace correctamente, false en caso contrario. 
	 */
	public static boolean lanzarConsolaEnIU( OutputStream salidaOut, OutputStream salidaErr, int... numLineas ) {
		if (numLineas.length>0 && numLineas[0]>100) {
			taOut.setLimLineas( numLineas[0] );
			taErr.setLimLineas( numLineas[0] );
		}
		try {
			if (SwingUtilities.isEventDispatchThread()) {
				rLanzarIU.run();
			} else {
				SwingUtilities.invokeAndWait( rLanzarIU );
			}
		} catch (Exception e) {  // No se ha podido crear la ventana
			return false;
		}
		try {
			System.setOut( new PrintStream( new MiOutputStream( taOut, System.out, salidaOut ) ) );
			System.setErr( new PrintStream( new MiOutputStream( taErr, System.err, salidaErr ) ) );
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			taErr.append( e.toString() );
			return false;
		}
	}
	
	/** Cierra la ventana de salida de consola
	 */
	public static void cerrarConsolaEnIU() {
		if (ventana!=null) ventana.dispose();
	}

	/** Main de prueba
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			int numPrueba = 1;
			if (numPrueba==1) {
				lanzarConsolaEnIU( new FileOutputStream( "test_out.txt" ), new FileOutputStream( "test_err.txt" ) );
				System.out.println( "Prueba consola salida" );
				for (int i=0; i<1000; i++)
					System.out.println( "Prueba números consola salida " + i );
				System.err.println( "Prueba consola error" );
				System.err.println( "Prueba consola error 2" );
			} else if (numPrueba==2) {
				lanzarConsolaEnIU( null, null );
				System.out.println( "Prueba tilde: áéíóúñÑÁÉÍÓÚ" );
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
	@SuppressWarnings("serial")
	private static class VentanaIUConsola extends JFrame {
		public VentanaIUConsola() {
			setTitle( "Consola de Java" );
			setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
			JSplitPane p = new JSplitPane();
			setContentPane( p );
			p.setLeftComponent( new JScrollPane( taOut ) );
			p.setRightComponent( new JScrollPane( taErr ) );
			p.setDividerLocation( 0.5 );
			taErr.setForeground( Color.red );
			addWindowListener( new WindowAdapter() {
				@Override
				public void windowClosed(WindowEvent e) {
					if (ventana!=null) ventana = null;
				}
			});
			pack();
		}
	}
	
	@SuppressWarnings("serial")
	private static class MiJTextArea extends JTextArea {
		private int limLineas = 10000;
		/** Crea una JTextArea con límite de líneas
		 * @param limiteLineas	Número de líneas límite (cada vez que se alcance ese umbral, se corta la primera mitad de las líneas)
		 */
		public MiJTextArea( int limiteLineas ) {
			super( 20, 60 );
			this.limLineas = limiteLineas;
		}
		public void setLimLineas( int limiteLineas ) {
			this.limLineas = limiteLineas;
		}
		@Override
		public void append(String str) {
			try {
				boolean ponerAlFinal = getLineCount()<=3 || getSelectionStart()>getLineEndOffset( getLineCount()-3 );
				super.append(str);
				if (getLineCount() > limLineas) {
					setSelectionStart(0);
					setSelectionEnd( getLineEndOffset( limLineas/2 ) );
					replaceSelection( "" );
					setSelectionStart( getText().length() );
					setSelectionEnd( getText().length() );
				}
				if (ponerAlFinal) { // Posiciona el caret al final del cuadro de texto, excepto si el usuario lo mueve explícitamente antes (en ese caso se mantiene)
					setSelectionStart( getText().length() );  
					setSelectionEnd( getText().length() );
				}
			} catch (BadLocationException e) {}
		}
	}
	
	/** Nuevo stream de salida que saca a varios streams (indicados) y a la textArea indicada
	 */
	private static class MiOutputStream extends OutputStream
	{
		private OutputStream[] outputStreams;
		private MiJTextArea textArea; 
		
		public MiOutputStream(MiJTextArea textArea, OutputStream... outputStreams) {
			this.outputStreams= outputStreams; 
			this.textArea = textArea;
		}
		
		@Override
		public void write(int b) throws IOException {
			for (OutputStream out: outputStreams)
				if (out!=null) out.write(b);
			if (ventana!=null) textArea.append( b+"" );
		}
		
		@Override
		public void write(byte[] b) throws IOException {
			for (OutputStream out: outputStreams)
				if (out!=null) out.write(b);
			if (ventana!=null) textArea.append( new String( b ) );
		}

		@Override
		public void write(byte[] b, int off, int len) throws IOException {
			for (OutputStream out: outputStreams)
				if (out!=null) out.write(b, off, len);
			if (ventana!=null) textArea.append( new String( b, off, len ) );
		}

		@Override
		public void flush() throws IOException {
			for (OutputStream out: outputStreams)
				if (out!=null) out.flush();
		}

		@Override
		public void close() throws IOException {
			for (OutputStream out: outputStreams)
				if (out!=null) out.close();
			if (ventana!=null) { ventana.dispose(); ventana = null; }
		}
	}

}
