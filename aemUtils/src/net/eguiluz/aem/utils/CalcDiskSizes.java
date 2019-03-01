package net.eguiluz.aem.utils;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.OutputStream;
import java.io.PrintStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JSpinner;
import javax.swing.filechooser.FileSystemView;

public class CalcDiskSizes {

			private static DecimalFormat numFormat = new DecimalFormat("#.#");  // con "#.0" sale siempre un decimal aunque sea 0
			private static String lastString = "";
			private static int nivelMax;
			private static long fechaRef = 0;  // Si positivo o cero, coge ficheros a partir de esa fecha. Si negativo, hasta esa -fecha.
			
			private static boolean SACA_EVOLUCION = true;
			static String SEPARADOR = "\t";
			
		// f debe ser un directorio existente
		private static InfoCarpeta processPath( File inic, int nivel, double porcInicial, double porcTotalCarp ) {
			InfoCarpeta inicial = new InfoCarpeta( inic, nivel );
			if (miVentana==null || miVentana.isRunning()) {
				// System.out.println( inic );
	            File[] children = inic.listFiles();
	            double num=0;
	            if (children != null) {
		            for (File ficheroODir : children) {
		            	if (miVentana != null && !miVentana.isRunning()) break;
		            	if (SACA_EVOLUCION && miVentana != null) ponMensaje( ficheroODir.getAbsolutePath() );
		            	if (ficheroODir.isDirectory()) {
		                	InfoCarpeta infoPath = processPath( ficheroODir, nivel+1, porcInicial + porcTotalCarp*(num/children.length), (porcTotalCarp/children.length) );
		                	inicial.addSubcarpeta( infoPath );
		            	} else if (ficheroODir.isFile()) {
		            		if (ficheroODir.lastModified() > System.currentTimeMillis()) {
		            			ponAlarma( "ERROR EN FECHA (posterior a hoy) en fichero:\n  " + ficheroODir.getAbsolutePath() );
		            		}
							// Si positivo, coge ficheros a partir de esa fecha. Si negativo, hasta esa -fecha. Si 0, no hace nada
		            		if (fechaRef==0 ||
		            				(fechaRef>0 && ficheroODir.lastModified() >= fechaRef) ||
		            				(fechaRef<0 && ficheroODir.lastModified() <= -fechaRef)) {
			            		inicial.bytes += ficheroODir.length();
			            		inicial.numFics++;
			            		if (ficheroODir.lastModified() > inicial.ultFechaModifFichero) 
			            			inicial.ultFechaModifFichero = ficheroODir.lastModified();
		            		}
		            	}
		            	num = num + 1;
		            	if (SACA_EVOLUCION) {
		            		for (int borrarCar=0; borrarCar < lastString.length(); borrarCar++ ) ponEvolucion( "\b" );
		            		double evolPorc = 100*(porcInicial + porcTotalCarp*(num/children.length));
			            	lastString = numFormat.format( evolPorc ) + "%  ";
			            	ponEvolucionPorc( lastString, evolPorc );
		            	}
		            }
		            inicial.ordena();
	            }
			}
            return inicial;
		}

	public static InfoCarpeta calcSizes( File inicio ) {
		InfoCarpeta total = null;
        if ( inicio.exists() && inicio.isDirectory() ) {
        	ponEvolucion( "Realizando recorrido... " );
        	total = processPath( inicio, 0, 0.0, 1.0 );
        	ponEvolucion( "\n" );
        	ponEvolucion( "Proceso finalizado." );
        } else {
        	ponMensaje( "Error: " + inicio.getName() + " no es un directorio válido." );
        }
        return total;
	}
	
	private static void ponAlarma( String mens ) {
		if (miVentana == null) 
			System.out.println( mens );
		else
			JOptionPane.showMessageDialog( miVentana, mens, "¡Atención!", JOptionPane.ERROR_MESSAGE );
			miVentana.lMensaje.setText( mens );
	}
	
	private static void ponMensaje( String mens ) {
		if (miVentana == null) 
			System.out.println( mens );
		else
			miVentana.lMensaje.setText( mens );
	}

	private static void ponEvolucion( String mens ) {
		if (SACA_EVOLUCION) {
			if (miVentana == null) {
				System.err.print( mens );
			} else {
				miVentana.lMensaje.setText( mens.toString() );
			}
		}
	}
	
	private static void ponEvolucionPorc( String enString, double numerico ) {
		if (SACA_EVOLUCION) {
			if (miVentana == null) {
				System.err.print( enString );
			} else {
				miVentana.pbProgreso.setValue( (int) (numerico*100) );  // de 0 a 100 --> 0 a 10000
			}
		}
	}

