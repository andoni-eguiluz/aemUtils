package net.eguiluz.aem.utils.gui;
import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import javax.swing.border.TitledBorder;
import javax.swing.event.*;
import javax.swing.text.BadLocationException;
import javax.swing.tree.*;

import net.eguiluz.aem.utils.DirProcess;
import net.eguiluz.aem.utils.PopupMenu;
import net.eguiluz.aem.utils.TipoCopia;

/** Ventana principal de AEsync para funcionamiento interactivo
 * @author andoni
 */
public class DirProcessFrame extends JFrame implements TreeSelectionListener, MouseListener {
    // Atributos de interfaz
    private JTextField tfDirFuente = new JTextField();
    private JTextField tfDirDestino = new JTextField();
    private JLabel lMensaje = new JLabel(" ", JLabel.CENTER);
    private JLabel lProgreso = new JLabel("%");
    private JLabel lTamanyo = new JLabel("");
    private JTextArea taMensajes = new JTextArea();
    private JLabel lTiempo = new JLabel("");
    private boolean soloConteoIgnorados = true;
    private long numBytesCopiados = 0;
    private long horaInicio = System.currentTimeMillis();

    // Atributos de acción
    private JButton bPausar = new JButton( "Pausa" );
    private JButton bCerrarCancelar = new JButton( "Cancelar" );
    private JComboBox cbArbolesNoVistos = new JComboBox();
    private JComboBox cbVerOtroArbol = new JComboBox( new String[] { "Ventana 1", "Ventana 2", "Ventana 3", "Ventana 4", "Ventana 5", "Ventana 6" } );
    private JProgressBar pbProgreso = new JProgressBar( SwingConstants.HORIZONTAL, 0, 10000 );
    private JProgressBar pbProgresoCopia = new JProgressBar( SwingConstants.HORIZONTAL, 0, 10000 );
    private JSplitPane pCentro;
    private JSplitPane pCentro23;
    private JSplitPane pCentro1;
    private JSplitPane pCentro2;
    private JSplitPane pCentro3;
    private JTextField tfIgnoradosOrigen = new JTextField(5);
    private JTextField tfIgnoradosDestino = new JTextField(5);

    private JPanel[] pArbol = new JPanel[DPFTipoArbol.values().length];

        private void crearPanelDeJTree( DPFTipoArbol tipoArbol ) {
            pArbol[tipoArbol.ordinal()] = new JPanel( new BorderLayout() );
            pArbol[tipoArbol.ordinal()].setBorder( BorderFactory.createTitledBorder( DPFTipoArbol.nombreTipo[tipoArbol.ordinal()]));
            pArbol[tipoArbol.ordinal()].add( new JScrollPane( arbol[tipoArbol.ordinal()] ) );
        }
        private void muestraNumeroFicherosEnPanel( DPFTipoArbol tipoArbol ) {
            TitledBorder tb = (TitledBorder) pArbol[tipoArbol.ordinal()].getBorder();
            tb.setTitle( DPFTipoArbol.nombreTipo[tipoArbol.ordinal()] + " (" + numNodos[tipoArbol.ordinal()] + ") [" 
            		+ bytesTotales[tipoArbol.ordinal()]/1024 + " Kb]" );
            pArbol[tipoArbol.ordinal()].repaint();
            // System.out.println( DPFTipoArbol.nombreTipo[tipoArbol.ordinal()] + " (" + numNodos[tipoArbol.ordinal()] + ")" );
        }
        // Ver el árbol que se indica en la pantalla indicada (1 a 3 arriba, 4 a 6 abajo)
        private void verPanelArbolEnPantalla( DPFTipoArbol tipoArbol, int numPantalla) {
            if (numPantalla < 1 || numPantalla > 6) return;
            Component c = (tipoArbol==null) ? null : pArbol[tipoArbol.ordinal()];
            switch (numPantalla) {
                case 1: { pCentro1.setTopComponent( c ); break; }
                case 2: { pCentro2.setTopComponent( c ); break; }
                case 3: { pCentro3.setTopComponent( c ); break; }
                case 4: { pCentro1.setBottomComponent( c ); break; }
                case 5: { pCentro2.setBottomComponent( c ); break; }
                case 6: { pCentro3.setBottomComponent( c ); break; }
            }
        }
        // Buscar ventana que no se ve ahora, devuelve su posición según DPFTipoArbol (-1 si hay error)
        private void recalcVentanasNoEnPantalla() {
            boolean ventanaEnPantalla[] = new boolean[ DPFTipoArbol.values().length ];
            for (int i = 0; i<6; i++) {
                Component c = null;
                switch (i) {
                    case 0: { c = pCentro1.getTopComponent(); break; }
                    case 1: { c = pCentro2.getTopComponent(); break; }
                    case 2: { c = pCentro3.getTopComponent(); break; }
                    case 3: { c = pCentro1.getBottomComponent(); break; }
                    case 4: { c = pCentro2.getBottomComponent(); break; }
                    case 5: { c = pCentro3.getBottomComponent(); break; }
                }
                for (int k = 0; k<ventanaEnPantalla.length; k++) {
                    if (c == pArbol[k]) { ventanaEnPantalla[k] = true; break; }
                }
            }
            cbArbolesNoVistos.removeAllItems();
            cbArbolesNoVistos.addItem( "Vacío" );
            for (int i = 0; i<ventanaEnPantalla.length; i++) {
                if (!ventanaEnPantalla[i]) {
                    if (!soloConteoIgnorados || (DPFTipoArbol.values()[i]!=DPFTipoArbol.DPF_IGNORADOS_EN_FUENTE && DPFTipoArbol.values()[i]!=DPFTipoArbol.DPF_IGNORADOS_EN_DESTINO))
                        cbArbolesNoVistos.addItem( DPFTipoArbol.nombreTipo[i] );
                }
            }
        }

