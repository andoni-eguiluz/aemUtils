package net.eguiluz.aem.utils;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;
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
        copyFile( src, dst, false );
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

    /** Borra un directorio, borrando todos los ficheros y subdirectorios por debajo.<p>
     * ¡Usar con precaución!<p>
     * Si falla algo en el borrado, el método finaliza sin intentar borrar los siguientes elementos.
     * @param dir
     * @return  true si el borrado ocurrió correctamente, false en caso contrario
     */
    public static boolean deleteDir( File dir ) {
        if (!dir.isDirectory()) return false;
        String[] children = dir.list();
        for (int i=0; i< children.length; i++) {
            boolean success = deleteDir(new File(dir, children[i]));
            if (!success) {
                return false;
            }
        }
        return true;
    }
    // The directory is now empty so delete it returns dir.delete(); }


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
        if (tipoDeCopia == TipoCopia.TIPO_COMPARACION) {
        	Files.createDirectory( new File( "logs" ).toPath() );
        	Debug.activateDebugLog( "logs/AEsync-compare" ); // creates logs/AEsync-compare.log
        }
        else {
        	Files.createDirectory( new File( "logs" ).toPath() );
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
                                    syncAllDirsAndFiles( fuente, destino, dirF, dirD, tipoDeCopia, respaldo, consoleFeedback, 100.0*i/subdirsFuente.length, 100.0/subdirsFuente.length );
                                }
                            }
                        }
                    } catch (IOException ex) {
                        // IOException
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



    private static void syncAllDirsAndFiles( File fuente, File destino, String pathRelF, String pathRelD, TipoCopia tipoDeCopia, File dirRespaldo, boolean consoleFeedback,
                                             double porcAcumuladoPrevio, double porcAProcesarEnEstaLlamada ) throws IOException {
        // andoni/AppData/Roaming/Apple Computer/MobileSync/Backup/a01cd4be4fa75a92b4aa0e6e1e8db1a4b5fe8788/f481a5af250808bb20eeeee5fc609b6e030b4164
        // if (consoleFeedback && (ficherosIguales >= 0)) System.out.println( fuente + " # " + destino + " # " + pathRelF + " # " + pathRelD );
        if (pathRelF.contains("a01cd4be4fa75a92b4aa0e6e1e8db1a4b5fe8788")) return;  // TODO: Muy raro. Casca en este directorio (algo de Apple)
        // if (pathRelF.contains("Adobe Audition 2.0 Loopology")) return;  // TODO: Muy raro. Casca en este directorio (algo de Apple)
        if (cancelSyncProcess) return;
        // Proceso de directorio o fichero
        if (fuente == null) {
            // Caso especial no existe en fuente pero s� en destino (solo procesado si es BACKUP o COMPARACION)
            if (tipoDeCopia == TipoCopia.TIPO_COPIA_BACKUP || tipoDeCopia == TipoCopia.TIPO_COMPARACION) {
                if (destino.isDirectory()) {
                    if (tipoDeCopia == TipoCopia.TIPO_COMPARACION) inform( "  Directorio solo en destino: ", fuente, destino, DPFTipoArbol.DPF_SOLO_EN_DESTINO );
                    String[] childrenDest = destino.list();
                    int i = 0;
                    for (String sd : childrenDest) {
                        syncAllDirsAndFiles( null, new File(destino,sd), "", pathRelD+"/"+sd, tipoDeCopia, dirRespaldo, consoleFeedback, porcAcumuladoPrevio+porcAProcesarEnEstaLlamada*i/childrenDest.length, porcAProcesarEnEstaLlamada/childrenDest.length );
                        i++;
                    }
                } else {  // Fichero
                	System.out.println( "Solo en destino: " + destino );
                    if (tipoDeCopia == TipoCopia.TIPO_COMPARACION) {
                        if (includeFile(destino.getName()) && !excludeFile(destino.getName())) {
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
                String[] childrenDest = (destino==null) ? null : destino.list();
                if (children == null) children = new String[0];
                if (childrenDest == null) childrenDest = new String[0];
                Arrays.sort( children, String.CASE_INSENSITIVE_ORDER );
                Arrays.sort( childrenDest, String.CASE_INSENSITIVE_ORDER );
                int iF = 0;
                int iD = 0;
                // Recorre ambos arrays pareando las secuencias y con null donde no hay pareo
                while (iF < children.length || iD < childrenDest.length) {
                    if (iD >= childrenDest.length || (iF < children.length && children[iF].compareToIgnoreCase(childrenDest[iD])<0)) {  // El de fuente no está en destino (f < d)
                        // Crear el elemento del fuente también en el destino (salvo en comparación)
                        syncAllDirsAndFiles( new File(fuente, children[iF]), new File(destino, children[iF]), pathRelF+"/"+children[iF], pathRelD+"/"+children[iF], tipoDeCopia, dirRespaldo, consoleFeedback
                                , porcAcumuladoPrevio+porcAProcesarEnEstaLlamada*(iF+iD)/(children.length+childrenDest.length), porcAProcesarEnEstaLlamada/(children.length+childrenDest.length) );
                        iF++;
                    } else if (iF >= children.length || (iD < childrenDest.length && children[iF].compareToIgnoreCase(childrenDest[iD])>0)) {  // El de destino no está en fuente (f > d)
                        if (tipoDeCopia == TipoCopia.TIPO_COPIA_BACKUP) {   // Si es sincronización o restore no hay nada que hacer, es un destino que no está en el fuente
                            // Backup: el destino debe moverse a DELETED_FILES
                            syncAllDirsAndFiles( null, new File(destino, childrenDest[iD]), "", pathRelD+"/"+childrenDest[iD], tipoDeCopia, dirRespaldo, consoleFeedback
                                    , porcAcumuladoPrevio+porcAProcesarEnEstaLlamada*(iF+iD)/(children.length+childrenDest.length), porcAProcesarEnEstaLlamada/(children.length+childrenDest.length) );
                        } else if (tipoDeCopia == TipoCopia.TIPO_COMPARACION) {   // Si es comparación sigue comparando
                            syncAllDirsAndFiles( null, new File(destino, childrenDest[iD]), "", pathRelD+"/"+childrenDest[iD], tipoDeCopia, dirRespaldo, consoleFeedback
                                    , porcAcumuladoPrevio+porcAProcesarEnEstaLlamada*(iF+iD)/(children.length+childrenDest.length), porcAProcesarEnEstaLlamada/(children.length+childrenDest.length) );
                        }
                        iD++;
                    } else { // fuente y destino coinciden (f = d)
                        syncAllDirsAndFiles( new File(fuente, children[iF]), new File( destino, childrenDest[iD] ), pathRelF+"/"+children[iF], pathRelD+"/"+childrenDest[iD], tipoDeCopia, dirRespaldo, consoleFeedback
                                , porcAcumuladoPrevio+porcAProcesarEnEstaLlamada*(iF+iD)/(children.length+childrenDest.length), porcAProcesarEnEstaLlamada*2/(children.length+childrenDest.length) );
                        iF++;
                        iD++;
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
                return;
            }
            if (nobiggerFilesThanMbytes > 0 && fuente.length() > nobiggerFilesThanMbytes*1048576L) { // If bigger than Mbytes indicated, it is not copied (saved for later info)
                // If bigger than Mbytes indicated, it is not copied (saved for later info)
                ficherosIgnorados++;
                bigFilesNotCopied.add(fuente.getAbsolutePath());
                inform( fuente, DPFTipoArbol.DPF_IGNORADOS_EN_FUENTE );
                inform( "", fuente, fuente, DPFTipoArbol.DPF_FICHEROS_GRANDES );
                return;
            }
            if (includeFile(fuente.getName()) && !excludeFile(fuente.getName())) {
                if (!fuente.canRead() && (tipoDeCopia != TipoCopia.TIPO_COMPARACION)) {  // Fichero bloqueado
                    ficherosBloqueados++;
                    inform( " ERROR de copia. Fichero bloqueado no se ha copiado: ", fuente, null, DPFTipoArbol.DPF_MODERNO_EN_DESTINO );   // Suponemos moderno en destino
                    inform( "", fuente, fuente, DPFTipoArbol.DPF_ERRORES_COPIA );
                } else if (destino == null) {  // No debería ocurrir
                    lanzaIOException( "syncAllDirsAndFiles: subdirectorio destino nulo correspondiente a " + fuente.getAbsolutePath() );
                } else {
                    // Hay que copiar un fichero nuevo, o son iguales (o se comparan)
                    if (destino.exists()) {  // El fichero ya existía. Comprobar y comparar los atributos de fuente y destino
                        long modDateF = fuente.lastModified();
                        long modDateD = destino.lastModified();
                        long sizeF = fuente.length();
                        long sizeD = destino.length();
                        if (modDateF == modDateD || sizeF == sizeD) {  // Son iguales, nada que hacer
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
                                inform( "  Fichero en origen posterior al destino: ", fuente, destino, DPFTipoArbol.DPF_MODERNO_EN_FUENTE );
                                ficherosDiferentes++;
                            } else {  // Salvo que sea comparación o revisión, se hace la copia
                                try {
                                    copyFile( fuente, destino );
                                    inform( "  Reemplazo de fichero más moderno: ", fuente, null, DPFTipoArbol.DPF_MODERNO_EN_FUENTE );
                                    ficherosReemplazados++;
                                } catch (IOException e) {
                                    inform( " ERROR de copia. Fichero más moderno no pudo copiarse sobre destino: ", fuente, destino, DPFTipoArbol.DPF_MODERNO_EN_FUENTE );
                                    inform( "", destino, destino, DPFTipoArbol.DPF_ERRORES_COPIA );
                                    erroresCopia++;
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
                                if (!createDirectory(dirDestino))
                                    lanzaIOException( "syncAllDirsAndFiles: subdirectorio destino " + destino.getAbsolutePath() + " no pudo crearse" );
                            }
                            try {
                                copyFile( fuente, destino );
                                ficherosNuevos++;
                                if (consoleFeedback && (ficherosNuevos % 250 == 0)) System.out.println( " " + String.format("%1$1.2f", (porcAcumuladoPrevio + porcAProcesarEnEstaLlamada)) + "% - " + ficherosNuevos + " ficheros nuevos..." );
                                inform( "  Fichero nuevo copiado: ", fuente, null, DPFTipoArbol.DPF_SOLO_EN_FUENTE );
                            } catch (IOException e) {
                                inform( " ERROR de copia. Fichero nuevo no pudo ser copiado a destino: ", fuente, destino, DPFTipoArbol.DPF_SOLO_EN_FUENTE );
                                inform( "", destino, destino, DPFTipoArbol.DPF_ERRORES_COPIA );
                                erroresCopia++;
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
                    if (DPFTipoArbol.sonDeFuente[ tipoArbol.ordinal() ])
                        windowForFeedback.nuevoFichero( fuente.getAbsolutePath(), tipoArbol, fuente.length() );
                    else
                        windowForFeedback.nuevoFichero( destino.getAbsolutePath(), tipoArbol, destino.length() );
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
}