		private static VentCalcDiskSizes miVentana = null;
	public static void main( String s[] ) {
		InfoCarpeta recorrido = null;
		if (s.length>0) {
			nivelMax = 9999;
			if (s.length>1) {
				try {
					nivelMax = Integer.parseInt( s[1] );
				} catch (NumberFormatException e) {}
				if (s.length>2) {
					try {
						fechaRef = Integer.parseInt( s[2] );
						// Si positivo, coge ficheros a partir de esa fecha. Si negativo, hasta esa -fecha. Si 0, no hace nada
						if (fechaRef > 0) {
							fechaRef = fechaRef*24*60*60*1000;  // Días a milisegundos
							fechaRef = System.currentTimeMillis() - fechaRef;
						} else {
							fechaRef = -fechaRef*24*60*60*1000;  // Días a milisegundos
							fechaRef = System.currentTimeMillis() - fechaRef;
							fechaRef = -fechaRef;  // lo deja negativo
						}
					} catch (NumberFormatException e) {
						
					}
				}
			}
			recorrido = calcSizes( new File(s[0]) );
	    	recorrido.visualiza( nivelMax );
		} else {
			System.out.println( "Debe indicarse el path cuyo tamaño calcular. Opcionalmente puede indicarse el nivel máximo de carpetas a visualizar. Y después el número de días de antigüedad de los ficheros a considerar (si positivo, los más modernos, si negativo los más antiguos)");
			System.out.println( "Lanzando versión interactiva..." );
			JFileChooser jfc = new JFileChooser();
			jfc.setVisible( true );
			File raiz = FileSystemView.getFileSystemView().getHomeDirectory();
				// o también raiz = FileSystemView.getFileSystemView().getRoots()[0];
			raiz = raiz.listFiles()[0];  // Coge el primero del home directory, en windows es "mi equipo"
			jfc.setCurrentDirectory( raiz );
		    jfc.setDialogTitle( "Selecciona el path cuyos tamaños calcular:" );
		    jfc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
		    jfc.setAcceptAllFileFilterUsed( false );
		    if (jfc.showOpenDialog( null ) == JFileChooser.APPROVE_OPTION) {
				try {
					nivelMax = Integer.parseInt( JOptionPane.showInputDialog( null, 
							"Introduce el número de niveles a procesar:", "3" ) );
					fechaRef = Integer.parseInt( JOptionPane.showInputDialog( null, 
							"Introduce el número de días de antigüedad a mirar (0=no mirar,+=modernos,-=antiguos): ", "0" ) );
						// Si positivo, coge ficheros a partir de esa fecha. Si negativo, hasta esa -fecha. Si 0, no hace nada
						if (fechaRef > 0) {
							fechaRef = fechaRef*24*60*60*1000;  // Días a milisegundos
							fechaRef = System.currentTimeMillis() - fechaRef;
						} else {
							fechaRef = -fechaRef*24*60*60*1000;  // Días a milisegundos
							fechaRef = System.currentTimeMillis() - fechaRef;
							fechaRef = -fechaRef;  // lo deja negativo
						}
			    	miVentana = new VentCalcDiskSizes();
			    	miVentana.lTitulo.setText( "Calculando directorio " + jfc.getSelectedFile().getAbsolutePath() );
			    	miVentana.setVisible( true );
			    	recorrido = calcSizes( jfc.getSelectedFile() );
	            	if (miVentana != null && !miVentana.isRunning()) return;  // Acaba si corte del usuario
			    	File ficheroDatos = new File("aeCalcDiskSizes.csv");
			    	recorrido.sacaAFichero( nivelMax, ficheroDatos );
			    	miVentana.lMensaje.setText( "Abriendo excel con fichero " + ficheroDatos.getAbsolutePath() );
			    	try {
				    	try {
				    		// Process p = Runtime.getRuntime().exec( "excel " + ficheroDatos.getAbsolutePath() );
				    		// ProcessBuilder pb = new ProcessBuilder("cmd", "/c", "excel");
				    		// Process p = pb.start();	    
				    		Desktop.getDesktop().open( ficheroDatos );
				    		Thread.sleep( 5000 );
				    		miVentana.dispose();  // Cierra
				    	} catch (Exception e) {
				    		e.printStackTrace();
				    	}
			    	} catch (Exception e) {
			    		e.printStackTrace();
			    	}
			    	
				} catch (NumberFormatException e) {}
		  	}
		}
	}
	
}

class VentCalcDiskSizes extends JFrame {
	JLabel lMensaje = new JLabel( " " );
	JLabel lTitulo = new JLabel( " " );
	JProgressBar pbProgreso = new JProgressBar(0, 10000);
	boolean running = true;
	public VentCalcDiskSizes() {
		setTitle( "aeCalcDiskSizes - calcula tamaño de carpetas en disco" ); 
		setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		getContentPane().add( lTitulo, BorderLayout.NORTH );
		getContentPane().add( pbProgreso, BorderLayout.CENTER );
		getContentPane().add( lMensaje, BorderLayout.NORTH );
		setSize( 700, 100 );
		setLocationRelativeTo( null );
		this.addWindowListener( new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				running = false;
				JOptionPane.showMessageDialog( VentCalcDiskSizes.this, "Parando aplicación" );
			}
		});
	}
	public boolean isRunning() {
		return running;
	}
}