    /** Constructor de ventana de seguimiento de proceso de sincronización
     * @param dirFuente
     * @param dirDestino
     * @param soloConteoIgnorados
     */
    public DirProcessFrame( File dirFuente, File dirDestino, boolean soloConteoIgnorados ) {
        // Inicialización de la ventana
//        try {
//            UIManager.setLookAndFeel( "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel" );
//            //UIManager.setLookAndFeel("javax.swing.plaf.metal.MetalLookAndFeel");
//        } catch (Exception ex) { ex.printStackTrace(); }
//		SwingUtilities.updateComponentTreeUI( this );
        // setDefaultLookAndFeelDecorated(true);
        setTitle( "AEsync: Informe de sincronización de ficheros");
        setMinimumSize( new Dimension( 760, 450 ));
        setPreferredSize( new Dimension( 950, 720 ));
        setDefaultCloseOperation( JFrame.DO_NOTHING_ON_CLOSE );  // No se cierra la ventana - salvo que se acabe
        getRootPane().setDefaultButton( bCerrarCancelar );
        this.soloConteoIgnorados = soloConteoIgnorados;
        tfDirFuente.setText( dirFuente.getAbsolutePath() );
        tfDirDestino.setText( dirDestino.getAbsolutePath() );
        tfDirFuente.setEditable( false );
        tfDirDestino.setEditable( false );
        taMensajes.setEditable( false );
        taMensajes.setTabSize(3);
        pbProgreso.setValue( 0 );
        pbProgreso.setStringPainted(true);
        pbProgreso.setPreferredSize( new Dimension( 75, 20 ) );
        pbProgresoCopia.setValue( 0 );
        pbProgresoCopia.setStringPainted(true);
        pbProgresoCopia.setPreferredSize( new Dimension( 75, 20 ) );
        pbProgresoCopia.setEnabled( false );
        tfIgnoradosOrigen.setEditable( false );
        tfIgnoradosDestino.setEditable( false );

        // Inic. JTrees y sus paneles
        for (DPFTipoArbol ta : DPFTipoArbol.values()) {
            if (DPFTipoArbol.sonDeFuente[ta.ordinal()])
                arbol[ta.ordinal()] = crearArbol( dirFuente.getAbsolutePath(), ta );
            else
                arbol[ta.ordinal()] = crearArbol( dirDestino.getAbsolutePath(), ta );
            crearPanelDeJTree(ta);
        }

        // Paneles
        JPanel pNorte = new JPanel( new BorderLayout() );
            JPanel pNorte1 = new JPanel();
                pNorte1.setLayout( new BoxLayout( pNorte1, BoxLayout.Y_AXIS ));
                pNorte1.setBorder( BorderFactory.createTitledBorder( "Discos" ));
                pNorte1.setMaximumSize( new Dimension( 1000, 30 ));   // 30 pixs de alto max
                JPanel pNorteA = new JPanel();
                    pNorteA.setLayout( new BoxLayout( pNorteA, BoxLayout.X_AXIS ));
                    pNorteA.add( new JLabel( "Origen:") );
                    pNorteA.add( tfDirFuente );
                pNorte1.add( pNorteA );
                JPanel pNorteB = new JPanel();
                    pNorteB.setLayout( new BoxLayout( pNorteB, BoxLayout.X_AXIS ));
                    pNorteB.add( new JLabel( "Destino:") );
                    pNorteB.add( tfDirDestino );
                pNorte1.add( pNorteB );
            pNorte.add( pNorte1, "West" );
            JPanel pNorte2 = new JPanel();
                pNorte2.setLayout( new BoxLayout( pNorte2, BoxLayout.Y_AXIS ) );
                pNorte2.setBorder( BorderFactory.createTitledBorder( "Acciones" ));
                JPanel pNorte2a = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
                    pNorte2a.add( bCerrarCancelar );
                    pNorte2a.add( new JLabel( "Ver" ) );
                    pNorte2a.add( cbArbolesNoVistos );
                    pNorte2a.add( new JLabel( "en" ) );
                    pNorte2a.add( cbVerOtroArbol );
                    pNorte2a.add( lTamanyo );
                    pNorte2a.add( lProgreso );
                    pNorte2a.add( pbProgreso );
                    pNorte2a.add( lTiempo );
                    pNorte2a.add( bPausar );
                pNorte2.add( pNorte2a );
                JPanel pNorte2b = new JPanel( new FlowLayout( FlowLayout.LEFT ) );
                    pNorte2b.add( new JLabel( "Ignorados en origen:") );
                    pNorte2b.add( tfIgnoradosOrigen );
                    pNorte2b.add( new JLabel( "en destino:") );
                    pNorte2b.add( tfIgnoradosDestino );
                    pNorte2b.add( new JLabel( " Progreso copia" ) );
                    pNorte2b.add( pbProgresoCopia );
                pNorte2.add( pNorte2b );
            pNorte.add( pNorte2, "Center" );
        getContentPane().add( pNorte, "North" );
        pCentro = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
            pCentro.setDividerLocation( 0.5 );
            pCentro1 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
                pCentro1.setPreferredSize( new Dimension( 1000, 200 ));
                pCentro1.setDividerLocation( 0.5 );
                verPanelArbolEnPantalla(DPFTipoArbol.DPF_SOLO_EN_FUENTE, 1);
                verPanelArbolEnPantalla(DPFTipoArbol.DPF_SOLO_EN_DESTINO, 4);
//                pCentro1.setTopComponent( pArbol[DPFTipoArbol.DPF_SOLO_EN_FUENTE.ordinal()] );
//                pCentro1.setBottomComponent( pArbol[DPFTipoArbol.DPF_SOLO_EN_DESTINO.ordinal()] );
            pCentro23 = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true);
                pCentro23.setDividerLocation( 0.5 );
                pCentro2 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
                    pCentro2.setPreferredSize( new Dimension( 1000, 200 ));
                    pCentro2.setDividerLocation( 0.5 );
                    verPanelArbolEnPantalla(DPFTipoArbol.DPF_MODERNO_EN_FUENTE, 2);
                    verPanelArbolEnPantalla(DPFTipoArbol.DPF_MODERNO_EN_DESTINO, 5);
//                    pCentro2.setTopComponent( pArbol[DPFTipoArbol.DPF_MODERNO_EN_FUENTE.ordinal()] );
//                    pCentro2.setBottomComponent( pArbol[DPFTipoArbol.DPF_MODERNO_EN_DESTINO.ordinal()] );
                pCentro3 = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true);
                    pCentro3.setPreferredSize( new Dimension( 1000, 200 ));
                    pCentro3.setDividerLocation( 0.5 );
                    verPanelArbolEnPantalla(DPFTipoArbol.DPF_IGUALES, 3);
                    verPanelArbolEnPantalla(DPFTipoArbol.DPF_ERRORES_COPIA, 6);
