package net.eguiluz.aem.utils;
import java.awt.Window;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InvalidClassException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.TreeMap;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

import javax.swing.JDialog;
import javax.swing.JOptionPane;

import net.eguiluz.aem.utils.TipoCopia;
import net.eguiluz.aem.utils.gui.DPFTipoArbol;
import net.eguiluz.aem.utils.gui.DirProcessFrame;

/**
 *
 * @author andoni
 */
public class DirProcess {
    static final String DELETED_FILES = "DELETED_FILES";
    static final String UPDATED_FILES = "UPDATED_FILES";
    static final long BIG_SIZE = 20 * 1024 * 1024;   // 50 Mb, file big to take time copying (console warning)

    /** Procesa el fichero o directorio f, de acuerdo al código indicado:<p>
     * 0 - No hace nada
     * Redefine action and codes in subclasses
     * @param f     Fichero o directorio sobre el que hay que hacer el proceso
     * @param codProcess    Código del proceso a realizar
     */
    public static void process( File f, int codProcess ) {
        switch (codProcess) {
            case 0: { System.out.println (f.getAbsolutePath() + " - " + f.getName() ); break; }
            default: break;
        }
    }
    /** Procesa el fichero o directorio f, de acuerdo al último código configurado por configureProcess (0 inicialmente)
     * @param f     Fichero o directorio sobre el que hay que hacer el proceso
     */
    public static void process( File f ) {
        process( f, lastConfigurationProcess );
    }
    /** Configure default action to method process
     * @param actionCode
     */
    public static void configureProcess( int actionCode ) {
        lastConfigurationProcess = actionCode;
    }
    static int lastConfigurationProcess = 0;

    /** Visita todos los directorios y ficheros del directorio dir
     * ejecutando sobre ellos el método de proceso process con el código indicado
     * @see process
     * @param dir
     * @param codProcess
     */
    public static void visitAllDirsAndFiles( File dir, int codProcess ) {
        process( dir, codProcess );
        if (dir.isDirectory()) { 
            String[] children = dir.list(); 
            for (int i=0; i<children.length; i++) { 
                visitAllDirsAndFiles( new File(dir, children[i]), codProcess );
            } 
        } 
    } 
    
    /** Visita todos los directorios y ficheros del directorio dir
     * usando el código de proceso configurado con configureProcess
     * @param dir
     */
    public static void visitAllDirsAndFiles( File dir ) {
        visitAllDirsAndFiles( dir, lastConfigurationProcess );
    }

    /** Visita y procesa sólo los directorios bajo el directorio dir
     * @param dir
     * @param codProcess 
     */
    public static void visitAllDirs(File dir, int codProcess ) {
        if (dir.isDirectory()) { 
            process( dir, codProcess );
            String[] children = dir.list(); 
            for (int i=0; i<children.length; i++) { 
                visitAllDirs( new File(dir, children[i]), codProcess );
            } 
        } 
    } 
    
    /** Visita todos los directorios
     * usando el código de proceso configurado con configureProcess
     * @param dir
     */
    public static void visitAllDirs( File dir ) {
        visitAllDirsAndFiles( dir, lastConfigurationProcess );
    }

    /** Visita y procesa sólo los ficheros bajo el directorio dir
     * @param dir
     * @param codProcess 
     */
    public static void visitAllFiles( File dir, int codProcess ) {
        if (dir.isDirectory()) { 
            String[] children = dir.list(); 
            for (int i=0; i<children.length; i++) { 
                visitAllFiles(new File(dir, children[i]), codProcess);
            } 
        } else { 
            process(dir, codProcess);
        } 
    }

    /** Visita y procesa sólo los ficheros bajo el directorio dir
     * usando el código de proceso configurado con configureProcess
     * @param dir
     */
    public static void visitAllFiles( File dir ) {
        visitAllFiles( dir, lastConfigurationProcess );
    }


    /** Copies all files under srcDir to dstDir. If dstDir does not exist, it will be created.
     * @param srcDir
     * @param dstDir
     * @throws IOException
     */
    public void copyDirectory(File srcDir, File dstDir) throws IOException {
        if (srcDir.isDirectory()) {
            if (!dstDir.exists()) {
                dstDir.mkdir();
            }
            String[] children = srcDir.list();
            for (int i=0; i<children.length; i++) {
                copyDirectory(new File(srcDir, children[i]), new File(dstDir, children[i]));
            }
        } else {
            // This method is implemented in Copying a File
            copyFile(srcDir, dstDir);
        }
    }

    /** Makes something if file f exists<p>
     * Redefine action and codes in subclasses
     * @param f
     */
    public static void actionIfFileExist( File f ) {
        switch (lastConfigurationActionIfFileExist) {
            case 0: { break;}
            default: break;
        }
    }
    /** Configure action to method actionIfFileExist
     * @param actionCode
     */
    public static void configureActionIfFileExist( int actionCode ) {
        lastConfigurationActionIfFileExist = actionCode;
    }
    static int lastConfigurationActionIfFileExist = 0;

    /** Copies src file to dst file. If the dst file does not exist, it is created. If it existed, it is overwritten. <p>
     * @param src
     * @param dst
     * @throws IOException
     */
    public static void copyFile(File src, File dst ) throws IOException {
    	try {
			windowForFeedback.inicioCopia( src.length() );
	        copyFile( src, dst, false );
    	} finally {
            windowForFeedback.finCopia( src.length() );
    	}
    }