/** Información de carpetas
 * @author AEM
 */
class InfoCarpeta implements Comparable<InfoCarpeta> {
	long bytes = 0;
	long numFics = 0;
	long numCarps = 0;
	long numFicsDentro = 0;
	long numCarpsDentro = 0;
	long ultFechaModifFichero = 0;
	int nivel = 0;
	File carpeta = null;
	ArrayList<InfoCarpeta> subCarpetas = new ArrayList<InfoCarpeta>();
	
	/** Crea una carpeta de información con sus subcarpetas
	 * @param carpeta	Datos del nombre de la carpeta
	 * @param subCarpetas	Lista de subcarpetas ya creadas y con datos
	 * @param nivel	Nivel de la carpeta
	 */
	public InfoCarpeta( File carpeta, ArrayList<InfoCarpeta> subCarpetas, int nivel ) {
		this.subCarpetas = subCarpetas;
		this.carpeta = carpeta;
		this.nivel = nivel;
		for ( InfoCarpeta c : subCarpetas ) {
			bytes += c.bytes;
			numFicsDentro += c.numFicsDentro;
			numCarps++;
			numCarpsDentro = numCarpsDentro + c.numCarpsDentro + 1;
		}
	}
	
	/** Crea la información de carpeta con solo el nombre
	 * @param carpeta	Datos del nombre de la carpeta
	 * @param nivel	Nivel de la carpeta
	 */
	public InfoCarpeta( File carpeta, int nivel ) {
		this.carpeta = carpeta;
		this.nivel = nivel;
	}
	
	public long getNumTotalFicheros() {
		return numFics + numFicsDentro;
	}
	
	public void addSubcarpeta( InfoCarpeta ic ) {
		subCarpetas.add( ic );
		bytes += ic.bytes;
		numFicsDentro += ic.numFicsDentro + ic.numFics;
		numCarps++;
		numCarpsDentro = numCarpsDentro + ic.numCarpsDentro + 1;
		if (ic.ultFechaModifFichero > ultFechaModifFichero) ultFechaModifFichero = ic.ultFechaModifFichero;
	}

		private static SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
	public String getFechaUltModif() {
		return sdf.format( ultFechaModifFichero );	
	}
	
	
	public void ordena() {
		subCarpetas.sort( null );
	}
	
		private void visualiza( InfoCarpeta ic, int nivel, int nivelMaximo, PrintStream os ) {
			for (int i=0; i<nivel; i++) os.print( "   " );
			os.println( ic );
			if (nivel < nivelMaximo)
				for (InfoCarpeta subCarp : ic.subCarpetas) {
					visualiza( subCarp, nivel+1, nivelMaximo, os );
				}
		}
		
	public void visualiza( int nivelMaximo ) {
    	System.out.println( "Nombre" + CalcDiskSizes.SEPARADOR + "Nivel" + CalcDiskSizes.SEPARADOR + "Tamaño(Kb)"
    			+ CalcDiskSizes.SEPARADOR + "Num.fics" + CalcDiskSizes.SEPARADOR + "Num.carps"
    			+ CalcDiskSizes.SEPARADOR + "Ult.fec.modif." + CalcDiskSizes.SEPARADOR + "Path completo" );
		visualiza( this, 0, nivelMaximo, System.out );
	}
	
	public void sacaAFichero( int nivelMaximo, File f ) {
		try {
			CalcDiskSizes.SEPARADOR = ";";
			PrintStream ps = new PrintStream( f );
			ps.println( "Nombre" + CalcDiskSizes.SEPARADOR + "Nivel" + CalcDiskSizes.SEPARADOR + "Tamaño(Kb)"
					+ CalcDiskSizes.SEPARADOR + "Num.fics" + CalcDiskSizes.SEPARADOR + "Num.carps" + CalcDiskSizes.SEPARADOR +
					"Ult.fec.modif." + CalcDiskSizes.SEPARADOR + "Path completo" );
			visualiza( this, 0, nivelMaximo, ps );
			ps.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
	}
	
		// Formateador con puntos y comas en castellano
		private static DecimalFormat numFormat = new DecimalFormat("#,###.##");
	@Override
	public String toString() {
		String ks = numFormat.format( bytes/1024.0 );
    	return carpeta.getName() + CalcDiskSizes.SEPARADOR + nivel + CalcDiskSizes.SEPARADOR + ks + CalcDiskSizes.SEPARADOR + (numFicsDentro+numFics) 
    			+ CalcDiskSizes.SEPARADOR + numCarpsDentro + CalcDiskSizes.SEPARADOR + getFechaUltModif() + CalcDiskSizes.SEPARADOR + carpeta.getAbsolutePath();
	}

	@Override
	public int compareTo(InfoCarpeta o) {
		if (bytes < o.bytes) return +1;
		else if (bytes > o.bytes) return -1;
		else return 0;
	}
		
}