//                    pCentro3.setTopComponent( pArbol[DPFTipoArbol.DPF_IGNORADOS_EN_FUENTE.ordinal()] );
//                    pCentro3.setBottomComponent( pArbol[DPFTipoArbol.DPF_IGNORADOS_EN_DESTINO.ordinal()] );
                pCentro23.setLeftComponent( pCentro2 );
                pCentro23.setRightComponent( pCentro3 );
            pCentro.setLeftComponent( pCentro1 );
            pCentro.setRightComponent( pCentro23 );
        getContentPane().add( pCentro, "Center" );
        JPanel pSur = new JPanel( new BorderLayout() );
            pSur.setBorder( BorderFactory.createMatteBorder(2, 2, 2, 2, Color.BLUE ) );
            JScrollPane spMensajes = new JScrollPane( taMensajes, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED );
                spMensajes.setPreferredSize( new Dimension( 1000, 100 ));
            pSur.add( spMensajes, "Center" );
            pSur.add( lMensaje, "South" );
        getContentPane().add( pSur, "South" );

        // Listeners
        bCerrarCancelar.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (bCerrarCancelar.getText().equals( "Cancelar")) {  // Estado del botón: cancelar sincronización
                    Object[] opciones;
                    if (DirProcess.programIsCopyingNow())
                        opciones = new String[] {"Sí", "Volver", "Cancelar copia de fichero en curso" };
                    else
                        opciones = new String[] {"Sí", "Volver" };
                    int respuesta = JOptionPane.showOptionDialog( null, "Ha solicitado cancelar el proceso de sincronización. ¿Estás seguro?", "Confirmación de cancelado de sincronización",
                        JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[1]);
                    if (respuesta == 2) DirProcess.cancelCopy(); // Cancela sólo la copia de fichero
                    if (respuesta == 0) { // Cancela la sincronización a medias
    					DirProcess.pausaSync( false );
                        DirProcess.cancelSync();
                        DirProcess.freeConsole();
                        bCerrarCancelar.setText( "Cerrar" );
                    }
                } else {   // Estado del botón: cerrar (ventana)
					DirProcess.pausaSync( false );
                    DirProcess.freeConsole();
                    dispose();
                }
            }
        } );
        bPausar.addActionListener( new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				if (bPausar.getText().equals("Pausa")) {
					bPausar.setText( "Pausando..." );
					DirProcess.pausaSync( true );
					(new Thread() {
						public void run() {
							while (bPausar.getText().equals("Pausando...")) {
								try { Thread.sleep( 1000 ); } catch (InterruptedException e) { }
								if (DirProcess.isPausedSync()) {
									bPausar.setText( "Continuar" );
								}
							}
						}
					}).start();
				} else {
					DirProcess.pausaSync( false );
					bPausar.setText( "Pausa" );
				}
			}
		});
        cbVerOtroArbol.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (cbVerOtroArbol.getSelectedIndex() >= 0 && cbArbolesNoVistos.getSelectedIndex() >= 0) {
                    // Poner la ventana que no está en pantalla en la ventana seleccionada en el combo
                    int ventanaNoEnPantalla = DPFTipoArbol.valueOfDescription( cbArbolesNoVistos.getSelectedItem().toString() );
                    if (ventanaNoEnPantalla >= 0) {
                        verPanelArbolEnPantalla( DPFTipoArbol.values()[ventanaNoEnPantalla], cbVerOtroArbol.getSelectedIndex()+1 );
                    } else {  // Vacío
                        verPanelArbolEnPantalla( null, cbVerOtroArbol.getSelectedIndex()+1 );
                    }
                    recalcVentanasNoEnPantalla();
                }
            }
        });
        pCentro.setResizeWeight( 0.33 );
        pCentro23.setResizeWeight( 0.5 );
        pCentro1.setResizeWeight( 0.5 );
        pCentro2.setResizeWeight( 0.5 );
        pCentro3.setResizeWeight( 0.5 );
        recalcVentanasNoEnPantalla();
        setSize( 960, 650 );
        setLocationRelativeTo(null);
        setVisible(true);
        for (JTree a : arbol) {
            a.addTreeSelectionListener( this );
            a.addMouseListener( this );
        }
        // Hilo de progreso de copia
        Thread hiloProgCopia = new Thread() {
        	public void run() {
        		while (true) {
        			try { Thread.sleep( 500 ); } catch (InterruptedException e) { }
        			muestraProgresoCopia();
        		}
        	}
        };
        hiloProgCopia.setDaemon( true );
        hiloProgCopia.start();
    }
    
    // Datos y métodos de progreso de copia
    private long totalTiempoCopia = 0;
    private long totalBytesCopia = 0;
    private long timeStampCopia;
    private long timeStampCopiaActual;
    private long ultimoTamanyoBytes;
    
    /** Informa de inicio de copia de fichero. Activa el progressbar de copia en ficheros por encima de 1 Mb
     * Si hay varias copias concurrentes solo considera una (la primera que se empiece)
     * @param tamanyoBytes	Tamaño del fichero que se empieza a copiar
     */
    public synchronized void inicioCopia( long tamanyoBytes ) {
    	if (ultimoTamanyoBytes!=0) return;
    	ultimoTamanyoBytes = tamanyoBytes;
    	timeStampCopia = System.currentTimeMillis();
    	timeStampCopiaActual = System.currentTimeMillis();
    }
    
    /** Informa de fin de copia (del último fichero que se estaba copiando). Desactiva el progressbar de copia si lo estuviera.
     * @param tamanyoBytes	Tamaño del fichero que se acaba de copiar
     */
    public synchronized void finCopia( long tamanyoBytes ) {
    	if (ultimoTamanyoBytes!=tamanyoBytes) return;
    	ultimoTamanyoBytes = 0;
    	totalBytesCopia += tamanyoBytes;
    	totalTiempoCopia += (System.currentTimeMillis() - timeStampCopia);
    	pbProgresoCopia.setValue( 0 );
    	pbProgresoCopia.setEnabled( false );
    }
    
    private synchronized void muestraProgresoCopia() {
    	if (ultimoTamanyoBytes>0 && totalTiempoCopia>2000) {  // Hasta que no haya estado 2 sgs de copia no lo consideramos suficiente muestra
    		double MsPorByte = totalTiempoCopia * 1.0 / totalBytesCopia;
    		double msEsperados = ultimoTamanyoBytes * MsPorByte;
    		double msTranscurridos = (System.currentTimeMillis() - timeStampCopiaActual);
    		double porcentajeAproximado = msTranscurridos / msEsperados;
    		if (porcentajeAproximado>1.0) return;
    		int porcentajeAproximadoEntero = (int) Math.round( porcentajeAproximado * 10000 );
    		if (porcentajeAproximadoEntero>0) {
        		pbProgresoCopia.setEnabled( true );
        		pbProgresoCopia.setValue( porcentajeAproximadoEntero );
    		}
    	}
    }
    

        private String getFileNameFromPath( TreePath tp ) {
            String ret = "";
            Object[] path = tp.getPath();
            for (Object o : path) {
                ret = ret + o + "\\";
            }
            DefaultMutableTreeNode ultimoNodo = (DefaultMutableTreeNode) path[path.length-1];
            if (ultimoNodo.isLeaf()) ret = ret.substring( 0, ret.length()-1 );
            return ret;
        }

        private int getTreeIndex( JTree t ) {
            for (int i = 0; i < arbol.length; i++) {
                if (arbol[i] == t) return i;
            }
            return -1;
        }

        // null si componente no es JTree correcto
        private DPFTipoArbol getTipoArbol( Component c ) {
            if (!(c instanceof JTree)) return null;
            JTree actualTree = (JTree) c;
            int indexTree = getTreeIndex( actualTree );
            if (indexTree == -1) return null;
            return DPFTipoArbol.values()[indexTree];
        }

        private boolean isLeaf( TreePath tp ) {
            Object[] busqPath = tp.getPath();
            if (busqPath[busqPath.length-1] instanceof DefaultMutableTreeNode) {
                DefaultMutableTreeNode node = (DefaultMutableTreeNode) busqPath[busqPath.length-1];
                return (node.isLeaf());
            } else {
                throw new IllegalArgumentException( "Not a correct TreePath: " + tp );
            }
        }

            private int borraPathRec( String fileNameDePath, DefaultMutableTreeNode nodoABorrar ) {
                int cont = 0;
                if (!nodoABorrar.isLeaf()) {
                    int numHijos = nodoABorrar.getChildCount();
                    for (int i = 0; i<numHijos; i++) {
                        DefaultMutableTreeNode nodoHijo = (DefaultMutableTreeNode) nodoABorrar.getChildAt(i);
                        if (nodoHijo.isLeaf()) {  // Fichero - borrarlo
                            File f = new File( fileNameDePath + nodoHijo );
                            if (DirProcess.deleteFile( f )) {
                            	cont++;
                            }
                        } else {  // Carpeta - borrado recursivo
                            cont += borraPathRec( fileNameDePath + nodoHijo + "\\", nodoHijo );
                        }
                    }
                    File dir = new File( fileNameDePath );
                    DirProcess.deleteSingleDir( dir );  // Sólo borra el directorio si está vacío
                }
                return cont;
            }
        // pathABorrar es un directorio
        private int borraPathRec( TreePath pathABorrar ) {
            return borraPathRec( getFileNameFromPath(pathABorrar), getLastNodeOfPath(pathABorrar) );
        }

        private DefaultMutableTreeNode getLastNodeOfPath( TreePath tp ) {
            if (tp == null) return null;
            Object[] path = tp.getPath();
            return (DefaultMutableTreeNode) path[path.length-1];
        }

    static private Object[] opcionesBorrado = {"Sí", "No" };
    private Desktop desktop = Desktop.getDesktop();
    private TreePath justClickedPath = null;
    private long justClickedTime = 0;
    private TreePath actionPath = null;
    private Point lastClickedPathCoords = new Point( -10000, -10000 );  // Punto imposible (marca)
    private ActionListener myActionCommandListener = new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            // TODO: Real action
            if (actionPath!=null && e.getSource() instanceof JTree) {
                JTree actualTree = (JTree) e.getSource();
                int indexTree = getTreeIndex( actualTree );
                if (indexTree != -1) {
                    try {
                        boolean isFile = isLeaf( actionPath );
                        String fileName = getFileNameFromPath( actionPath );
                        if (e.getActionCommand().equals( "Ejecutar" )) {
                            desktop.open( new File( fileName ));
                        } else if (e.getActionCommand().equals( "Abrir" )) {
                            actualTree.expandPath( actionPath );
                        } else if (e.getActionCommand().equals( "Cerrar" )) {
                            actualTree.collapsePath( actionPath );
                        } else if (e.getActionCommand().equals( "Borrar" )) {
                            int respuesta = JOptionPane.showOptionDialog( null, "Borrar " + fileName + "\n¿Estás seguro?", "Confirmación de borrado",
                                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, opcionesBorrado, opcionesBorrado[1]);
                            if (respuesta == 0) {
                                int ficsBorrados = 0;
                                if (isFile) {
                                    File f = new File( fileName );
                                    if (DirProcess.deleteFile( f )) {
                                    	ficsBorrados++;
                                    }
                                } else {  // Es directorio
                                    respuesta = JOptionPane.showOptionDialog( null, fileName + " es un directorio. Se borrarán sólo los ficheros indicados en este árbol.\n¿Estás seguro de que quieres borrar su contenido?", "Confirmación de borrado de carpeta",
                                        JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE, null, opcionesBorrado, opcionesBorrado[1]);
                                    if (respuesta == 0) {
                                        ficsBorrados = borraPathRec( actionPath );
                                    }
                                }
                                if (respuesta==0) JOptionPane.showConfirmDialog( null, ficsBorrados + " fichero(s) borrado(s).", "Confirmación de borrado", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE );
                            }
                        } else if (e.getActionCommand().equals( "Reintentar" )) {
                            Object[] path = actionPath.getPath();
                            DefaultMutableTreeNode ultimoNodo = (DefaultMutableTreeNode) path[path.length-1];
                            if (ultimoNodo.isLeaf() && ultimoNodo instanceof DefaultMutableTreeNodeCopia) {  // Debería serlo
                            	String fileNameFuente = ((DefaultMutableTreeNodeCopia)ultimoNodo).ficheroFuente;
            					System.out.println( "Reintentando copia de fichero " + fileName + "..." );
                            	if (!fileNameFuente.equals(fileName)) {  // Si son iguales es que no cabe el reintento, si es viable entonces son diferentes
                            		Thread hiloReintento = new Thread() {
                            			public void run() {
                            				try {
												DirProcess.copyFile( new File(fileNameFuente), new File(fileName) );
                            					System.out.println( "Reintento de " + fileName + "satisfactorio." );
												JOptionPane.showMessageDialog( DirProcessFrame.this, "Reintento satisfactorio. Copia realizada\n" + fileNameFuente );
											} catch (IOException e) {
                            					System.out.println( "Reintento de " + fileName + "inválido." );
												JOptionPane.showMessageDialog( DirProcessFrame.this, "Reintento inválido. Copia no realizada.\n" + fileNameFuente + "\nComprueba si hay que desbloquear fichero origen o destino." );
											}
                            			}
                            		};
                            		hiloReintento.setDaemon( true );
                            		hiloReintento.start();
                            	} else {
                					System.out.println( "Reintento de " + fileName + "inválido. No se puede copiar de " + fileNameFuente );
                            	}
                            }
                        // TODO: Faltan el resto de opciones:
                            // Copiar - Hace copia de nuevo de origen a destino [en origen] o de destino a origen [en destino]
                            // Respaldar - Hace copia a respaldo en destino [sólo en destino]
                            // Restaurar - Hace copia de respaldo de borrado a destino [sólo en destino]
                        } else {
                            System.out.println( e.getActionCommand() + " -- " + actionPath + " --- " + e.getSource() );
                        }
                    } catch (IOException ex) {
                        JOptionPane.showConfirmDialog( null, "Error de E/S: " + ex.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE );
                    }
                }
            }
        }
    };
    private PopupMenu myTreePopupMenu = new PopupMenu(
            new String[] { "Abrir", "Cerrar", null, "Borrar", "Ejecutar", "Copiar", null, "Respaldar", "Restaurar", "Reintentar" },
                // Abrir - Abre la carpeta en el árbol
                // Cerrar - Cierra la carpeta en el árbol
                // Borrar - Borra el fichero/carpeta (pidiendo confirmación previa) [origen o destino]
                // Ejecutar - Lanza el fichero con la asociación por defecto que tenga el S.O. [origen o destino]
                // Copiar - Hace copia de nuevo de origen a destino [en origen] o de destino a origen [en destino]
                // Respaldar - Hace copia a respaldo en destino [sólo en destino]
                // Restaurar - Hace copia de respaldo de borrado a destino [sólo en destino]
                // Reintentar - Reintenta operación fallida si hay error [sólo en erróneos]
            new String[] { "Abrir", "Cerrar", null, "Borrar", "Ejecutar", "Copiar", null, "Respaldar", "Restaurar", "Reintentar" },
            myActionCommandListener );

    // Eventos para gestión de interacción con JTree
    public void valueChanged(TreeSelectionEvent e) {
        // System.out.println( e.getNewLeadSelectionPath() );
        // System.out.println( e.getPath() );
        // System.out.println( e.getPaths() );
        justClickedPath = e.getPath();
        justClickedTime = System.currentTimeMillis();
    }
    class PopupMenuThread extends Thread {
        @Override
        public void run() {
            try { Thread.sleep(300); } catch (InterruptedException ex) {}   // Retraso para permitir un doble click antes del popup
            SwingUtilities.invokeLater( new Runnable() {
                public void run() {
                    synchronized (lastClickedPathCoords) {
                        if (lastClickedPathCoords.getX() != -10000) {  // Si no ha habido doble click, sacar el popup
                            if (myMouseEvent.getSource() instanceof Component) {
                                DPFTipoArbol tipoA = getTipoArbol( (Component)myMouseEvent.getSource() );
                                if (tipoA != null) {
                                    // Sacar el menú contextual con las opciones activas que correspondan
                                    // Orden de flags:                                               Abrir,Cerrar,   SEP,Borrar,Ejecut,Copiar,   SEP,Respal,Restau,Reintn
                                    switch (tipoA) {
                                        case DPF_ERRORES_COPIA: {        myTreePopupMenu.setEnabled(  true,  true,  true, false,  true, false,  true, false, false, true  ); break; }
                                        case DPF_FICHEROS_GRANDES: {     myTreePopupMenu.setEnabled(  true,  true,  true,  true,  true, false,  true, false, false, false ); break; }
                                        case DPF_IGNORADOS_EN_DESTINO: { myTreePopupMenu.setEnabled(  true,  true,  true,  true,  true, false,  true, false, false, false ); break; }
                                        case DPF_IGNORADOS_EN_FUENTE: {  myTreePopupMenu.setEnabled(  true,  true,  true,  true,  true, false,  true, false, false, false ); break; }
                                        case DPF_IGUALES: {              myTreePopupMenu.setEnabled(  true,  true,  true, false,  true, false,  true, false, false, false ); break; }
                                        case DPF_MODERNO_EN_DESTINO: {   myTreePopupMenu.setEnabled(  true,  true,  true,  true,  true, false,  true, false, false, false ); break; }
                                        case DPF_MODERNO_EN_FUENTE: {    myTreePopupMenu.setEnabled(  true,  true,  true,  true,  true, false,  true, false, false, false ); break; }
                                        case DPF_SOLO_EN_DESTINO: {      myTreePopupMenu.setEnabled(  true,  true,  true,  true,  true, false,  true, false, false, false ); break; }
                                        case DPF_SOLO_EN_FUENTE: {       myTreePopupMenu.setEnabled(  true,  true,  true,  true,  true, false,  true, false, false, false ); break; }
                                    }
                                    myTreePopupMenu.manualShow( (Component)myMouseEvent.getSource(), myMouseEvent.getX(), myMouseEvent.getY() );
                                    lastClickedPathCoords.setLocation( -10000,-10000 );  // Marca de punto no encontrado
                                }
                            }
                        }
                    }
                }
            });
        }
    }
    private MouseEvent myMouseEvent;
    public void mouseClicked(MouseEvent e) {
        long timeGap =  System.currentTimeMillis() - justClickedTime;
        if (timeGap > 2000) {  // Más de dos segundos -> Mouse no tiene que ver con el valueChanged (que puede haber sido por teclado o algo)
            justClickedPath = null;
            return;
        }
        if (justClickedPath != null) {
            myMouseEvent = e;
            actionPath = justClickedPath;
            synchronized (lastClickedPathCoords) {
                lastClickedPathCoords.setLocation( e.getPoint() );
            }
            justClickedPath = null;
            (new PopupMenuThread()).start();
            // System.out.println( "PRIMER CLICK: " + actionPath +  " - " + e.getClickCount() + " - " + e.getX() + "," + e.getY() );
            // System.out.println( justClickedPath );
            // System.out.println( " - " + e.getClickCount() + " - " + e.getX() + "," + e.getY() );
        } else if (e.getPoint().equals(lastClickedPathCoords)) {
            // Segundo click en el mismo sitio de la selección (ignora el resto de eventos posteriores)
            synchronized (lastClickedPathCoords) {
                if (lastClickedPathCoords.getX() != -10000 && e.getClickCount()==2) {
                    // System.out.println( "DOBLE CLICK: " + actionPath +  " - " + e.getX() + "," + e.getY() + " --- " + e.getSource() );
                    String fileName = getFileNameFromPath( actionPath );
                    try {
                        desktop.open( new File( fileName ));
                    } catch (IOException ex) {
                        JOptionPane.showConfirmDialog( null, "Error de E/S: " + ex.getMessage(), "Error", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE );
                    }
                    lastClickedPathCoords.setLocation( -10000,-10000 );  // Marca de punto no encontrado
                }
            }
        }
    }
    public void mousePressed(MouseEvent e) {
    }
    public void mouseReleased(MouseEvent e) {
    }
    public void mouseEntered(MouseEvent e) {
    }
    public void mouseExited(MouseEvent e) {
    }


    // Atributos para la gestión de los árboles
    private int[] numNodos = new int[ DPFTipoArbol.values().length ];
    private long[] bytesTotales = new long[ DPFTipoArbol.values().length ];
    private boolean[] mostrarExpandido = { false, false, true, true, true, true, false, true, true };
    private JTree[] arbol = new JTree[ DPFTipoArbol.values().length ];
    private DefaultMutableTreeNode[] raiz = new DefaultMutableTreeNode[ DPFTipoArbol.values().length ];
    private DefaultMutableTreeNode[] ultimoDir = new DefaultMutableTreeNode[ DPFTipoArbol.values().length ];
    private TreePath[] ultimoDirPath = new TreePath[ DPFTipoArbol.values().length ];
    private String[] ultimoDirString = new String[ DPFTipoArbol.values().length ];


    // Método privado de crear los árboles
    private JTree crearArbol( String nomRaiz, DPFTipoArbol tipoArbol ) {
        numNodos[tipoArbol.ordinal()] = 0;
        if (!nomRaiz.endsWith("\\")) nomRaiz += "\\";
        raiz[tipoArbol.ordinal()] = new DefaultMutableTreeNode( nomRaiz.substring(0,nomRaiz.length()-1) );
        ultimoDir[tipoArbol.ordinal()] = raiz[tipoArbol.ordinal()];
        ultimoDirPath[tipoArbol.ordinal()] = new TreePath( ultimoDir[tipoArbol.ordinal()] );
        ultimoDirString[tipoArbol.ordinal()] = nomRaiz;
        arbol[tipoArbol.ordinal()] = new JTree( raiz[tipoArbol.ordinal()] );
        return arbol[tipoArbol.ordinal()];
    }

    // Método privado de volver al raíz
    private void irARaiz( DPFTipoArbol tipoArbol ) {
        ultimoDir[tipoArbol.ordinal()] = raiz[tipoArbol.ordinal()];
        ultimoDirPath[tipoArbol.ordinal()] = new TreePath( ultimoDir[tipoArbol.ordinal()] );
        ultimoDirString[tipoArbol.ordinal()] = raiz[tipoArbol.ordinal()] + "\\";
    }

    // Método privado de subir una carpeta
    private void subirUnNivelUltimoAccedido( DPFTipoArbol tipoArbol ) {
        if (ultimoDir[tipoArbol.ordinal()] != raiz[tipoArbol.ordinal()]) {
            ultimoDir[tipoArbol.ordinal()] = (DefaultMutableTreeNode) ultimoDir[tipoArbol.ordinal()].getParent();
            ultimoDirPath[tipoArbol.ordinal()] = ultimoDirPath[tipoArbol.ordinal()].getParentPath();
            String path = ultimoDirString[tipoArbol.ordinal()];
            path = path.substring( 0, path.length()-2 );
            int ultimaBarra = path.lastIndexOf("\\");
            ultimoDirString[tipoArbol.ordinal()] = path.substring( 0, ultimaBarra+1 );
        }
    }

    // Método privado de buscar un fichero en último directorio accedido
    private boolean existeFicheroEnUltimoAccedido( String fichero, DPFTipoArbol tipoArbol ) {
        DefaultMutableTreeNode busq = ultimoDir[tipoArbol.ordinal()];
        if (busq.isLeaf()) busq = null;
        else {
            busq = (DefaultMutableTreeNode) busq.getFirstChild();
            while(busq!=null && !busq.getUserObject().toString().equalsIgnoreCase(fichero)) busq = busq.getNextSibling();
        }
        return (busq != null);
    }

    // Método privado de crear los árboles
    // Crea o busca un directorio y lo deja en la variable de último Dir. Si ya existía, simplemente actualiza las variables
    // el path es relativo al último dir accedido
    // nuevoPathDirRelativo TIENE QUE acabar en \
    private void crearOBuscarDirEnUltimoAccedido( String nuevoPathDirRelativo, DPFTipoArbol tipoArbol ) {
        do {
            int primeraBarra = nuevoPathDirRelativo.indexOf("\\");
            String primerSubdir = nuevoPathDirRelativo.substring( 0, primeraBarra );
            nuevoPathDirRelativo = nuevoPathDirRelativo.substring( primeraBarra+1 );
            // Buscar si ya existe el nodo
            DefaultMutableTreeNode busq = ultimoDir[tipoArbol.ordinal()];
            if (busq.isLeaf()) busq = null;
            else {
                busq = (DefaultMutableTreeNode) busq.getFirstChild();
                while(busq!=null && !busq.getUserObject().toString().equalsIgnoreCase(primerSubdir)) busq = busq.getNextSibling();
            }
            if (busq == null) {  // No encontrado, hay que crearlo
                busq = new DefaultMutableTreeNode( primerSubdir );
                DefaultTreeModel tm = (DefaultTreeModel) arbol[tipoArbol.ordinal()].getModel();
                tm.insertNodeInto( busq, ultimoDir[tipoArbol.ordinal()], ultimoDir[tipoArbol.ordinal()].getChildCount() );
            } 
            ultimoDir[tipoArbol.ordinal()] = busq;
            ultimoDirPath[tipoArbol.ordinal()] = ultimoDirPath[tipoArbol.ordinal()].pathByAddingChild( busq );
            ultimoDirString[tipoArbol.ordinal()] = ultimoDirString[tipoArbol.ordinal()] + primerSubdir + "\\";
        } while (!nuevoPathDirRelativo.equals(""));
    }


    // Método privado de crear los árboles
    // Crea un directorio y lo deja en la variable de último Dir. Si ya existía, simplemente actualiza las variables
    private void crearOBuscarDir( String nuevoPathDir, DPFTipoArbol tipoArbol ) {
        if (nuevoPathDir.startsWith( ultimoDirString[tipoArbol.ordinal()])) {  // El nuevo fichero está más anidado que el último
            crearOBuscarDirEnUltimoAccedido( nuevoPathDir.substring( ultimoDirString[tipoArbol.ordinal()].length() ), tipoArbol );
        } else if (ultimoDirString[tipoArbol.ordinal()].startsWith(nuevoPathDir)) {   // El nuevo fichero está menos anidado que el último
            // refrescaArbol( arbol[tipoArbol.ordinal()], raiz[tipoArbol.ordinal()], ultimoDirPath[tipoArbol.ordinal()] );  // Expandir visualmente
            do {
                subirUnNivelUltimoAccedido(tipoArbol);
            } while (!nuevoPathDir.equalsIgnoreCase( ultimoDirString[tipoArbol.ordinal()] ));
        } else {  // Son directorios independientes
            irARaiz( tipoArbol );
            crearOBuscarDirEnUltimoAccedido( nuevoPathDir.substring( ultimoDirString[tipoArbol.ordinal()].length() ), tipoArbol );
        }
    }


    /** Añade nuevo fichero en el árbol
     * @param nomFic    Nombre completo del fichero que se añade
     * @param tipoArbol Árbol en el que se añade
     * @param bytesNuevoFic	Bytes del fichero que se añade (para estadística)
     */
    // private int contFicheros = 0;
    public void nuevoFichero( String nomFic, DPFTipoArbol tipoArbol, long bytesNuevoFic, String... ficheroFuenteOpcional ) {
        // getAccess( "nuevoFic " + tipoArbol.toString() );
        numNodos[tipoArbol.ordinal()]++;
        bytesTotales[tipoArbol.ordinal()] += bytesNuevoFic;
        if (soloConteoIgnorados && (tipoArbol==DPFTipoArbol.DPF_IGNORADOS_EN_DESTINO || tipoArbol==DPFTipoArbol.DPF_IGNORADOS_EN_FUENTE)) {
            if (tipoArbol==DPFTipoArbol.DPF_IGNORADOS_EN_FUENTE)
                tfIgnoradosOrigen.setText( ""+numNodos[tipoArbol.ordinal()] );
            else
                tfIgnoradosDestino.setText( ""+numNodos[tipoArbol.ordinal()] );
        } else {
            muestraNumeroFicherosEnPanel( tipoArbol );
            nomFic = nomFic.replaceAll( "/", "\\\\" );
            int ultimaBarra = nomFic.lastIndexOf( "\\" );
            String path = nomFic.substring(0,ultimaBarra+1);  // Incluye \ al final
            String nombre = nomFic.substring(ultimaBarra+1);  // Solo el nombre
            if (!path.equalsIgnoreCase(ultimoDirString[tipoArbol.ordinal()])) {  // Distinto directorio que la última vez
                crearOBuscarDir( path, tipoArbol );
            }
            if (!existeFicheroEnUltimoAccedido( nombre, tipoArbol )) {
            	if (ficheroFuenteOpcional.length>0) {  // Caso especial : en árbol de copia se guarda el fichero fuente (para poder reintentar)
	                DefaultMutableTreeNode nuevoFic = new DefaultMutableTreeNodeCopia( nombre, ficheroFuenteOpcional[0] );
	                DefaultTreeModel tm = (DefaultTreeModel) arbol[tipoArbol.ordinal()].getModel();
	                tm.insertNodeInto( nuevoFic, ultimoDir[tipoArbol.ordinal()], ultimoDir[tipoArbol.ordinal()].getChildCount() );
	                if (mostrarExpandido[tipoArbol.ordinal()]) refrescaArbol( arbol[tipoArbol.ordinal()], raiz[tipoArbol.ordinal()], new TreePath(nuevoFic.getPath()) /* ultimoDirPath[tipoArbol.ordinal()] */ );  // Expandir visualmente
	                // ultimoDir[tipoArbol.ordinal()].add( nuevoFic ); -- Modo incorrecto de insertar (hay que hacerlo en el modelo)
            	} else {
	                DefaultMutableTreeNode nuevoFic = new DefaultMutableTreeNode( nombre );
	                DefaultTreeModel tm = (DefaultTreeModel) arbol[tipoArbol.ordinal()].getModel();
	                tm.insertNodeInto( nuevoFic, ultimoDir[tipoArbol.ordinal()], ultimoDir[tipoArbol.ordinal()].getChildCount() );
	                if (mostrarExpandido[tipoArbol.ordinal()]) refrescaArbol( arbol[tipoArbol.ordinal()], raiz[tipoArbol.ordinal()], new TreePath(nuevoFic.getPath()) /* ultimoDirPath[tipoArbol.ordinal()] */ );  // Expandir visualmente
	                // ultimoDir[tipoArbol.ordinal()].add( nuevoFic ); -- Modo incorrecto de insertar (hay que hacerlo en el modelo)
            	}
            }
        }
    }    
    
	    @SuppressWarnings("serial")
		private static class DefaultMutableTreeNodeCopia extends DefaultMutableTreeNode {
	    	public String ficheroFuente;
	    	public DefaultMutableTreeNodeCopia( Object userNode, String ficheroFuente ) {
	    		super( userNode );
	    		this.ficheroFuente = ficheroFuente;
	    	}
	    }
	
        // If expand is true, expands all nodes in the tree.
        // Otherwise, collapses all nodes in the tree.
        // * ATENCION!  Después de expandir, hay que recalcular los datos porque no se actualiza ningún cambio
        public void expandAll(JTree tree, boolean expand) {
            TreeNode root = (TreeNode)tree.getModel().getRoot();
            // Traverse tree from root
            expandAll(tree, new TreePath(root), expand);
        }
        private void expandAll(JTree tree, TreePath parent, boolean expand) {
            // Traverse children
            TreeNode node = (TreeNode)parent.getLastPathComponent();
            if (node.getChildCount() >= 0) {
                for (Enumeration<?> e=node.children(); e.hasMoreElements(); ) {
                    TreeNode n = (TreeNode)e.nextElement();
                    TreePath path = parent.pathByAddingChild(n);
                    expandAll(tree, path, expand);
                }
            }
            // Expansion or collapse must be done bottom-up
            if (expand) {
                tree.expandPath(parent);
                // if (!tree.isExpanded(parent)) System.out.println( "-> NO EXPANDIDO!! " + parent );
            } else {
                tree.collapsePath(parent);
            }
        }

        private void refrescaArbol( JTree t, DefaultMutableTreeNode n, TreePath tp ) {
            if (!n.isLeaf()) {
                // System.out.println( "Intentando expandir... " + tp.toString() );
                t.expandPath( tp );
                if (tp.getPathCount() > 1) refrescaArbolRec( t, tp.getParentPath(), 2 );
                // if (!t.isExpanded(tp)) System.out.println( "-> NO EXPANDIDO!!: " + tp.toString()); else System.out.println();
            }
        }
            private void refrescaArbolRec( JTree t, TreePath tp, int profundidad ) {
                // System.out.println( String.format( "%1$" + profundidad + "c", ' ' ) + "Intentando expandir... " + tp.toString() );
                t.expandPath( tp );
                if (tp.getPathCount() > 1) refrescaArbolRec( t, tp.getParentPath(), profundidad + 2 );
                // if (!t.isExpanded(tp)) System.out.println( String.format( "%1$" + profundidad + "c", ' ' ) + "-> NO EXPANDIDO!!: " + tp.toString()); else System.out.println();
            }