    /** Copies src file to dst file. If the dst file does not exist, it is created.<p>
     * Sets same date last mod to both files.
     * @param src
     * @param dst
     * @param callsActionIfDestExisted Si es true, llama al método actionIfDestExisted( dst ). Si es false, simplemente sobreescribe
     * @throws IOException
     */
    public static void copyFile(File src, File dst, boolean callsActionIfDestExisted ) throws IOException {
        if (src == null) lanzaIOException( "copyFile: null source file" );
        if (dst == null) lanzaIOException( "copyFile: null destination file" );
        if (!src.exists()) lanzaIOException("copyFile: no such source file: " + src.getAbsolutePath() );
        if (!src.isFile()) lanzaIOException("copyFile: can't copy directory: " + src.getAbsolutePath() );
        if (!src.canRead()) lanzaIOException("copyFile: source file is unreadable: " + src.getAbsolutePath() );
        if (dst.isDirectory()) dst = new File(dst, dst.getName());
        if (dst.exists()) {
            if (callsActionIfDestExisted) actionIfFileExist( dst ); // Something to made before overwrite
            if (!dst.canWrite()) lanzaIOException("copyFile: destination file is unwriteable: " + dst.getAbsolutePath() );
            if (dst.isHidden()) dst.delete();  // Borrar si es oculto para poder sobreescribirlo
        } else {
            String parent = dst.getParent();
            if (parent == null) parent = System.getProperty("user.dir");
            File dir = new File(parent);
            if (!dir.exists()) lanzaIOException("copyFile: destination directory doesn't exist: " + parent);
            if (dir.isFile()) lanzaIOException("copyFile: destination path is not a directory: " + parent);
            if (!dir.canWrite()) lanzaIOException("copyFile: destination directory is unwriteable: " + parent);
        }
        // Warning of big file (time without feedback)
        if (src.length() > BIG_SIZE) {
            if (consoleFeedback) System.out.println( "   ... copying a file of " + src.length()/1048576L + " Mb: " + src.getAbsolutePath() );
            if (windowForFeedback != null) windowForFeedback.sacaMensaje( "Copiando fichero de " + src.length()/1048576L + " Mb... " + src.getName() );
        }
        // Copy file itself:
        notCopyingOrCancelCopyProcess = false;
        FileInputStream from = null;
        FileOutputStream to = null;
        long fromTime = src.lastModified();
        try {
            from = new FileInputStream(src);
            to = new FileOutputStream(dst);
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = from.read(buffer)) != -1) {
                to.write(buffer, 0, bytesRead); // write
                if (cancelSyncProcess) { try { to.close(); dst.delete(); to = null; } catch (IOException exx) {} break; }  // Se cancela a medias todo el proceso de sincronización
                if (notCopyingOrCancelCopyProcess) {   // Se cancela solo la copia
                    try { to.close(); dst.delete(); to = null; } catch (IOException exx) {}
                    ficherosIgnorados++;
                    bigFilesNotCopied.add(src.getAbsolutePath());
                    inform( src, DPFTipoArbol.DPF_IGNORADOS_EN_FUENTE );
                    break;
                }  // Si se cancela sólo copia
            }
        } finally {
            notCopyingOrCancelCopyProcess = true;
            if (from != null)
                try {
                    from.close();
                } catch (IOException e) { }
            if (to != null)
                try {
                    to.close();
                } catch (IOException e) { }
        }
        if (windowForFeedback != null) windowForFeedback.sumaBytesCopiados( src.length() );
        dst.setLastModified( fromTime );
        if (src.length() > BIG_SIZE && windowForFeedback != null) windowForFeedback.sacaMensaje( " " );
    }

    /** Creates a directory if it does not exist. Returns true if it exists or is correctly created.
     * @param d Directory to create
     * @return true if correct creation, false if creation failed
     */
    public static boolean createDirectory( File d ) {
        if (d==null) return false;
        return d.isDirectory() || d.mkdirs();
    }

    /** Borra un directorio 
     * @param dir
     */
    public static void deleteSingleDir( File dir ) {
    	boolean ret = dir.delete();
    	if (ret && usaMapeoDestinoGuardado) { // Si usa mapeo y se borra un directorio del destino, quitarlo del mapeo también (solo si está vacío)
    		borraDirEnMapeo( dir.getAbsolutePath() );
    	}
    }
    
    public static boolean deleteFile( File fic ) {
    	boolean ret = fic.delete();
    	if (ret && usaMapeoDestinoGuardado) { // Si usa mapeo y se borra un fichero del destino, quitarlo del mapeo también
    		borraFicEnMapeo( fic.getAbsolutePath() );
    	}
    	return ret;
    }

    /** Cancela el proceso de sincronización en el momento en que esté
     */
    public static void cancelSync() {
        cancelSyncProcess = true;
    }

    /** Cancela el proceso de copia del fichero que esté o del próximo fichero
     */
    public static void cancelCopy() {
        notCopyingOrCancelCopyProcess = true;
    }

    /** Informa si se está copiando un fichero actualmente
     */
    public static boolean programIsCopyingNow() {
        return (!notCopyingOrCancelCopyProcess);
    }

    // Mapeo de destino
    private static boolean usaMapeoDestinoGuardado = false;
    private static ItemDirectorio directorioDestinoActual = null;

    /** Determina si se usa o no el mapeo de destino guardado (si existe) para guardar la sincronización
     * @param usar	true para usarlo (si existe - fichero en logs/ con el mismo nombre de la ruta destino con # en vez de \), false para no usarlo
     */
    public static void setUsoMapeoDestinoGuardado( boolean usar ) {
    	usaMapeoDestinoGuardado = usar;
    }

    private static void initMapeoDestino( String rutaSinBarraFinal ) {
		directorioDestinoActual = new ItemDirectorio( rutaSinBarraFinal );
    }
    
    private static void cargaMapeoDestinoGuardado( String carpetaInicial ) {
    	String ruta = quitaBarraFinal( carpetaInicial );
    	File dir = new File( "logs" );
    	if (!dir.exists()) {
    		initMapeoDestino(ruta);
    		return;
    	}
    	File fMapeo = new File( dir, sustituyeBarras( ruta ) );
    	if (!fMapeo.exists()) {
    		initMapeoDestino(ruta);
    		return;
    	}
    	try (ObjectInputStream ois = new ObjectInputStream( new FileInputStream( fMapeo ))) {
        	System.out.println( "Leyendo mapa guardado. Espera unos segundos...");
    		directorioDestinoActual = (ItemDirectorio) ois.readObject();
        	if (directorioDestinoActual==null) {
        		initMapeoDestino(ruta);
            	System.out.println( "Fin de lectura (vacía).");
        	} else {
        		quitaInfoNuevo( directorioDestinoActual );
            	System.out.println( "Fin de lectura correcta.");
        	}
    	} catch (IOException | ClassNotFoundException | ClassCastException e) {
        	System.out.println( "Fin de lectura (incorrecta).");
    		initMapeoDestino(ruta);
    	}
    }
    	private static void quitaInfoNuevo( ItemArchivo item ) {
    		if (item==null) return;
    		item.nuevoEnMapa = false;
    		if (item instanceof ItemDirectorio) {
    			for (ItemArchivo i : ((ItemDirectorio)item).contenido.values()) {
    				quitaInfoNuevo( i );
    			}
    		}
    	}
    private static void guardaMapeoDestino( String carpetaInicial ) {
    	String ruta = quitaBarraFinal( carpetaInicial );
    	File dir = new File( "logs" );
    	if (!dir.exists()) dir.mkdir();
    	File fMapeo = new File( dir, sustituyeBarras( ruta ) );
    	try (ObjectOutputStream oos = new ObjectOutputStream( new FileOutputStream( fMapeo, false ))) {
    		oos.writeObject( directorioDestinoActual );
    	} catch (IOException e) {
    		JOptionPane.showMessageDialog( windowForFeedback, "No ha podido guardarse el fichero de mapeo de destino logs/" + fMapeo.getName() );
    	}
    }
    	private static String quitaBarraFinal( String path ) {
    		while (path.endsWith( "\\" ) || path.endsWith( "/" ) ) {
    			path = path.substring( 0, path.length() - 1 );
    		}
    		return path;
    	}
    	private static String sustituyeBarras( String path ) {
    		return path.replaceAll( "\\\\", "#" ).replaceAll( "/", "#" ).replaceAll( ":", "#" );
    	}

    // Borra dir en mapeo si está en el mapeo destino
	private static void borraDirEnMapeo( String pathAbsoluto ) {
		ItemArchivo item = buscaDirEnMapeo( pathAbsoluto, true );
		if (item!=null && item instanceof ItemDirectorio) {
			int barra = pathAbsoluto.lastIndexOf( "/" );
			if (barra==-1) barra = pathAbsoluto.lastIndexOf( "\\" );
			if (barra!=-1) {
				((ItemDirectorio)item).removeItem( pathAbsoluto.substring( barra+1 ) );
			}
		}
	}

    // Borra fic en mapeo si está en el mapeo destino
	private static void borraFicEnMapeo( String pathAbsoluto ) {
		ItemArchivo item = buscaFicEnMapeo( pathAbsoluto, true );
		if (item!=null && item instanceof ItemDirectorio) {
			int barra = pathAbsoluto.lastIndexOf( "/" );
			if (barra==-1) barra = pathAbsoluto.lastIndexOf( "\\" );
			if (barra!=-1) {
				((ItemDirectorio)item).removeItem( pathAbsoluto.substring( barra+1 ) );
			}
		}
	}
	
	public static ItemArchivo buscaFicEnMapeo( String pathAbsoluto, boolean buscaPadre ) {
    	String rutaInicial = DirProcess.dirDestino.getAbsolutePath();
    	if (pathAbsoluto.startsWith( rutaInicial )) {
    		String rutaResto = pathAbsoluto.substring( rutaInicial.length()+1 );  // Quitando ruta inicial y la barra-contrabarra
    		return buscaFicEnMapeoRec( directorioDestinoActual, rutaResto, buscaPadre, true );
    	}
    	return null;
	}
		private static ItemArchivo buscaFicEnMapeoRec( ItemDirectorio dir, String ruta, boolean buscaPadre, boolean primeraLlamada ) {
			int barra = ruta.indexOf( "/" );
			if (barra==-1) barra = ruta.indexOf( "\\" );
			if (barra==-1) {  // Es final de trayecto
				ItemArchivo item = dir.getItem( ruta );
				if (item==null) {  // No existe en mapa
					return null;
				}
				if (!(item instanceof ItemFichero)) {  // No es un fichero (que es lo que debería)
					return null;
				}
				if (buscaPadre) {
					return dir;
				} else {
					return item;
				}
			} else {  // Es directorio intermedio
				String carpeta = ruta.substring( 0, barra );
				ItemArchivo item = dir.getItem( carpeta );
	    		// Solo al principio puede ser que haya varias carpetas consecutivas. Esas hay que buscarlas por si acaso  (t\camtasia se guardará como camtasia)
				while (primeraLlamada && item==null) {
					ruta = ruta.substring( barra + 1 );
					barra = ruta.indexOf( "/" );
					if (barra==-1) barra = ruta.indexOf( "\\" );
					if (barra==-1) {
						break;
					}
					carpeta = ruta.substring( 0, barra );
					item = dir.getItem( carpeta );
				}
				if (item==null || !(item instanceof ItemDirectorio)) { // No existe en mapa o no es directorio
					return null;
				}
				return buscaFicEnMapeoRec( (ItemDirectorio)item, ruta.substring( barra+1 ), buscaPadre, false );
			}
		}
	
	public static ItemDirectorio buscaDirEnMapeo( String pathAbsoluto, boolean buscaPadre ) {
    	String rutaInicial = DirProcess.dirDestino.getAbsolutePath();
    	if (pathAbsoluto.startsWith( rutaInicial )) {
    		String rutaResto = pathAbsoluto.substring( rutaInicial.length()+1 );  // Quitando ruta inicial y la barra-contrabarra
    		return buscaDirEnMapeoRec( directorioDestinoActual, rutaResto, buscaPadre, true );
    	}
    	return null;
	}
		private static ItemDirectorio buscaDirEnMapeoRec( ItemDirectorio dir, String ruta, boolean buscaPadre, boolean primeraLlamada ) {
			int barra = ruta.indexOf( "/" );
			if (barra==-1) barra = ruta.indexOf( "\\" );
			if (barra==-1) {  // Es final de trayecto
				ItemArchivo item = dir.getItem( ruta );
				if (item==null) {  // No existe en mapa
					return null;
				}
				if (!(item instanceof ItemDirectorio)) {  // No es un fichero (que es lo que debería)
					return null;
				}
				if (buscaPadre) {
					return dir;
				} else {
					return (ItemDirectorio) item;
				}
			} else {  // Es directorio intermedio
				String carpeta = ruta.substring( 0, barra );
				ItemArchivo item = dir.getItem( carpeta );
	    		// Solo al principio puede ser que haya varias carpetas consecutivas. Esas hay que buscarlas por si acaso  (t\camtasia se guardará como camtasia)
				while (primeraLlamada && item==null) {
					ruta = ruta.substring( barra + 1 );
					barra = ruta.indexOf( "/" );
					if (barra==-1) barra = ruta.indexOf( "\\" );
					if (barra==-1) {
						break;
					}
					carpeta = ruta.substring( 0, barra );
					item = dir.getItem( carpeta );
				}
				if (item==null || !(item instanceof ItemDirectorio)) { // No existe en mapa o no es directorio
					return null;
				}
				return buscaDirEnMapeoRec( (ItemDirectorio)item, ruta.substring( barra+1 ), buscaPadre, false );
			}
		}
    	
    
    // Funcionamiento
    
    private static boolean pauseSyncProcess = false;
    private static boolean syncProcessPaused = false;
    private static boolean cancelSyncProcess = false;
    private static boolean notCopyingOrCancelCopyProcess = true;
    static DirProcessFrame windowForFeedback = null;

    static long timeConsidered = 0;
    static int nobiggerFilesThanMbytes = 0;
    static ArrayList<String> bigFilesNotCopied;
    static long ficherosNuevos = 0;
    static long ficherosReemplazados = 0;
    static long ficherosBorrados = 0;
    static long ficherosRespaldados = 0;
    static long ficherosIguales = 0;
    static long ficherosBloqueados = 0;
    static long ficherosIgnorados = 0;
    static long erroresRespaldados = 0;
    static long erroresCopia = 0;
    static long avisosReemplazados = 0;
    static long ficherosSoloEnOrigen = 0;
    static long ficherosSoloEnDestino = 0;
    static long ficherosDiferentes = 0;

    /**  Sincroniza un directorio con otro
     * @param daysModified  Intervalo (en días) en los que considera los ficheros. 0 para considerar todos
     * @param dirFuente Directorio fuente
     * @param dirDestino    Directorio destino
     * @param subdirsFuente Array de nombres correctos de subdirectorios en fuente a sincronizar (si es vacío, se sincroniza todo el fuente)
     * @param subdirsDestino Array de nombres correctos de subdirectorios en destino a sincronizar. Debe tener el mismo tamaño que aubdirsFuente.
     * @param ficherosACopiar Patrón de expresión regular válida (@see java.util.regex.Pattern) para los ficheros que se quieren copiar ("" no se considera)
     * @param ficherosAExcluir Patrón de expresión regular válida (@see java.util.regex.Pattern) para los ficheros que no se quieren copiar ("" no se considera)
     * @param carpetasAExcluir Patrón de expresión regular válida (@see java.util.regex.Pattern) para las carpetas que no se quieren copiar ("" no se considera)
     * @param tipoDeCopia   Valor de tipo de copia: <p>
     *   TipoCopia.TIPO_COPIA_SINCRONIZACION - Borra antiguos, reescribe modificados<p>
     *   TipoCopia.TIPO_COPIA_BACKUP - Lleva antiguos y modificados a carpeta de respaldo<p>
     *   TipoCopia.TIPO_COPIA_RESTORE - Lleva modificados a carpeta de respaldo, mantiene los antiguos<p>
     *   TipoCopia.TIPO_COMPARACION - Compara origen y destino e informa de las diferencias, sin hacer cambios<p>
     * @param subdirRespaldoDestino Subdirectorio con nombre correcto en destino donde dejar el respaldo. Se crearán DELETED_FILES y UPDATED_FILES dentro de ella. Si es vacío, directamente en el directorio destino
     * @param consoleFeedback - Si true, se genera información de las operaciones en consola
     * @param windowFeedback  - Si true, se genera información de las operaciones en ventana
     * @param nobiggerFilesThanMbytes   Si es <= 0, no tiene efecto. Si es > 0, indica que los ficheros de tamaño mayor que el número indicado se ignoran (pero se indican al final)
     * @throws IOException  Se genera si hay cualquier error en los parámetros o un error irrecuperable en el proceso de sincronización.
     */
    public static void syncAllDirsAndFiles( int daysModified, File dirFuente, File dirDestino, String[] subdirsFuente, String[] subdirsDestino, 
            String ficherosACopiar, String ficherosAExcluir, String carpetasAExcluir,
            TipoCopia tipoDeCopia, String subdirRespaldoDestino, boolean consoleFeedback, boolean windowFeedback, int nobiggerFilesThanMbytes ) throws IOException {
        cancelSyncProcess = false;
        if (daysModified == 0) timeConsidered = 0; else timeConsidered = System.currentTimeMillis() - daysModified*24*3600000L;  // Cada día tiene 24 * 3.600 * 1000 milisegundos
        File carpetaLogs = new File( "logs" );
        if (tipoDeCopia == TipoCopia.TIPO_COMPARACION) {
        	if (!carpetaLogs.exists()) { Files.createDirectory( new File( "logs" ).toPath() ); }
        	Debug.activateDebugLog( "logs/AEsync-compare" ); // creates logs/AEsync-compare.log
        }
        else {
        	if (!carpetaLogs.exists()) { Files.createDirectory( new File( "logs" ).toPath() ); }
        	Debug.activateDebugLog("logs/AEsync");  // creates AEsync.log
        }
        Debug.setConsoleView( consoleFeedback );
        Debug.setWindowView( false );
        Debug.setPrefix("");
        if (windowFeedback) {
            windowForFeedback = new DirProcessFrame( dirFuente, dirDestino, true );
        } else {
            windowForFeedback = null;
        }
        DirProcess.nobiggerFilesThanMbytes = nobiggerFilesThanMbytes;
        bigFilesNotCopied = new ArrayList<String>();
        ficherosNuevos = 0;
        ficherosReemplazados = 0;
        ficherosBorrados = 0;
        ficherosRespaldados = 0;
        ficherosIguales = 0;
        ficherosBloqueados = 0;
        ficherosIgnorados = 0;
        erroresRespaldados = 0;
        erroresCopia = 0;
        avisosReemplazados = 0;
        ficherosSoloEnDestino = 0;
        ficherosSoloEnOrigen = 0;
        ficherosDiferentes = 0;

        if (!initPatterns(ficherosACopiar, ficherosAExcluir, carpetasAExcluir)) lanzaIOException( "syncAllDirsAndFiles: error en patrones de copia/exclusión" );
        if (dirFuente == null) lanzaIOException( "syncAllDirsAndFiles: directorio fuente nulo" );
        if (dirFuente == null) lanzaIOException( "syncAllDirsAndFiles: directorio destino nulo" );
        if (!dirFuente.isDirectory()) lanzaIOException( "syncAllDirsAndFiles: directorio fuente incorrecto " + dirFuente.getAbsolutePath() );
        if (!dirDestino.exists()) {
            if (!dirDestino.isDirectory()) lanzaIOException( "syncAllDirsAndFiles: directorio destino incorrecto " + dirDestino.getAbsolutePath() );
        } else {
            if (!createDirectory(dirDestino)) lanzaIOException( "syncAllDirsAndFiles: no se pudo crear directorio destino " + dirDestino.getAbsolutePath() );
        }
        if (subdirsFuente == null && subdirsDestino != null) lanzaIOException( "syncAllDirsAndFiles: subdirectorios incompatibles fuente y destino" );
        if (subdirsFuente != null && subdirsDestino == null) lanzaIOException( "syncAllDirsAndFiles: subdirectorios incompatibles fuente y destino" );
        if (subdirsFuente != null && subdirsDestino != null && subdirsFuente.length != subdirsDestino.length)
            lanzaIOException( "syncAllDirsAndFiles: subdirectorios incompatibles fuente y destino: " + dirFuente.getAbsolutePath() + " y "  + dirDestino.getAbsolutePath() );
        if (tipoDeCopia==null) lanzaIOException( "syncAllDirsAndFiles: tipo de copia no definida" );
        // Creación de directorio de respaldo y sus subdirectorios
        respaldo = null;  // Vble para el thread
        if (tipoDeCopia == TipoCopia.TIPO_COPIA_BACKUP || tipoDeCopia == TipoCopia.TIPO_COPIA_RESTORE) {
            respaldo = new File( dirDestino, subdirRespaldoDestino );
            if (!respaldo.isDirectory() && !createDirectory(respaldo)) {  // Directorio de respaldo no existe y no se puede crear. No se procesa
                lanzaIOException( "syncAllDirsAndFiles: subdirectorio de respaldo " + respaldo.getAbsolutePath() + " no pudo crearse" );
            }
            if (!createDirectory( new File(respaldo,DELETED_FILES) ) || !createDirectory( new File(respaldo,UPDATED_FILES) )) {
                lanzaIOException( "syncAllDirsAndFiles: subdirectorios de respaldo " + respaldo.getAbsolutePath() + " no pudieron crearse" );
            }
        }
        // Gestión de mapeo de destino
        if (usaMapeoDestinoGuardado) {
        	cargaMapeoDestinoGuardado( dirDestino.getAbsolutePath() );
        }
        // Variables para el thread
        DirProcess.subdirsFuente = subdirsFuente;
        DirProcess.subdirsDestino = subdirsDestino;
        DirProcess.dirFuente = dirFuente;
        DirProcess.dirDestino = dirDestino;
        DirProcess.tipoDeCopia = tipoDeCopia;
        DirProcess.consoleFeedback = consoleFeedback;
        syncInThread();
    }

        private static String[] subdirsFuente;
        private static String[] subdirsDestino;
        private static File dirFuente;
        private static File dirDestino;
        private static TipoCopia tipoDeCopia;
        private static File respaldo;
        private static boolean consoleFeedback;

        private static void syncInThread() {
            Runnable r = new Runnable() {
                public void run() {
                    try {
                        // Bucle de subdirectorios
                        if ((subdirsFuente == null && subdirsDestino == null) || (subdirsFuente.length == 0 && subdirsDestino.length == 0)) {
                            subdirsFuente = new String[] { "" }; subdirsDestino = new String[] { "" };
                        }
                        for (int i = 0; i < subdirsFuente.length; i++) {
                            String dirF = (subdirsFuente[i]==null) ? "" : subdirsFuente[i];
                            String dirD = (subdirsDestino[i]==null) ? "" : subdirsDestino[i];
                            File fuente = new File( dirFuente, dirF );
                            File destino = new File( dirDestino, dirD );
                            // Chequeo de directorios fuente y destino
                            if (!fuente.isDirectory()) { // No hay directorio fuente. No se procesa
                                inform( "Directorio fuente no existe. No procesado: ", fuente, null );
                            } else {
                                boolean destinoOk = true;
                                if (destino.exists()) {
                                    if (!destino.isDirectory()) {  // Directorio destino es un fichero. No se procesa
                                        inform( "Directorio destino no es un directorio. No procesado: ", destino, null );
                                        destinoOk = false;
                                    }
                                } else {
                                    if (!createDirectory(destino)) {  // Directorio destino no se puede crear. No se procesa
                                        inform( "Directorio destino no pudo crearse. No procesado: ", destino, null );
                                        destinoOk = false;
                                    }
                                }
                                if (destinoOk && !cancelSyncProcess) {
                                	ItemArchivo item = null;
                                    if (usaMapeoDestinoGuardado) {
                                    	item = directorioDestinoActual.getItem( destino.getName() );
                                    	if (item==null || item instanceof ItemFichero) {
                                    		item = new ItemDirectorio( destino.getName() );
                                    		directorioDestinoActual.addItem( item );
                                    	}
                                    }
                                    syncAllDirsAndFilesRec( fuente, destino, dirF, dirD, tipoDeCopia, respaldo, consoleFeedback, 100.0*i/subdirsFuente.length, 100.0/subdirsFuente.length, item );
                                }
                            }
                        }
                    } catch (IOException ex) {
                        // IOException
                    }
                    // Fin de mapeo
                    if (usaMapeoDestinoGuardado && directorioDestinoActual!=null) {

                    	
                    	
                    	
                    	
                    	
                    	
                    	
                    	
                    	
                    	
                    	
                    	
                    	
                    	
                    	
                    	
                    	
                    	
                    	
                    	
                    	// TODO quitar
                    	directorioDestinoActual.volcarEnConsola();
                    	guardaMapeoDestino( dirDestino.getAbsolutePath() );
                    }
                    Debug.show( "Proceso syncAllDirsAndFiles finalizado." );
                    String mensajeFinal = "";
                    String mensajeFinal1 = "";  // Para debug
                    String mensajeFinal2 = "";  // Para ventana
                    if (bigFilesNotCopied.size() > 0) {
                        mensajeFinal2 += ("Ficheros mayores que " + nobiggerFilesThanMbytes + " Mbytes (no copiados): " + bigFilesNotCopied.size() + "\n" );
                        mensajeFinal1 += ("Ficheros mayores que " + nobiggerFilesThanMbytes + " Mbytes (no copiados):" +"\n" );
                        for (String s : bigFilesNotCopied)
                            mensajeFinal1 += ( "  " + s +"\n" );
                    }
                    mensajeFinal += ( "Ficheros iguales:           " + ficherosIguales +"\n" );
                    mensajeFinal += ( "Ficheros ignorados:         " + ficherosIgnorados +"\n" );
                    if (tipoDeCopia == TipoCopia.TIPO_COMPARACION) {
                        mensajeFinal += ( "Ficheros diferentes:        " + ficherosDiferentes +"\n" );
                        mensajeFinal += ( "Ficheros solo en origen:    " + ficherosSoloEnOrigen +"\n" );
                        mensajeFinal += ( "Ficheros solo en destino:   " + ficherosSoloEnDestino +"\n" );
                    } else {
                        mensajeFinal += ( "Ficheros copiados nuevos:   " + ficherosNuevos +"\n" );
                        mensajeFinal += ( "Ficheros sobreescritos:     " + ficherosReemplazados +"\n" );
                        mensajeFinal += ( "Ficheros borrados:          " + ficherosBorrados +"\n" );
                        mensajeFinal += ( "Errores en copia:           " + (erroresCopia+ficherosBloqueados) + " (" + ficherosBloqueados + " ficheros bloqueados)" +"\n" );
                        mensajeFinal += ( "Avisos ficheros mas nuevos: " + avisosReemplazados );
                        if (tipoDeCopia == TipoCopia.TIPO_COPIA_BACKUP || tipoDeCopia == TipoCopia.TIPO_COPIA_RESTORE) {
                            mensajeFinal += ( "\n" + "Ficheros sobreescritos y borrados salvados en respaldo: " + ficherosRespaldados );
                            mensajeFinal += ( "\n" + "Errores en respaldo:                                    " + erroresRespaldados );
                        }
                    }
                    Debug.show( mensajeFinal1 + mensajeFinal );
                    if (windowForFeedback != null) {
                        windowForFeedback.finSincronizacion( "Final de proceso de sincronización" );
                        windowForFeedback = null;
                        Object[] opciones;
                        if (bigFilesNotCopied.size() > 0)
                            opciones = new String[] {"Ok", "Ver ficheros grandes no copiados" };
                        else
                            opciones = new String[] {"Ok" };
                        int respuesta = JOptionPane.showOptionDialog( null, mensajeFinal2 + mensajeFinal, "AEsync: Informe de fin de proceso",
                            JOptionPane.INFORMATION_MESSAGE, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0] );
                        if (respuesta == 1) {
                            JOptionPane.showMessageDialog( null, mensajeFinal1, "AEsync: Informe de ficheros grandes no copiados", JOptionPane.INFORMATION_MESSAGE );
                        }
                    }
                }
            };
            Thread t = new Thread(r, "Hilo-DirProcess");
            t.start();
        }

        static private void lanzaIOException( String mens ) throws IOException {
            if (windowForFeedback != null) {
                windowForFeedback.anyadeZonaMensajes( mens, true );
                windowForFeedback.sacaMensaje( mens );
            }
            throw new IOException( mens );
        }

    /**  Sincroniza un directorio con otro (versión reducida de parámetros, sin exclusión/inclusión)
     * @param daysModified  Intervalo (en días) en los que considera los ficheros. 0 para considerar todos
     * @param dirFuente Directorio fuente
     * @param dirDestino    Directorio destino
     * @param subdirsFuente Array de nombres correctos de subdirectorios en fuente a sincronizar (si es vacío, se sincroniza todo el fuente)
     * @param subdirsDestino Array de nombres correctos de subdirectorios en destino a sincronizar. Debe tener el mismo tamaño que aubdirsFuente.
     * @param tipoDeCopia   Valor de tipo de copia: <p>
     *   TipoCopia.TIPO_COPIA_SINCRONIZACION - Borra antiguos, reescribe modificados<p>
     *   TipoCopia.TIPO_COPIA_BACKUP - Lleva antiguos y modificados a carpeta de respaldo<p>
     *   TipoCopia.TIPO_COPIA_RESTORE - Lleva modificados a carpeta de respaldo, deja los antiguos<p>
     *   TipoCopia.TIPO_COMPARACION - Compara origen y destino e informa de las diferencias, sin hacer cambios<p>
     * @param subdirRespaldoDestino Subdirectorio con nombre correcto en destino donde dejar el respaldo. Se crearán DELETED_FILES y UPDATED_FILES dentro de ella. Si es vacío, directamente en el directorio destino
     * @param nobiggerFilesThanMbytes   Si es <= 0, no tiene efecto. Si es > 0, indica que los ficheros de tamaño mayor que el número indicado se ignoran (pero se indican al final)
     * @param consoleFeedback - Si true, se genera información de las operaciones en consola
     * @param windowFeedback  - Si true, se genera información de las operaciones en ventana
     * @throws IOException  Se genera si hay cualquier error en los parámetros o un error irrecuperable en el proceso de sincronización.
     */
    public static void syncAllDirsAndFiles( int daysModified, File dirFuente, File dirDestino, String[] subdirsFuente, String[] subdirsDestino,
            TipoCopia tipoDeCopia, String subdirRespaldoDestino, boolean consoleFeedback, boolean windowFeedback, int nobiggerFilesThanMbytes ) throws IOException {
        syncAllDirsAndFiles( daysModified, dirFuente, dirDestino, subdirsFuente, subdirsDestino, 
                "", "", "",
                tipoDeCopia, subdirRespaldoDestino, consoleFeedback, windowFeedback, nobiggerFilesThanMbytes );
    }

    /**  Sincroniza un directorio con otro (versión reducida de parámetros, sin subdirectorios ni exclusión/inclusión, con dir. de respaldo "BACKUP")
     * @param daysModified  Intervalo (en días) en los que considera los ficheros. 0 para considerar todos
     * @param dirFuente Directorio fuente
     * @param dirDestino    Directorio destino
     * @param tipoDeCopia   Valor de tipo de copia: <p>
     *   TipoCopia.TIPO_COPIA_SINCRONIZACION - Borra antiguos, reescribe modificados<p>
     *   TipoCopia.TIPO_COPIA_BACKUP - Lleva antiguos y modificados a carpeta de respaldo<p>
     *   TipoCopia.TIPO_COPIA_RESTORE - Lleva modificados a carpeta de respaldo, deja los antiguos<p>
     *   TipoCopia.TIPO_COMPARACION - Compara origen y destino e informa de las diferencias, sin hacer cambios<p>
     * @param consoleFeedback - Si true, se genera información de las operaciones en consola
     * @param windowFeedback  - Si true, se genera información de las operaciones en ventana
     * @param noBiggerFilesThanMbytes  Si es <= 0, no tiene efecto. Si es > 0, indica que los ficheros de tamaño mayor que el número indicado se ignoran (pero se indican al final)
     * @throws IOException  Se genera si hay cualquier error en los parámetros o un error irrecuperable en el proceso de sincronización.
     */
    public static void syncAllDirsAndFiles( int daysModified, File dirFuente, File dirDestino, TipoCopia tipoDeCopia, boolean consoleFeedback, boolean windowFeedback, int noBiggerFilesThanMbytes ) throws IOException {
        syncAllDirsAndFiles( daysModified, dirFuente, dirDestino, null, null, tipoDeCopia, "BACKUP", consoleFeedback, windowFeedback, noBiggerFilesThanMbytes );
    }

        private static String getPath( String fileName ) {
            int barra = Math.max( fileName.lastIndexOf('/'), fileName.lastIndexOf('\\') );
            if (barra < 0) return ""; else return fileName.substring(0,barra);
        }
        private static String getName( String fileName ) {
            int barra = Math.max( fileName.lastIndexOf('/'), fileName.lastIndexOf('\\') );
            if (barra < 0) return fileName; else return fileName.substring(barra+1);
        }


        private static String genVersionFileName( String fileName, int verNumber ) {
            int punto = fileName.lastIndexOf('.');
            if (punto < 0) punto = fileName.length();
            if (verNumber <= 9999)
                return fileName.substring(0,punto) + "_VER_" + String.format("%1$04d", verNumber) + fileName.substring(punto);
            else
                return fileName.substring(0,punto) + "_VER_" + String.format("%1$09d", verNumber) + fileName.substring(punto);
        }

        private static Pattern pFileYes = null;
        private static Pattern pFileNo = null;
        private static Pattern pDirNo = null;
        // If any pattern incorrect, returns false
        private static boolean initPatterns( String fileYes, String fileNo, String dirNo ) {
            try {
                if (fileYes!=null && !fileYes.equals("")) pFileYes = Pattern.compile( fileYes, Pattern.CASE_INSENSITIVE );
                if (fileNo!=null && !fileNo.equals("")) pFileNo = Pattern.compile( fileNo, Pattern.CASE_INSENSITIVE );
                if (dirNo!=null && !dirNo.equals("")) pDirNo = Pattern.compile( dirNo, Pattern.CASE_INSENSITIVE );
            } catch (PatternSyntaxException e) {
                return false;
            }
            return true;
        }
        private static boolean includeFile( String fileName ) {
            return ((pFileYes==null) || pFileYes.matcher(fileName).matches() );
        }
        private static boolean excludeFile( String fileName ) {
            return ((pFileNo!=null) && pFileNo.matcher(fileName).matches() );
        }
        private static boolean excludeDir( String dirName ) {
            return ((pDirNo!=null) && pDirNo.matcher(dirName).matches() );
        }

    /** Pausa o reactiva el hilo de sincronización
     * @param pausa	true para pausar, false para reactivar
     */
    public static void pausaSync( boolean pausa ) {
    	pauseSyncProcess = pausa;
    }
    
    public static void freeConsole() {
		IUConsola.cerrarConsolaEnIU();
    }
    
    /** Informa si el hilo de sincronización está efectivamente pausado (si se pide pausa y este método devuelve true, es que está realizándose aún una copia de fichero)
     * @return	true si el hilo está efectivamente pausado, false si está activo
     */
    public static boolean isPausedSync() {
    	return syncProcessPaused;
    }

    // Proceso recursivo
    private static void syncAllDirsAndFilesRec( File fuente, File destino, String pathRelF, String pathRelD, TipoCopia tipoDeCopia, File dirRespaldo, boolean consoleFeedback,
                                             double porcAcumuladoPrevio, double porcAProcesarEnEstaLlamada, ItemArchivo ficDestinoMapeo ) throws IOException {
        // andoni/AppData/Roaming/Apple Computer/MobileSync/Backup/a01cd4be4fa75a92b4aa0e6e1e8db1a4b5fe8788/f481a5af250808bb20eeeee5fc609b6e030b4164
        // if (consoleFeedback && (ficherosIguales >= 0)) System.out.println( fuente + " # " + destino + " # " + pathRelF + " # " + pathRelD );
        if (pathRelF.contains("a01cd4be4fa75a92b4aa0e6e1e8db1a4b5fe8788")) return;  // TODO: Muy raro. Casca en este directorio (algo de Apple)
        // if (pathRelF.contains("Adobe Audition 2.0 Loopology")) return;  // TODO: Muy raro. Casca en este directorio (algo de Apple)
        if (cancelSyncProcess) return;
        while (pauseSyncProcess) {
        	syncProcessPaused = true;
        	try { Thread.sleep(100); } catch (InterruptedException e) {}
        }
        syncProcessPaused = false;
        // Proceso de directorio o fichero
        if (fuente == null) {
            // Caso especial no existe en fuente pero sí en destino (solo procesado si es BACKUP o COMPARACION)
            if (tipoDeCopia == TipoCopia.TIPO_COPIA_BACKUP || tipoDeCopia == TipoCopia.TIPO_COMPARACION) {
                if ((usaMapeoDestinoGuardado && ficDestinoMapeo!=null && ficDestinoMapeo instanceof ItemDirectorio && !ficDestinoMapeo.nuevoEnMapa) 
                	|| destino.isDirectory()) {
                    if (tipoDeCopia == TipoCopia.TIPO_COMPARACION) inform( "  Directorio solo en destino: ", fuente, destino, DPFTipoArbol.DPF_SOLO_EN_DESTINO );
                    if (usaMapeoDestinoGuardado && ficDestinoMapeo!=null && ficDestinoMapeo instanceof ItemDirectorio && !ficDestinoMapeo.nuevoEnMapa) {
                        int i = 0;
                        int numHijos = ((ItemDirectorio)ficDestinoMapeo).contenido.size();
                    	for (ItemArchivo item : ((ItemDirectorio)ficDestinoMapeo).contenido.values()) {
                            syncAllDirsAndFilesRec( null, new File(destino,item.nombre), "", pathRelD+"/"+item.nombre, tipoDeCopia, dirRespaldo, consoleFeedback, porcAcumuladoPrevio+porcAProcesarEnEstaLlamada*i/numHijos, porcAProcesarEnEstaLlamada/numHijos, item );
                            i++;
                    	}
                    } else {
                        String[] childrenDest = destino.list();
                        int i = 0;
                        for (String sd : childrenDest) {
                        	File nuevoDestino = new File(destino,sd);
                        	ItemArchivo item = null;
                        	if (cancelSyncProcess) {
                        		break;
                        	}
                        	if (usaMapeoDestinoGuardado && ficDestinoMapeo!=null) {
	                        	item = ((ItemDirectorio)ficDestinoMapeo).getItem( sd );
	                        	if (item==null) {
	                        		if (nuevoDestino.isDirectory()) {
	                        			item = new ItemDirectorio( sd );
	                        		} else {
	                        			item = new ItemFichero( sd );
	                        		}
		                        	((ItemDirectorio)ficDestinoMapeo).addItem( item );
	                        	}
                        	}
                            syncAllDirsAndFilesRec( null, nuevoDestino, "", pathRelD+"/"+sd, tipoDeCopia, dirRespaldo, consoleFeedback, porcAcumuladoPrevio+porcAProcesarEnEstaLlamada*i/childrenDest.length, porcAProcesarEnEstaLlamada/childrenDest.length, item );
                            i++;
                        }
                    }
                } else {  // Fichero
                	System.out.println( "Solo en destino: " + destino );
                    if (tipoDeCopia == TipoCopia.TIPO_COMPARACION) {
                        if (usaMapeoDestinoGuardado && ficDestinoMapeo!=null && ficDestinoMapeo instanceof ItemFichero && !ficDestinoMapeo.nuevoEnMapa) {
                        	ItemFichero item = (ItemFichero) ficDestinoMapeo;
	                        if (includeFile(item.nombre) && !excludeFile(item.nombre)) {
	                            if (item.fechaUltMod >= timeConsidered) { // If modified before time considered, do nothing (see parameter daysModified of public method)
	                                if (nobiggerFilesThanMbytes > 0 && item.tamanyoBytes > nobiggerFilesThanMbytes*1048576L) { // If bigger than Mbytes indicated, it is not copied (saved for later info)
	                                    // If bigger than Mbytes indicated, it is not counted (saved for later info)
	                                    bigFilesNotCopied.add(item.getPathAbsoluto());
	                                    inform( item, DPFTipoArbol.DPF_IGNORADOS_EN_DESTINO );
	                                } else {
	                                	System.out.println( "Solo: " + fuente + " / " + destino );
	                                    inform( "  Fichero solo en destino: ", fuente, destino, DPFTipoArbol.DPF_SOLO_EN_DESTINO );
	                                    ficherosSoloEnDestino++;
	                                }
	                            }
	                        } else {
	                            inform( destino, DPFTipoArbol.DPF_IGNORADOS_EN_DESTINO );
	                        }
                        } else {
	                        if (includeFile(destino.getName()) && !excludeFile(destino.getName())) {
	                        	if (usaMapeoDestinoGuardado && ficDestinoMapeo!=null && ficDestinoMapeo instanceof ItemFichero) {
	                        		((ItemFichero)ficDestinoMapeo).fechaUltMod = destino.lastModified();
	                        		((ItemFichero)ficDestinoMapeo).tamanyoBytes = destino.length();
	                        	}
	                            if (destino.lastModified() >= timeConsidered) { // If modified before time considered, do nothing (see parameter daysModified of public method)
	                                if (nobiggerFilesThanMbytes > 0 && destino.length() > nobiggerFilesThanMbytes*1048576L) { // If bigger than Mbytes indicated, it is not copied (saved for later info)
	                                    // If bigger than Mbytes indicated, it is not counted (saved for later info)
	                                    bigFilesNotCopied.add(destino.getAbsolutePath());
	                                    inform( destino, DPFTipoArbol.DPF_IGNORADOS_EN_DESTINO );
	                                } else {
	                                	System.out.println( "Solo: " + fuente + " / " + destino );
	                                    inform( "  Fichero solo en destino: ", fuente, destino, DPFTipoArbol.DPF_SOLO_EN_DESTINO );
	                                    ficherosSoloEnDestino++;
	                                }
	                            }
	                        } else {
	                            inform( destino, DPFTipoArbol.DPF_IGNORADOS_EN_DESTINO );
	                        }
                        }
                    } else {  // BACKUP - fichero a borrados (deleted_files)
                        int posBarra = Math.max( pathRelD.lastIndexOf( '/' ), pathRelD.lastIndexOf( '\\' ) );
                        if (posBarra >= 0) {
                            String nuevoDir = pathRelD.substring( 0, posBarra );
                            createDirectory( new File( dirRespaldo, DELETED_FILES + "/" + nuevoDir ));
                        }
                        File backupFile = new File( dirRespaldo, DELETED_FILES + "/" + pathRelD );
                        int version=0;
                        while (backupFile.exists()) { version++; backupFile = new File( dirRespaldo, genVersionFileName( DELETED_FILES + "/" + pathRelD, version ) ); }
                        if (destino.renameTo( backupFile )) {
                            inform( "  Respaldo. Fichero de destino ya borrado. Movido a " + DELETED_FILES + ": ", destino, destino, DPFTipoArbol.DPF_SOLO_EN_DESTINO );
                            ficherosRespaldados++;
                            ficherosBorrados++;
                        	if (usaMapeoDestinoGuardado && ficDestinoMapeo!=null && ficDestinoMapeo instanceof ItemFichero) {
                        		if (ficDestinoMapeo.padre!=null) {
                        			ficDestinoMapeo.padre.removeItem( ficDestinoMapeo.nombre );
                        		}
                        	}
                        } else {  // Error en movimiento
                            inform( " ERROR de respaldo. Fichero de destino ya borrado no se ha podido mover a " + DELETED_FILES + ": ", destino, destino, DPFTipoArbol.DPF_SOLO_EN_DESTINO );
                            inform( "", destino, destino, DPFTipoArbol.DPF_ERRORES_COPIA );
                            erroresRespaldados++;
                        }
                    }
                }
            }
        } else if (fuente.isDirectory()) {
            // No se crea el directorio aquí para no replicar toda la estructura de directorios si está vacía. Se hace en <dir> abajo
            //            if (!destino.isDirectory()) {
            //                if (!createDirectory(destino))
            //                    lanzaIOException( "syncAllDirsAndFiles: subdirectorio destino " + destino.getAbsolutePath() + " no pudo crearse" );
            //            }
            // Toma los ficheros y subdirectorios de fuente y destino y los ordena, sólo si el directorio no está cumple patrón de exclusión. Ojo con el caso)
            if (!excludeDir(fuente.getAbsolutePath())) {
                String[] children = fuente.list();
                if (children == null) children = new String[0];
                if (usaMapeoDestinoGuardado && ficDestinoMapeo!=null && ficDestinoMapeo instanceof ItemDirectorio && !ficDestinoMapeo.nuevoEnMapa) {
                	List<ItemArchivo> childrenDest = new ArrayList<ItemArchivo>( ((ItemDirectorio)ficDestinoMapeo).contenido.values() );
	                Arrays.sort( children, String.CASE_INSENSITIVE_ORDER );
	                Collections.sort( childrenDest, new Comparator<ItemArchivo>() {
						@Override
						public int compare(ItemArchivo o1, ItemArchivo o2) {
							return o1.nombre.compareToIgnoreCase( o2.nombre );
						}
	                });
	                int iF = 0;
	                int iD = 0;
	                // Recorre ambos arrays pareando las secuencias y con null donde no hay pareo
	                while (iF < children.length || iD < childrenDest.size()) {
	                	if (cancelSyncProcess) {
	                		break;
	                	}
	                    if (iD >= childrenDest.size() || (iF < children.length && children[iF].compareToIgnoreCase(childrenDest.get(iD).nombre)<0)) {  // El de fuente no está en destino (f < d)
	                        // Crear el elemento del fuente también en el destino (salvo en comparación)
	                    	File nuevoFuente = new File(fuente, children[iF]);
	                    	ItemArchivo nuevoItem = null;
	                    	if (nuevoFuente.isFile()) {
	                    		nuevoItem = new ItemFichero( children[iF] );
	                    	} else {
	                    		nuevoItem = new ItemDirectorio( children[iF] );
	                    	}
	                    	((ItemDirectorio)ficDestinoMapeo).addItem(nuevoItem);
	                        syncAllDirsAndFilesRec( nuevoFuente, new File(destino, children[iF]), pathRelF+"/"+children[iF], pathRelD+"/"+children[iF], tipoDeCopia, dirRespaldo, consoleFeedback
	                                , porcAcumuladoPrevio+porcAProcesarEnEstaLlamada*(iF+iD)/(children.length+childrenDest.size()), porcAProcesarEnEstaLlamada/(children.length+childrenDest.size()), nuevoItem );
	                        iF++;
	                    } else if (iF >= children.length || (iD < childrenDest.size() && children[iF].compareToIgnoreCase(childrenDest.get(iD).nombre)>0)) {  // El de destino no está en fuente (f > d)
	                        if (tipoDeCopia == TipoCopia.TIPO_COPIA_BACKUP) {   // Si es sincronización o restore no hay nada que hacer, es un destino que no está en el fuente
	                            // Backup: el destino debe moverse a DELETED_FILES
	                            syncAllDirsAndFilesRec( null, new File(destino, childrenDest.get(iD).nombre), "", pathRelD+"/"+childrenDest.get(iD).nombre, tipoDeCopia, dirRespaldo, consoleFeedback
	                                    , porcAcumuladoPrevio+porcAProcesarEnEstaLlamada*(iF+iD)/(children.length+childrenDest.size()), porcAProcesarEnEstaLlamada/(children.length+childrenDest.size()), childrenDest.get(iD) );
	                        } else if (tipoDeCopia == TipoCopia.TIPO_COMPARACION) {   // Si es comparación sigue comparando
	                            syncAllDirsAndFilesRec( null, new File(destino, childrenDest.get(iD).nombre), "", pathRelD+"/"+childrenDest.get(iD).nombre, tipoDeCopia, dirRespaldo, consoleFeedback
	                                    , porcAcumuladoPrevio+porcAProcesarEnEstaLlamada*(iF+iD)/(children.length+childrenDest.size()), porcAProcesarEnEstaLlamada/(children.length+childrenDest.size()), childrenDest.get(iD) );
	                        }
	                        iD++;
	                    } else { // fuente y destino coinciden (f = d)
	                        syncAllDirsAndFilesRec( new File(fuente, children[iF]), new File( destino, childrenDest.get(iD).nombre ), pathRelF+"/"+children[iF], pathRelD+"/"+childrenDest.get(iD).nombre, tipoDeCopia, dirRespaldo, consoleFeedback
	                                , porcAcumuladoPrevio+porcAProcesarEnEstaLlamada*(iF+iD)/(children.length+childrenDest.size()), porcAProcesarEnEstaLlamada*2/(children.length+childrenDest.size()), childrenDest.get(iD) );
	                        iF++;
	                        iD++;
	                    }
	                }
                } else {
	                String[] childrenDest = (destino==null) ? null : destino.list();
	                if (childrenDest == null) childrenDest = new String[0];
	                Arrays.sort( children, String.CASE_INSENSITIVE_ORDER );
	                Arrays.sort( childrenDest, String.CASE_INSENSITIVE_ORDER );
	                int iF = 0;
	                int iD = 0;
	                // Recorre ambos arrays pareando las secuencias y con null donde no hay pareo
	                while (iF < children.length || iD < childrenDest.length) {
	                	if (cancelSyncProcess) {
	                		break;
	                	}
	                    if (iD >= childrenDest.length || (iF < children.length && children[iF].compareToIgnoreCase(childrenDest[iD])<0)) {  // El de fuente no está en destino (f < d)
	                    	File nuevoFuente = new File(fuente, children[iF]);
	                    	ItemArchivo nuevoItem = null;
	                    	if (usaMapeoDestinoGuardado && ficDestinoMapeo!=null && ficDestinoMapeo instanceof ItemDirectorio) {
		                    	if (nuevoFuente.isFile()) {
		                    		nuevoItem = new ItemFichero( children[iF] );
		                    	} else {
		                    		nuevoItem = new ItemDirectorio( children[iF] );
		                    	}
		                    	((ItemDirectorio)ficDestinoMapeo).addItem(nuevoItem);
	                    	}
	                        // Crear el elemento del fuente también en el destino (salvo en comparación)
	                        syncAllDirsAndFilesRec( new File(fuente, children[iF]), new File(destino, children[iF]), pathRelF+"/"+children[iF], pathRelD+"/"+children[iF], tipoDeCopia, dirRespaldo, consoleFeedback
	                                , porcAcumuladoPrevio+porcAProcesarEnEstaLlamada*(iF+iD)/(children.length+childrenDest.length), porcAProcesarEnEstaLlamada/(children.length+childrenDest.length), nuevoItem );
	                        iF++;
	                    } else if (iF >= children.length || (iD < childrenDest.length && children[iF].compareToIgnoreCase(childrenDest[iD])>0)) {  // El de destino no está en fuente (f > d)
	                    	File nuevoDestino = new File(destino, childrenDest[iD]);
	                    	ItemArchivo nuevoItem = null;
	                    	if (usaMapeoDestinoGuardado && ficDestinoMapeo!=null && ficDestinoMapeo instanceof ItemDirectorio) {
		                    	if (nuevoDestino.isFile()) {
		                    		nuevoItem = new ItemFichero( childrenDest[iD] );
		                    	} else {
		                    		nuevoItem = new ItemDirectorio( childrenDest[iD] );
		                    	}
		                    	((ItemDirectorio)ficDestinoMapeo).addItem(nuevoItem);
	                    	}
	                        if (tipoDeCopia == TipoCopia.TIPO_COPIA_BACKUP) {   // Si es sincronización o restore no hay nada que hacer, es un destino que no está en el fuente
	                            // Backup: el destino debe moverse a DELETED_FILES
	                            syncAllDirsAndFilesRec( null, nuevoDestino, "", pathRelD+"/"+childrenDest[iD], tipoDeCopia, dirRespaldo, consoleFeedback
	                                    , porcAcumuladoPrevio+porcAProcesarEnEstaLlamada*(iF+iD)/(children.length+childrenDest.length), porcAProcesarEnEstaLlamada/(children.length+childrenDest.length), nuevoItem );
	                        } else if (tipoDeCopia == TipoCopia.TIPO_COMPARACION) {   // Si es comparación sigue comparando
	                            syncAllDirsAndFilesRec( null, new File(destino, childrenDest[iD]), "", pathRelD+"/"+childrenDest[iD], tipoDeCopia, dirRespaldo, consoleFeedback
	                                    , porcAcumuladoPrevio+porcAProcesarEnEstaLlamada*(iF+iD)/(children.length+childrenDest.length), porcAProcesarEnEstaLlamada/(children.length+childrenDest.length), nuevoItem );
	                        }
	                        iD++;
	                    } else { // fuente y destino coinciden (f = d)
	                    	File nuevoFuente = new File(fuente, children[iF]);
	                    	ItemArchivo nuevoItem = null;
	                    	if (usaMapeoDestinoGuardado && ficDestinoMapeo!=null && ficDestinoMapeo instanceof ItemDirectorio) {
		                    	if (nuevoFuente.isFile()) {
		                    		nuevoItem = new ItemFichero( children[iF] );
		                    	} else {
		                    		nuevoItem = new ItemDirectorio( children[iF] );
		                    	}
		                    	((ItemDirectorio)ficDestinoMapeo).addItem(nuevoItem);
	                    	}
	                        syncAllDirsAndFilesRec( nuevoFuente, new File( destino, childrenDest[iD] ), pathRelF+"/"+children[iF], pathRelD+"/"+childrenDest[iD], tipoDeCopia, dirRespaldo, consoleFeedback
	                                , porcAcumuladoPrevio+porcAProcesarEnEstaLlamada*(iF+iD)/(children.length+childrenDest.length), porcAProcesarEnEstaLlamada*2/(children.length+childrenDest.length), nuevoItem );
	                        iF++;
	                        iD++;
	                    }
	                }
                }
            } else {
                // System.out.println( "  *** Excluding dir: " + fuente.getAbsolutePath() );
            }
        } else { // Fichero
            if (fuente.lastModified() < timeConsidered) {  // If modified before time considered, do nothing (see parameter daysModified of public method)
                ficherosIgnorados++;
                inform( fuente, DPFTipoArbol.DPF_IGNORADOS_EN_FUENTE );
                if (consoleFeedback && (ficherosIgnorados % 1000 == 0)) System.out.println( " " + String.format("%1$1.2f", (porcAcumuladoPrevio + porcAProcesarEnEstaLlamada)) + "% - " + ficherosIgnorados + " ficheros ignorados..." );
                else if (windowForFeedback != null && ficherosIgnorados % 50 == 0) {
                        windowForFeedback.sacaMensaje( ficherosIgnorados + " ficheros ignorados..." );
                        windowForFeedback.setProgreso( (int) Math.round((porcAcumuladoPrevio + porcAProcesarEnEstaLlamada)*100) );
                }
                if (usaMapeoDestinoGuardado && ficDestinoMapeo!=null) {
                	ficDestinoMapeo.quitarDelPadre();
                }
                return;
            }
            if (nobiggerFilesThanMbytes > 0 && fuente.length() > nobiggerFilesThanMbytes*1048576L) { // If bigger than Mbytes indicated, it is not copied (saved for later info)
                // If bigger than Mbytes indicated, it is not copied (saved for later info)
                ficherosIgnorados++;
                bigFilesNotCopied.add(fuente.getAbsolutePath());
                inform( fuente, DPFTipoArbol.DPF_IGNORADOS_EN_FUENTE );
                inform( "", fuente, fuente, DPFTipoArbol.DPF_FICHEROS_GRANDES );
                if (usaMapeoDestinoGuardado && ficDestinoMapeo!=null) {
                	ficDestinoMapeo.quitarDelPadre();
                }
                return;
            }
            if (includeFile(fuente.getName()) && !excludeFile(fuente.getName())) {
                if (!fuente.canRead() && (tipoDeCopia != TipoCopia.TIPO_COMPARACION)) {  // Fichero bloqueado
                    ficherosBloqueados++;
                    inform( " ERROR de copia. Fichero bloqueado no se ha copiado: ", fuente, null, DPFTipoArbol.DPF_MODERNO_EN_DESTINO );   // Suponemos moderno en destino
                    inform( "", fuente, destino, DPFTipoArbol.DPF_ERRORES_COPIA );
                    if (usaMapeoDestinoGuardado && ficDestinoMapeo!=null) {
                    	ficDestinoMapeo.quitarDelPadre();
                    }
                } else if (destino == null) {  // No debería ocurrir
                    lanzaIOException( "syncAllDirsAndFiles: subdirectorio destino nulo correspondiente a " + fuente.getAbsolutePath() );
                } else {
                    // Hay que copiar un fichero nuevo, o son iguales (o se comparan)
                    if ( (usaMapeoDestinoGuardado && ficDestinoMapeo!=null && ficDestinoMapeo instanceof ItemFichero && !ficDestinoMapeo.nuevoEnMapa)
                        || destino.exists()) {  // El fichero ya existía. Comprobar y comparar los atributos de fuente y destino
                        long modDateF = fuente.lastModified();
                        long sizeF = fuente.length();
                        long modDateD = 0;
                        long sizeD = 0;
                        if (usaMapeoDestinoGuardado && ficDestinoMapeo!=null && ficDestinoMapeo instanceof ItemFichero && !ficDestinoMapeo.nuevoEnMapa) {
	                        modDateD = ((ItemFichero)ficDestinoMapeo).fechaUltMod;
	                        sizeD = ((ItemFichero)ficDestinoMapeo).tamanyoBytes;
                        } else {
	                        modDateD = destino.lastModified();
	                        sizeD = destino.length();
                        }
                        if (modDateF == modDateD || sizeF == sizeD) {  // Son iguales, nada que hacer
                        	if (usaMapeoDestinoGuardado && ficDestinoMapeo!=null && ficDestinoMapeo instanceof ItemFichero && ficDestinoMapeo.nuevoEnMapa) {
                        		((ItemFichero)ficDestinoMapeo).fechaUltMod = modDateF;
                        		((ItemFichero)ficDestinoMapeo).tamanyoBytes = sizeF;
                        	}
                            ficherosIguales++;
                            if (consoleFeedback && (ficherosIguales % 1000 == 0)) System.out.println( " " + String.format("%1$1.2f", (porcAcumuladoPrevio + porcAProcesarEnEstaLlamada)) + "% - " + ficherosIguales + " ficheros iguales..." );
                            else if (windowForFeedback != null) {
                                inform( null, fuente, null, DPFTipoArbol.DPF_IGUALES );   // Añade fichero igual pero sin mensaje
                                if (ficherosIguales % 50 == 0) {
                                    windowForFeedback.sacaMensaje( ficherosIguales + " ficheros iguales..." );
                                    windowForFeedback.setProgreso( (int) Math.round((porcAcumuladoPrevio + porcAProcesarEnEstaLlamada)*100) );
                                }
                            }
                        } else if (modDateF < modDateD) {  // El fuente es anterior al destino: raro
                        	if (usaMapeoDestinoGuardado && ficDestinoMapeo!=null && ficDestinoMapeo instanceof ItemFichero && ficDestinoMapeo.nuevoEnMapa) {
                        		((ItemFichero)ficDestinoMapeo).fechaUltMod = modDateD;
                        		((ItemFichero)ficDestinoMapeo).tamanyoBytes = sizeD;
                        	}
                            if (tipoDeCopia == TipoCopia.TIPO_COMPARACION) {
                                inform( "  Fichero en origen anterior al destino: ", fuente, destino, DPFTipoArbol.DPF_MODERNO_EN_DESTINO  );
                                ficherosDiferentes++;
                            } else {
                                inform( "  ATENCION: hay en destino un fichero de fecha posterior al origen. No se ha copiado: ", fuente, destino, DPFTipoArbol.DPF_MODERNO_EN_DESTINO );
                                    avisosReemplazados++;
                            }
                        } else {  // El fuente es posterior al destino. Se copia, salvándolo previamente si es backup
                            if (windowForFeedback!=null) windowForFeedback.setProgreso( (int) Math.round((porcAcumuladoPrevio + porcAProcesarEnEstaLlamada)*100) );
                            if (tipoDeCopia == TipoCopia.TIPO_COPIA_BACKUP || tipoDeCopia == TipoCopia.TIPO_COPIA_RESTORE) {
                                // Backup: el destino debe moverse a UPDATED_FILES
                                int posBarra = Math.max( pathRelD.lastIndexOf( '/' ), pathRelD.lastIndexOf( '\\' ) );
                                if (posBarra >= 0) {
                                    String nuevoDir = pathRelD.substring( 0, posBarra );
                                    createDirectory( new File( dirRespaldo, UPDATED_FILES + "/" + nuevoDir ));
                                }
                                File backupFile = new File( dirRespaldo, UPDATED_FILES + "/" + pathRelD );
                                int version=0;
                                while (backupFile.exists()) { version++; backupFile = new File( dirRespaldo, genVersionFileName( UPDATED_FILES + "/" + pathRelD, version ) ); }
                                if (destino.renameTo( backupFile )) {
                                    inform( "  Respaldo. Fichero sustituido por uno más reciente, movido a " + UPDATED_FILES + ": ", destino, null );
                                    ficherosRespaldados++;
                                } else {  // Error en movimiento
                                    inform( " ERROR de respaldo. Fichero antiguo no se ha podido mover a " + UPDATED_FILES + ": ", destino, null );
                                    inform( "", destino, destino, DPFTipoArbol.DPF_ERRORES_COPIA );
                                    erroresRespaldados++;
                                }
                            }
                            if (tipoDeCopia == TipoCopia.TIPO_COMPARACION || tipoDeCopia == TipoCopia.TIPO_REVISION) {
                            	if (usaMapeoDestinoGuardado && ficDestinoMapeo!=null && ficDestinoMapeo instanceof ItemFichero && ficDestinoMapeo.nuevoEnMapa) {
                            		((ItemFichero)ficDestinoMapeo).fechaUltMod = modDateD;
                            		((ItemFichero)ficDestinoMapeo).tamanyoBytes = sizeD;
                            	}
                                inform( "  Fichero en origen posterior al destino: ", fuente, destino, DPFTipoArbol.DPF_MODERNO_EN_FUENTE );
                                ficherosDiferentes++;
                            } else {  // Salvo que sea comparación o revisión, se hace la copia
                                try {
                                    copyFile( fuente, destino );
                                    inform( "  Reemplazo de fichero más moderno: ", fuente, null, DPFTipoArbol.DPF_MODERNO_EN_FUENTE );
                                    ficherosReemplazados++;
                                    if (usaMapeoDestinoGuardado && ficDestinoMapeo!=null && ficDestinoMapeo instanceof ItemFichero) {
                                    	((ItemFichero)ficDestinoMapeo).fechaUltMod = fuente.lastModified();
                                    	((ItemFichero)ficDestinoMapeo).tamanyoBytes = fuente.length();
                                    }
                                } catch (IOException e) {
                                    inform( " ERROR de copia. Fichero más moderno no pudo copiarse sobre destino: ", fuente, destino, DPFTipoArbol.DPF_MODERNO_EN_FUENTE );
                                    inform( "", fuente, destino, DPFTipoArbol.DPF_ERRORES_COPIA );
                                    erroresCopia++;
                                    if (usaMapeoDestinoGuardado && ficDestinoMapeo!=null && ficDestinoMapeo instanceof ItemFichero && ficDestinoMapeo.nuevoEnMapa) {
                                		((ItemFichero)ficDestinoMapeo).fechaUltMod = modDateD;
                                		((ItemFichero)ficDestinoMapeo).tamanyoBytes = sizeD;
                                	}
                                }
                            }
                        }
                    } else {   // El fichero no existía: copiarlo  <dir>
                        if (windowForFeedback!=null) windowForFeedback.setProgreso( (int) Math.round((porcAcumuladoPrevio + porcAProcesarEnEstaLlamada)*100) );
                        if (tipoDeCopia == TipoCopia.TIPO_COMPARACION || tipoDeCopia == TipoCopia.TIPO_REVISION) {
                            inform( "  Fichero sólo en origen: ", fuente, null, DPFTipoArbol.DPF_SOLO_EN_FUENTE );
                            ficherosSoloEnOrigen++;
                        } else {
                            File dirDestino = new File( getPath( destino.getAbsolutePath() ) );
                            if (!dirDestino.isDirectory()) {
                                if (!createDirectory(dirDestino)) {
                                	// Reintentar unos minutos con feedback al usuario
                                	// Sacar y borrar el JOptionPane si el reintento funciona en algún momento
                            		(new Thread() {
                            			@Override
                            			public void run() {
                                        	long tiempoInicio = System.currentTimeMillis();
                                        	long tiempoReintento = 1000; // Empieza en un segundo y va incrementándose hasta 10 minutos
                                        	while( tiempoReintento < 600000L ) {
                                				try { Thread.sleep( tiempoReintento ); } catch (InterruptedException e) {}
                                                if (createDirectory(dirDestino)) {  // Se ha podido crear
                                    				eliminaJOptionPane( "Directorio destino no pudo crearse. Reintentando hasta 5 minutos o hasta que se acepte cancelación de proceso" );
                                                	return;
                                                }
                                        		tiempoReintento *= 2;
                                        		System.out.println( "Reintentando creación de carpeta " + dirDestino.getAbsolutePath() + " (" + (tiempoReintento/1000) + " sgs.)" );
                                        	}
                            				eliminaJOptionPane( "Directorio destino no pudo crearse. Reintentando hasta 5 minutos o hasta que se pulse cancelación de proceso" );
                            			}
                            		}).start();
                            		int resp = JOptionPane.showConfirmDialog( null, "Directorio destino no pudo crearse. Reintentando hasta 5 minutos o hasta que se pulse cancelación de proceso", "Error en destino", JOptionPane.CLOSED_OPTION );
                            		if (resp==JOptionPane.OK_OPTION || !dirDestino.isDirectory()) {  // Cierre de usuario o no se ha podido crear: acaba el proceso
                                        if (usaMapeoDestinoGuardado && ficDestinoMapeo!=null && ficDestinoMapeo instanceof ItemFichero && ficDestinoMapeo.nuevoEnMapa) {
                                        	ficDestinoMapeo.quitarDelPadre();
                                        }
                            			lanzaIOException( "syncAllDirsAndFiles: subdirectorio destino " + destino.getAbsolutePath() + " no pudo crearse" );
                            		}
                                }
                            }
                            try {
                                copyFile( fuente, destino );
                                ficherosNuevos++;
                                if (consoleFeedback && (ficherosNuevos % 250 == 0)) System.out.println( " " + String.format("%1$1.2f", (porcAcumuladoPrevio + porcAProcesarEnEstaLlamada)) + "% - " + ficherosNuevos + " ficheros nuevos..." );
                                inform( "  Fichero nuevo copiado: ", fuente, null, DPFTipoArbol.DPF_SOLO_EN_FUENTE );
                                if (usaMapeoDestinoGuardado && ficDestinoMapeo!=null && ficDestinoMapeo instanceof ItemFichero) {
                                	((ItemFichero)ficDestinoMapeo).fechaUltMod = fuente.lastModified();
                                	((ItemFichero)ficDestinoMapeo).tamanyoBytes = fuente.length();
                                }
                            } catch (IOException e) {
                                inform( " ERROR de copia. Fichero nuevo no pudo ser copiado a destino: ", fuente, destino, DPFTipoArbol.DPF_SOLO_EN_FUENTE );
                                inform( "", fuente, destino, DPFTipoArbol.DPF_ERRORES_COPIA );
                                erroresCopia++;
                                if (usaMapeoDestinoGuardado && ficDestinoMapeo!=null && ficDestinoMapeo instanceof ItemFichero && ficDestinoMapeo.nuevoEnMapa) {
                                	ficDestinoMapeo.quitarDelPadre();
                                }
                            }
                        }
                    }
                }
            } else {
                // System.out.println( "*** Excluding file: " + fuente.getAbsolutePath() );
                inform( fuente, DPFTipoArbol.DPF_IGNORADOS_EN_FUENTE );
            }
        }
    }
    
			private static void eliminaJOptionPane( String mensaje ) {
				Window[] windows = Window.getWindows();
		        for (Window window : windows) {
		            if (window instanceof JDialog) {
		                JDialog dialog = (JDialog) window;
		                if (dialog.getContentPane().getComponentCount() == 1
		                    && dialog.getContentPane().getComponent(0) instanceof JOptionPane){
		                	if (mensaje==null || ((JOptionPane) dialog.getContentPane().getComponent(0)).getMessage().equals(mensaje)) {
		                        dialog.dispose();
		                	}
		                }
		            }
		        }
			}

        static String antDirFuente = "";
        static String antDirDest = "";
        static String antMens = "";

        static private void inform( String mens, File fuente, File destino, DPFTipoArbol tipoArbol ) {
            if (mens != null || tipoArbol == null) {
                if (mens != null && !mens.equals("")) {
                    String dirFuente = (fuente==null) ? "" : getPath( fuente.getAbsolutePath() );
                    String dirDest = (destino==null) ? "" : getPath( destino.getAbsolutePath() );
                    if (mens != null && mens.equals(antMens)) { mens = "\t"; } else { antMens = mens; }
                    mens = mens + "\t" + (dirFuente.equals(antDirFuente) ? "\t" : "["+dirFuente+"] ") + getName( fuente==null?"":fuente.getAbsolutePath() ) +
                                ((destino==null) ? "" : ("\t" + (dirDest.equals(antDirDest) ? "" : "["+dirDest+"] ") + getName(destino.getAbsolutePath())));
                    Debug.show( mens );
                    antDirFuente = dirFuente;
                    antDirDest = dirDest;
                }
            }
            if (windowForFeedback != null) {
                if (mens != null && !mens.equals("")) windowForFeedback.anyadeZonaMensajes( mens, true );
                if (tipoArbol != null) {
                    if (DPFTipoArbol.sonDeFuente[ tipoArbol.ordinal() ]) {
                        windowForFeedback.nuevoFichero( fuente.getAbsolutePath(), tipoArbol, fuente.length() );
                    } else {
                    	if (tipoArbol==DPFTipoArbol.DPF_ERRORES_COPIA) {
                    		windowForFeedback.nuevoFichero( destino.getAbsolutePath(), tipoArbol, destino.length(), fuente.getAbsolutePath() );  // Si es de error se almacena el fichero fuente, para poder reintentar luego
                    	} else {
                    		windowForFeedback.nuevoFichero( destino.getAbsolutePath(), tipoArbol, destino.length() );
                    	}
                    }
                }
            }
        }

        static private void inform( String mens, File fuente, File destino ) {
            inform( mens, fuente, destino, null );
        }

        static private void inform( File fuenteODestino, DPFTipoArbol tipoArbol ) {
            if (tipoArbol==DPFTipoArbol.DPF_IGNORADOS_EN_DESTINO || tipoArbol==DPFTipoArbol.DPF_MODERNO_EN_DESTINO || tipoArbol==DPFTipoArbol.DPF_SOLO_EN_DESTINO)
                inform( null, null, fuenteODestino, tipoArbol );
            else
                inform( null, fuenteODestino, null, tipoArbol );
        }

        static private void inform( String mens, ItemArchivo fuente, ItemArchivo destino, DPFTipoArbol tipoArbol ) {
            if (mens != null || tipoArbol == null) {
                if (mens != null && !mens.equals("")) {
                    String dirFuente = (fuente==null) ? "" : getPath( fuente.getPathAbsoluto() );
                    String dirDest = (destino==null) ? "" : getPath( destino.getPathAbsoluto() );
                    if (mens != null && mens.equals(antMens)) { mens = "\t"; } else { antMens = mens; }
                    mens = mens + "\t" + (dirFuente.equals(antDirFuente) ? "\t" : "["+dirFuente+"] ") + getName( fuente==null?"":fuente.getPathAbsoluto() ) +
                                ((destino==null) ? "" : ("\t" + (dirDest.equals(antDirDest) ? "" : "["+dirDest+"] ") + getName(destino.getPathAbsoluto())));
                    Debug.show( mens );
                    antDirFuente = dirFuente;
                    antDirDest = dirDest;
                }
            }
            if (windowForFeedback != null) {
                if (mens != null && !mens.equals("")) windowForFeedback.anyadeZonaMensajes( mens, true );
                if (tipoArbol != null) {
                    if (DPFTipoArbol.sonDeFuente[ tipoArbol.ordinal() ]) {
                        windowForFeedback.nuevoFichero( fuente.getPathAbsoluto(), tipoArbol, ((fuente instanceof ItemFichero) ? ((ItemFichero)fuente).tamanyoBytes : 0) );
                    } else {
                    	if (tipoArbol==DPFTipoArbol.DPF_ERRORES_COPIA) {
                    		windowForFeedback.nuevoFichero( destino.getPathAbsoluto(), tipoArbol, ((destino instanceof ItemFichero) ? ((ItemFichero)destino).tamanyoBytes : 0), destino.getPathAbsoluto() );  // Si es de error se almacena el fichero fuente, para poder reintentar luego
                    	} else {
                    		windowForFeedback.nuevoFichero( destino.getPathAbsoluto(), tipoArbol, ((destino instanceof ItemFichero) ? ((ItemFichero)destino).tamanyoBytes : 0) );
                    	}
                    }
                }
            }
        }

        static private void inform( String mens, ItemArchivo fuente, ItemArchivo destino ) {
            inform( mens, fuente, destino, null );
        }
        
        static private void inform( ItemArchivo fuenteODestino, DPFTipoArbol tipoArbol ) {
            if (tipoArbol==DPFTipoArbol.DPF_IGNORADOS_EN_DESTINO || tipoArbol==DPFTipoArbol.DPF_MODERNO_EN_DESTINO || tipoArbol==DPFTipoArbol.DPF_SOLO_EN_DESTINO)
                inform( null, null, fuenteODestino, tipoArbol );
            else
                inform( null, fuenteODestino, null, tipoArbol );
        }

    /** Programa de prueba
     *
     * @param s
     */
    public static void main( String[] s ) {
        // Prueba de sort de array de Strings (ojo que el sort normal sí diferencia mayúsculas y minúsculas)
//        String[] pru = { "ZZ", "hola.txt", "vayaPorDios.hoaa", "Andoni", "00", "alberto", "1223", "---324dd", "-" };
//        System.out.println( Arrays.asList(pru) );
//        Arrays.sort( pru, String.CASE_INSENSITIVE_ORDER);
//        System.out.println( Arrays.asList(pru) );
//        Pattern p = Pattern.compile(".*\\.(jpg|jpeg|gif|png|bmp)|.*\\.doc", Pattern.CASE_INSENSITIVE);
        File ff1a = new File("d:/t");
        File ff2a = new File("d:/t/aborrarcarpeta");
        String[] fdirs1a = new String[] { "a", "i" };
        String[] fdirs2a = new String[] { "a", "i" };
        try {
            syncAllDirsAndFiles( 10000, ff1a, ff2a, fdirs1a, fdirs2a, null, null, null, TipoCopia.TIPO_COPIA_BACKUP, "BACKUP", false, true, 30 );
        } catch (IOException ex) {
            System.out.println( ex.getMessage() );
            ex.printStackTrace( System.out );
        }
        System.out.println( "Proceso finalizado." );
        try { Thread.sleep(5000); } catch (Exception e) {}
        System.exit(0);
        File ff1 = new File("d:");
        File ff2 = new File("f:/sync");
        String[] fdirs1 = new String[] { "internet", "JosuAnder" };
        String[] fdirs2 = new String[] { "internet", "JosuAnder" };
        try {
            syncAllDirsAndFiles( 10, ff1, ff2, fdirs1, fdirs2, null, null, null, TipoCopia.TIPO_COPIA_BACKUP, "", true, false, 30 );
        } catch (IOException ex) {
            System.out.println( ex.getMessage() );
            ex.printStackTrace( System.out );
        }
        System.out.println( "Proceso finalizado." );
        System.exit(0);
        File f1 = new File("C:/Users");
        File f2 = new File("d:/t/aborrar");
        String[] dirs1 = new String[2]; dirs1[0] = "andoni"; dirs1[1] = "All Users";
        String[] dirs2 = new String[2]; dirs2[0] = "andoni"; dirs2[1] = "All Users";
        try {
            syncAllDirsAndFiles( 10, f1, f2, dirs1, dirs2, null, ".*\\.(tmp|wbk)",
                    ".*(\\\\)andoni_tid(\\\\)icons|.*(\\\\)andoni_tid(\\\\)spool|.*(\\\\)eguiluz(\\\\)icons|.*(\\\\)eguiluz(\\\\)spool", TipoCopia.TIPO_COPIA_SINCRONIZACION, "BACKUP",true, false, 0 );
            // Easier: (\\\\) is the \    \\. is .
            // "*\.netbeans\*" in file pattern =   .* \ \\. netbeans \\ \\(.*)    in regular pattern
            // syncAllDirsAndFiles( 10, f1, f2, dirs1, dirs2, "m.*\\..*|n.*\\..*", ".*\\.(dat|log1|aum|jpg)",".*(\\\\)appdata|.*(\\\\)desktop|.*(\\\\).netbeans", TIPO_COPIA_SINCRONIZACION, "BACKUP",true );
            // syncAllDirsAndFiles( 10, f1, f2, dirs1, dirs2, TIPO_COPIA_SINCRONIZACION, "BACKUP",true);
            // syncAllDirsAndFiles( f1, f2, dirs1, dirs2, TIPO_COPIA_BACKUP, "BACKUP",true);
        } catch (IOException e) {
            System.out.println( e.getMessage() );
            e.printStackTrace( System.out );
        }
        System.out.println( "Proceso finalizado." );
        System.exit(0);
        File f = new File( "D:/t/AborrarCarpeta" );
        // File f = new File( "F:/backups/Andoni-20101001/c/Users/andoni");
        System.out.println( "=================================" );
        System.out.println( "=================================" );
        System.out.println( "Todo lo que hay en " + f.getAbsolutePath() + ":" );
        visitAllDirsAndFiles( f, 0 );
        System.out.println( "=================================" );
        System.out.println( "=================================" );
        System.out.println( "Todos los directorios que hay en " + f.getAbsolutePath() + ":" );
        visitAllDirs( f, 0 );
        System.out.println( "=================================" );
        System.out.println( "=================================" );
        System.out.println( "Todos los ficheros que hay en " + f.getAbsolutePath() + ":" );
        visitAllFiles( f, 0 );
    }
    
    // Clases para mapeo de ficheros/directorios en destino
    
    private abstract static class ItemArchivo implements Serializable {
		private static final long serialVersionUID = 1L;
    	String nombre;
    	boolean nuevoEnMapa;
    	ItemDirectorio padre;
    	public ItemArchivo( String nombre ) {
    		this( nombre, null );
    	}
    	public ItemArchivo( String nombre, ItemDirectorio dirPadre ) {
    		this.nombre = nombre;
    		nuevoEnMapa = true;
    		padre = dirPadre;
    	}
    	public String getPathAbsoluto() {
    		if (padre == null) {
    			return nombre;
    		} else {
    			return padre.getPathAbsoluto() + "\\" + nombre;
    		}
    	}
    	/** Quita este item del árbol de archivos mapeado
    	 */
    	public void quitarDelPadre() {
    		if (padre!=null) {
    			padre.contenido.remove( nombre );
    		}
    	}
    	/** Vuelca todo el contenido de este item en consola
    	 */
    	public void volcarEnConsola() {
    		nivel = "";
    		volcarEnConsolaRec();
    	}
    	protected abstract void volcarEnConsolaRec();
    	private static String nivel;
    	public static String getNivel() { return nivel; }
    	public static void setNivel(String nivel) { ItemArchivo.nivel = nivel; }
    }
    
    private static class ItemFichero extends ItemArchivo {
		private static final long serialVersionUID = 1L;
    	long tamanyoBytes;
    	long fechaUltMod;
    	public ItemFichero( String nombre ) {
    		super( nombre );
    	}
    	public ItemFichero( String nombre, ItemDirectorio dirPadre ) {
    		super( nombre, dirPadre );
    	}
    	public ItemFichero( String nombre, long tamanyoBytes, long fechaUltMod ) {
    		this( nombre );
    		this.tamanyoBytes = tamanyoBytes;
    		this.fechaUltMod = fechaUltMod;
    	}
    	public ItemFichero( String nombre, ItemDirectorio dirPadre, long tamanyoBytes, long fechaUltMod ) {
    		this( nombre, dirPadre );
    		this.tamanyoBytes = tamanyoBytes;
    		this.fechaUltMod = fechaUltMod;
    	}
    	protected void volcarEnConsolaRec() {
    		System.out.println( getNivel() + nombre + " (" + tamanyoBytes + ") " + sdf.format( new Date( fechaUltMod ) ) );
    	}
    }
    	private static SimpleDateFormat sdf = new SimpleDateFormat( "dd/MM/yyyy HH:mm:ss" );
    
    private static class ItemDirectorio extends ItemArchivo {
		private static final long serialVersionUID = 1L;
    	TreeMap<String,ItemArchivo> contenido;
    	public ItemDirectorio( String nombre ) {
    		super( nombre );
    		contenido = new TreeMap<>();
    	}
    	public ItemDirectorio( String nombre, ItemDirectorio dirPadre ) {
    		super( nombre, dirPadre );
    		contenido = new TreeMap<>();
    	}
    	public Collection<ItemArchivo> getContenido() {
    		return contenido.values();
    	}
    	/** Devuelve un item de archivo dentro del directorio
    	 * @param nombre	Nombre del item a buscar
    	 * @return	null si no existe, el item si lo hay con ese nombre en el directorio
    	 */
    	public ItemArchivo getItem( String nombre ) {
    		return contenido.get( nombre );
    	}
    	/** Añade un item de archivo al directorio
    	 * @param item	a añadir (este método modifica la información de padre de este ítem para ponerle el objeto en curso)
    	 * @return	null si no existía (lo normal), el item anterior si ya existía alguno con ese nombre
    	 */
    	public ItemArchivo addItem( ItemArchivo item ) {
    		item.padre = this;
    		return contenido.put( item.nombre, item );
    	}
    	/** Elimina un archivo del directorio
    	 * @param nombre	Nombre del archivo a eliminar
    	 */
    	public void removeItem( String nombre ) {
    		contenido.remove( nombre );
    	}
    	protected void volcarEnConsolaRec() {
    		System.out.println( getNivel() + nombre );
    		setNivel( getNivel() + "  " );
    		for (ItemArchivo item : contenido.values()) {
    			item.volcarEnConsolaRec();
    		}
    		setNivel( getNivel().substring( 0, getNivel().length()-1 ) );
    	}
    }
    
}
