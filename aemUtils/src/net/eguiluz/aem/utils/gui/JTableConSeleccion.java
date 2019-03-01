package net.eguiluz.aem.utils.gui;

import java.awt.BorderLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/** JTable que añade una columna a la izquierda de checkboxes que permite al usuario
 * seleccionar las filas que desee.
 * Pulsando la cabecera de esa columna se marcan o desmarcan todas.
 * @author andoni
 */
public class JTableConSeleccion extends JTable {
	private static final long serialVersionUID = -1801645679604901743L;
	private static int NUM_COLUMNA_MARCA = 0;  // DEBE SER CERO, si no habría que cambiar trozos de código
	private static int ANCH_MIN_CHECK = 25;
	protected BooleanTableModel miTableModel;
	public JTableConSeleccion() {
		// Eventos
		getTableHeader().addMouseListener( new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent mEv) {
				if (columnAtPoint(mEv.getPoint())==NUM_COLUMNA_MARCA) {  // Columna marca
					if (miTableModel.getRowCount() > 0) {
						Object o = (miTableModel.getValueAt(0, NUM_COLUMNA_MARCA));
						if (o instanceof Boolean) {
							Boolean valor = (Boolean) o;
							valor = !valor;
							for( int fila = 0; fila < miTableModel.getRowCount(); fila++) {
								miTableModel.setValueAt( valor, fila, NUM_COLUMNA_MARCA );
							}
						}
					}
				}
			}
		});
	}

    /** Clase interna para poner checkbox. JTable renderiza un checkbox cuando el método
     * getColumnClass devuelve la clase Boolean. Sólo con meter Boolean en las filas no ocurre,
     * porque el DefaultTableModel no sabe qué hay en cada columna y por defecto devuelve Object
     * para todos los casos. En esta clase se fuerza a que el objeto de la columna 1 sea Boolean.
     * También hace que sólo los booleans sean editables
     */
    private class BooleanTableModel extends DefaultTableModel {
		private static final long serialVersionUID = 182922056270679486L;
		BooleanTableModel( int r, int c ) { 
    		super( r, c );
    	}
        @Override
        public Class<?> getColumnClass(int columnIndex) {
        	if (columnIndex == NUM_COLUMNA_MARCA)
        		return Boolean.class;
        	else	
        		return super.getColumnClass(columnIndex);
        }
		@Override
		public boolean isCellEditable(int row, int col) {
			return (col==NUM_COLUMNA_MARCA);
		}
    }

		private String[] misCabeceras = null;
		private Integer[] misAnchos = null;
	/** Vacía la tabla
	 * @param cabeceras	Array de textos de cabeceras (excluyendo la de selección). 
	 * 			Si es null, elimina las cabeceras
	 * @param anchos	Array de anchos de pixels de las columnas. Si es negativo se interpreta
	 * 			como ancho mínimo, y si es positivo como ancho preferido
	 */
	public void vacia( String[] cabeceras, Integer[] anchos ) {
		if (cabeceras==null)  {
			setModel( new BooleanTableModel( 0, 0 ) );
			misCabeceras = null;
			misAnchos = null;
			return;
		}
		if (misCabeceras!=null && misAnchos!=null && Arrays.equals(misCabeceras,cabeceras) && Arrays.equals(misAnchos,anchos)) {
			// El modelo sigue siendo el mismo. Simplemente quitar las filas de valores
			while (miTableModel.getRowCount()>0) miTableModel.removeRow(0);
		} else {
			misCabeceras = cabeceras;
			misAnchos = anchos;
			String[] misCabeceras = new String[cabeceras.length+1];
			for (int i=0; i<cabeceras.length; i++) misCabeceras[i+1] = cabeceras[i];
			misCabeceras[0] = "Sel";  // Añadimos la cabecera de selección
			miTableModel = new BooleanTableModel( 0, misCabeceras.length );
			miTableModel.setColumnIdentifiers( misCabeceras );
			setModel( miTableModel );
			for (int i=0; i<cabeceras.length; i++) {
				if (i < anchos.length) {
					if (anchos[i] < 0)
						getColumn( cabeceras[i] ).setMinWidth( -anchos[i] );
					else
						getColumn( cabeceras[i] ).setPreferredWidth( anchos[i] );
				}
			}
			getColumn( "Sel" ).setMinWidth( ANCH_MIN_CHECK );
		}
	}
	
	/** Devuelve uno de los valores contenidos en la tabla 
	 * @param fila	Fila cuyo valor se quiere
	 * @param columna	Columna cuyo valor se quiere
	 * @throws IndexOutOfBoundsException	Error lanzado si el número de fila o columna es incorrecto
	 */
	public Object getValor( int fila, int columna ) throws IndexOutOfBoundsException {
		if (fila > miTableModel.getRowCount()-1 || fila < 0)
			throw new IndexOutOfBoundsException( "Fila incorrecta: " + fila );
		if (columna > miTableModel.getColumnCount()-1 || columna < 1)
			throw new IndexOutOfBoundsException( "Columna incorrecta: " + columna );
		return miTableModel.getValueAt( fila, columna );
	}
	
	/**
	 * @param fila	Fila cuyo valor se quiere modificar
	 * @param columna	Columna cuyo valor se quiere modificar (0 es el checkbox, no válido)
	 * @param nuevoValor	Valor que se quiere modificar en la tabla
	 * @throws IndexOutOfBoundsException	Error lanzado si el número de fila o columna es incorrecto
	 * @throws ClassCastException	Error lanzado si el tipo de dato que se quiere introducir no es compatible con el antiguo existente
	 */
	public void cambiaValor( int fila, int columna, Object nuevoValor ) throws IndexOutOfBoundsException, ClassCastException {
		if (fila > miTableModel.getRowCount()-1 || fila < 0)
			throw new IndexOutOfBoundsException( "Fila incorrecta: " + fila );
		if (columna > miTableModel.getColumnCount()-1 || columna < 1)
			throw new IndexOutOfBoundsException( "Columna incorrecta: " + columna );
		if (nuevoValor.getClass().isAssignableFrom( nuevoValor.getClass() )) {
			miTableModel.setValueAt( nuevoValor, fila, columna );
		} else {
			throw new ClassCastException( "Valor de tipo incorrecto: " + nuevoValor + "(" + nuevoValor.getClass().getName() + ")" );
		}
	}
	
	/** carga la JTable de etiquetas
	 * @param cabeceras	Array de textos de cabeceras (excluyendo la de selección). 
	 * 			Determina el número de columnas
	 * @param anchos	Array de anchos de pixels de las columnas. Si es negativo se interpreta
	 * 			como ancho mínimo, y si es positivo como ancho preferido
	 * @param seleccs	Array de booleanos de selección de cada fila
	 * @param arraysValores	Arrays de objetos, cada uno define una columna de la tabla
	 * 			Todos deben tener la misma longitud, y la misma longitud que seleccs
	 */
	public void cargaValores( String[] cabeceras, Integer[] anchos, boolean[] seleccs, Object[]... arraysValores ) {
		vacia( cabeceras, anchos );  // Inicializa la tabla con modelo vacío
		if (arraysValores!=null && arraysValores.length>0) {
			// Cargar la tabla con los datos de arraysValores
			for (int i = 0; i < arraysValores[0].length; i++) {
				Object[] fila = new Object[arraysValores.length + 1];
				if (seleccs == null || i >= seleccs.length)
					fila[0] = Boolean.FALSE;
				else
					fila[0] = new Boolean( seleccs[i] );
				for (int j = 0; j < arraysValores.length; j++) {
					Object posiblementeNulo = arraysValores[j][i];
					if (arraysValores[j][i] instanceof String && arraysValores[j][i]==null)
						posiblementeNulo = "";
					fila[j+1] = posiblementeNulo;
				}
				miTableModel.addRow( fila );
			}
		}
	}
	
	/** carga la JTable de etiquetas
	 * @param cabeceras	Array de textos de cabeceras (excluyendo la de selección). 
	 * 			Determina el número de columnas
	 * @param anchos	Array de anchos de pixels de las columnas. Si es negativo se interpreta
	 * 			como ancho mínimo, y si es positivo como ancho preferido
	 * @param arraysValores	Arrays de objetos, cada uno define una columna de la tabla
	 * 			Todos deben tener la misma longitud
	 */
	public void cargaValores( String[] cabeceras, Integer[] anchos, Object[]... arraysValores ) {
		cargaValores( cabeceras, anchos, null, arraysValores );
	}
	
	
	/** carga la JTable de etiquetas
	 * @param cabeceras	Lista de textos de cabeceras (excluyendo la de selección). 
	 * 			Determina el número de columnas
	 * @param anchos	Lista de anchos de pixels de las columnas. Si es negativo se interpreta
	 * 			como ancho mínimo, y si es positivo como ancho preferido
	 * @param listasDeListasDeValores	Lista de listas de objetos, cada sublista define una columna de la tabla
	 * 			Todos deben tener la misma longitud
	 */
	public void cargaValores( Collection<String> cabeceras, Collection<Integer> anchos, 
			Collection<Collection<Object>> listasDeListasDeValores ) {
		cargaValores( cabeceras.toArray(new String[0]), anchos.toArray(new Integer[0]),
				listasDeListasDeValores.toArray( new Collection[0]) );
	}

	
	/**	Consulta si la fila indicada está seleccionada
	 * @return	true si la fila está seleccionada con el checkbox, false en caso contrario
	 */
	public boolean isFilaSeleccionada( int numFila ) {
		if (numFila<0 || numFila>=miTableModel.getRowCount()) return false;
		return ((Boolean)miTableModel.getValueAt( numFila, NUM_COLUMNA_MARCA ));
	}
	
	/**	Devuelve la lista de todos los valores de las filas seleccionadas
	 * @return	Lista de arraylists de todos los valores
	 */
	public ArrayList<ArrayList<Object>> getFilasSeleccionadas() {
		ArrayList<ArrayList<Object>> result = new ArrayList<ArrayList<Object>>();
		for (int i=0; i<miTableModel.getRowCount(); i++) {
			if ((Boolean)miTableModel.getValueAt( i, NUM_COLUMNA_MARCA )) {  // Si está seleccionada
				ArrayList<Object> fila = new ArrayList<Object>();
				for (int j = 1; j < miTableModel.getColumnCount(); j++) {
					fila.add( miTableModel.getValueAt( i, j ) );
				}
				result.add( fila );
			}
		}
		return result;
	}
	
		private static DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
		private static DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
		static {
			rightRenderer.setHorizontalAlignment( JLabel.RIGHT );
			centerRenderer.setHorizontalAlignment( JLabel.CENTER );
		}
		
	/** Pone con alineación centrada la columna indicada (sin considerar los campos de selección
	 * @param numCol
	 */
	public void setColumnaCentrada( int numCol ) {
		if (numCol<getColumnCount()-1)
			getColumn( numCol+1 ).setCellRenderer( centerRenderer );
	}

	/** Pone con alineación derecha la columna indicada (sin considerar los campos de selección
	 * @param numCol
	 */
	public void setColumnaADerecha( int numCol ) {
		if (numCol<getColumnCount()-1)
			getColumn( numCol+1 ).setCellRenderer( rightRenderer );
	}
	
	
	/** Método main de prueba
	 * @param s	No utilizado
	 */
	public static void main( String[] s ) {
        try {
			UIManager.setLookAndFeel( "com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel" );
		} catch (Exception e) {
		}
		JFrame v = new JFrame();
		v.setDefaultCloseOperation( JFrame.DISPOSE_ON_CLOSE );
		v.setSize( 400, 300 );
		JTableConSeleccion miTabla = new JTableConSeleccion();
			String[] cabs = { "Nombres", "Teléfonos" };
			Integer[] anchos = { 1500, -90 };
			String[] nombres = { "Pepe", "Juan", "Marcos Antonio Luis Fernández Gutiérrez", "María" };
			Integer[] edad = { 30, 35, 28, 41 };
		miTabla.cargaValores(cabs, anchos, nombres, edad );
		v.getContentPane().add( new JScrollPane(miTabla), BorderLayout.CENTER );
		v.setVisible( true );
		try {Thread.sleep(10000); } catch (Exception e) {}
		System.out.println( miTabla.getFilasSeleccionadas() );
	}
	
}