/*   Un montón de código inútil para refrescar los árboles. No ha habido manera, hay que hacerlo solo al final, si no no funciona


    private boolean hayAlguien = false;
    private synchronized void getAccess( String mens ) {
        System.out.println( "ACCESS -> " + mens );
//        while (hayAlguien) {
//            try {Thread.sleep(50);} catch (Exception e) {}
//        }
        System.out.println( "       -> GRANTED" );
        hayAlguien = true;
    }
    private void releaseAccess() {
        hayAlguien = false;
        System.out.println( "       -> RELEASED" );
    }


        private void refresco2( JTree t, DefaultMutableTreeNode n ) {
            if (!n.isLeaf()) {
                for (int i = 0; i < n.getChildCount(); i++) {
                    refresco2( t, (DefaultMutableTreeNode) n.getChildAt(i) );
                }
                t.expandPath( new TreePath( n.getPath() ) );
            }
        }

        public void expandToLast(JTree tree) {
            TreeModel data = tree.getModel();
            Object node = data.getRoot();
            if (node == null) return;
            TreePath p = new TreePath(node);
            while (true) {
                int count = data.getChildCount(node);
                if (count == 0) break;
                node = data.getChild(node, count - 1);
                p = p.pathByAddingChild(node);
            }
            tree.scrollPathToVisible(p);
        }


    public void expandAll2(JTree tree) {
    int row = 0;
    while (row < tree.getRowCount()) {
      tree.expandRow(row);
      row++;
      }
    }


        private void refrescaArbol( JTree t, DefaultMutableTreeNode raiz, TreePath tp ) {
            // refresco2( t, raiz );
            // SwingUtilities.invokeLater( new Refresco( t, tp ) );
        }

        private void refrescaArbol( DPFTipoArbol tipoArbol ) {
            refresco2( arbol[tipoArbol.ordinal()], raiz[tipoArbol.ordinal()] );
            SwingUtilities.invokeLater( new Refresco( t, tp ) );
        }

        private class Refresco extends Thread {
            JTree t; TreePath tp;
            public Refresco( JTree t, TreePath tp ) {
                this.t = t; this.tp = tp;
            }
            @Override
            public void run() {
                getAccess( "run " + tp.toString() );
                // if (Math.random() < 0.3) t.expandPath(tp);
                // t.scrollPathToVisible( tp );
System.out.println( "1");
//                if (!t.isExpanded( tp )) {
//                    t.collapsePath( tp );
//                    t.expandPath( tp );  // Expandir visualmente
//                }
System.out.println( "2");
                releaseAccess();
            }
        }




*/

    /** Método que expande los árboles que haya ahora en la ventana
     */
    public void expandeArboles( ) {
        for (DPFTipoArbol ta : DPFTipoArbol.values()) {
            // expandAll2( arbol[ta.ordinal()] );
            // expandToLast( arbol[ta.ordinal()] );
            // expandAll( arbol[ta.ordinal()], false );
            expandAll( arbol[ta.ordinal()], true );
            //refresco2( arbol[ta.ordinal()], raiz[ta.ordinal()]);
        }
    }

    /** Método que informa a la ventana de que ha acabado la sincronización
     *  Saca el mensaje mensFinSincro y deja la ventana activa lista para cerrar (botón de cierre o icono de cierre)
     * @param mensFinSincro
     */
    public void finSincronizacion( String mensFinSincro ) {
        // expandeArboles(); - no es necesario, se va haciendo según se insertan nodos
        setVisible( true );
        setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );  // Ya se puede cerrar la ventana
        bCerrarCancelar.setText( "Cerrar" );
        lProgreso.setVisible( false );
        setProgreso( 10000 );
        pbProgreso.setVisible( false );
        sacaMensaje( mensFinSincro );
    }

    /** Saca el mensaje en la líena de mensajes
     * @param mens
     */
    public void sacaMensaje( String mens ) {
        if (mens == null || mens.equals("")) mens = " ";
        lMensaje.setText( mens );
    }

    /** Suma bytes copiados y los muestra
     * @param numBytes
     */
    public void sumaBytesCopiados( long numBytes ) {
        numBytesCopiados += numBytes;
        long kBytes = numBytesCopiados / 1024;
        lTamanyo.setText( String.format( "%1$,d", kBytes ) + " Kb" );
    }

    private static int MAXLINES = 1000;  // Líneas por encima de las que se trunca la textarea
    private static int RESUMELINES = 600;  // Líneas que se dejan al truncar (siempre las últimas)
    /** Añade el mensaje a la zona de mensajes
     * @param mens  Mensaje a visualizar
     * @param saltoLinea    Si true, salta a la siguiente línea de la zona de mensajes tras este mensaje
     */
    public void anyadeZonaMensajes( String mens, boolean saltoLinea ) {
        taMensajes.append( mens + (saltoLinea ? "\n" : "") );
        try {
            if (taMensajes.getLineCount() > MAXLINES) {
                int offsetToCut = taMensajes.getLineEndOffset( RESUMELINES );
                taMensajes.select(0, offsetToCut);
                taMensajes.replaceSelection("");
                taMensajes.insert( "Visualización truncada por ser excesivamente larga. Consulte el proceso completo en el fichero de registro (.log)." + "\n", 0 );
            }
            int offsetToPos = taMensajes.getLineStartOffset( taMensajes.getLineCount()-1 );
            taMensajes.select(offsetToPos, offsetToPos); // Hace que se redibuje la textarea para que se vea el último mensaje
        } catch (BadLocationException e) {
        }
    }

    /** Pone el indicador de progreso en la ventana
     * @param deCeroAMil    valor de 0 (0%) a 10000 (100%)
     */
    public void setProgreso( int deCeroADiezMil ) {
        pbProgreso.setValue( deCeroADiezMil );
        // Cálculo tiempo
        long tiempo = System.currentTimeMillis() - horaInicio;
        if (tiempo > 3000 && deCeroADiezMil > 2) {
            long faltan = Math.round( tiempo / deCeroADiezMil * (10000-deCeroADiezMil) );
            if (deCeroADiezMil == 10000)
                lTiempo.setText( formatoHHMMSS(tiempo) );
            else
                lTiempo.setText( formatoHHMMSS(tiempo) + "/" + formatoHHMMSS(faltan) );
        }
    }

        private String formatoHHMMSS( long tiempo ) {
            String ret = "";
            tiempo = tiempo/1000;
            if (tiempo > 3600) {
                ret = String.format( "%1$02d", tiempo/3600 ) + ":";
                tiempo = tiempo % 3600;
            }
            ret = ret + String.format( "%1$02d", tiempo/60 ) + ":";
            ret = ret + String.format( "%1$02d", tiempo%60 );
            return ret;
        }

        // Método privado para borrar una carpeta completa. CUIDADO CON ESTA OPCION!!!!  Usarla con extremo cuidado
        private static void borraDirPrueba( String dirABorrar ) {
            Object[] opciones = {"Sí", "No borrar", "Cancelar el programa" };
        	int respuesta = JOptionPane.showOptionDialog( null, "Se va a borrar TODO EL DIRECTORIO " + dirABorrar + "\n¿Estás seguro?", "Confirmación de borrado de prueba",
        		JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, opciones, opciones[0]);
            if (respuesta == 1) return;
            if (respuesta == 2) System.exit(-1);
            File fuente = new File( dirABorrar );
            if (fuente.isDirectory()) {
                String[] children = fuente.list();
                if (children == null) children = new String[0];
                for( String child : children ) {
                    File f = new File( fuente, child );
                    borraDirRec( f );
                }
            }
        }
        private static void borraDirRec( File f ) {
            if (f.isFile()) {
                // System.out.println( "Borrando... " + f.getAbsolutePath());
                f.delete();
            } else {
                String[] children = f.list();
                if (children == null) children = new String[0];
                for( String child : children ) {
                    File f2 = new File( f, child );
                    borraDirRec( f2 );
                }
                f.delete();  // Después de borrados los ficheros, borra el directorio
            }
        }

    public static void main( String[] s ) {
        System.out.println( System.getProperty("java.version"));
        System.out.println( "Borrando directorio de prueba...");
        borraDirPrueba( "d:/t/aborrarcarpeta" );  // CUIDADO CON ESTA FUNCION!!
        File ff1a = new File("d:/t");
        File ff2a = new File("d:/t/aborrarcarpeta");
        String[] fdirs1a = new String[] { "a", "i" };
        String[] fdirs2a = new String[] { "a", "i" };
        try {
            DirProcess.syncAllDirsAndFiles( 10000, ff1a, ff2a, fdirs1a, fdirs2a, null, null, null, TipoCopia.TIPO_COPIA_BACKUP, "BACKUP", false, true, 30 );
        } catch (IOException ex) {
            System.out.println( ex.getMessage() );
            ex.printStackTrace( System.out );
        }
        System.out.println( "Proceso finalizado." );
/*
        DirProcessFrame v = new DirProcessFrame( new File("D:/"), new File("F:/") );
        v.sacaMensaje( "Prueba");
        v.anyadeZonaMensajes( "Mensaje 1", true );
        v.anyadeZonaMensajes( "Mensaje 2", true );
        v.anyadeZonaMensajes( "Mensaje 3", true );
        v.anyadeZonaMensajes( "Mensaje 4", true );
        v.anyadeZonaMensajes( "Mensaje 5", true );
        v.anyadeZonaMensajes( "Mensaje 6", true );
        v.nuevoFichero( (new File("d:/t/A/tempo.bat")).getAbsolutePath(), DPFTipoArbol.DPF_IGNORADOS_EN_DESTINO);
        v.nuevoFichero( (new File("d:/t/A/tempo2.bat")).getAbsolutePath(), DPFTipoArbol.DPF_IGNORADOS_EN_DESTINO);
        v.nuevoFichero( (new File("d:/t/A/B/C/anidado.dat")).getAbsolutePath(), DPFTipoArbol.DPF_IGNORADOS_EN_DESTINO);
        v.nuevoFichero( (new File("d:/t/pronto.bat")).getAbsolutePath(), DPFTipoArbol.DPF_IGNORADOS_EN_DESTINO);
        v.nuevoFichero( (new File("d:/t/A/B/C/anidado.dat")).getAbsolutePath(), DPFTipoArbol.DPF_IGNORADOS_EN_DESTINO);
        v.nuevoFichero( (new File("d:/t/A/B/C/anidado2.dat")).getAbsolutePath(), DPFTipoArbol.DPF_IGNORADOS_EN_DESTINO);
        v.finSincronizacion( "Sincronización finalizada." );
 */
    }

    static InitUINimbus initLookAndFeel = new InitUINimbus();
    static class InitUINimbus {
        InitUINimbus() {
            try {
                UIManager.setLookAndFeel( "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel" );
            } catch (Exception ex) { 
                try {
                    UIManager.setLookAndFeel( "javax.swing.plaf.nimbus.NimbusLookAndFeel" );
                } catch (Exception ex2) { ex2.printStackTrace(); }
            }
        }
    }

}
