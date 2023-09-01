package net.eguiluz.aem.utils;

import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Properties;
import javax.swing.*;
import javax.swing.text.JTextComponent;

/** Ventana de edición genérica de propiedades de configuración.
 * Permite crear una ventana de diálogo que gestiona valores de configuración
 * Se utilizan properties para guardar estos valores en fichero de propiedades
 * @author Andoni Eguíluz Morán
 * Facultad de Ingeniería - Universidad de Deusto
 */
@SuppressWarnings("serial")
public class VentanaEdicionGenericaConfig  extends JDialog {

	private ArrayList<JTextComponent> listaTfPropiedad = new ArrayList<JTextComponent>();
	private ArrayList<String> propiedad = new ArrayList<String>();
	private ArrayList<String> valorDefecto = new ArrayList<String>();
	private String nomFic;
	private Properties misProps = null;
	private boolean hayCambios = false;
	private ArrayList<EventoCambio> listaEventos = new ArrayList<>();
	
	/** Construye un diálogo con definición particular de configuración de propiedades. 
	 * Además se pueden añadir otras propiedades, que no serán editadas de forma interactiva en la ventana, pero que sí se pueden cambiar, guardar y consultar
	 * @param nomFic	Nombre del fichero en el que se guardarán/cargarán las propiedades
	 * 		Cada vez que se abra el diálogo interactivo, se guardarán el fichero si hay cualquier cambio
	 * @param props	Array de strings de nombres de propiedades
	 * @param mensajes	Array de mensajes a mostrar al usuario con cada propiedad
	 * @param defecto	Array de strings de valores por defecto de las propiedades
	 * @param tipos	Array de Strings con los siguientes valores: null o "" si es un texto normal
	 *      "FIC" si corresponde a un path de fichero, "DIR" si corresponde a un path de carpeta
	 *      "Lnnn" si se quiere un texto con un número de columnas de textfield determinado (nn)
	 *      "INT" si se quiere limitar a un entero (no deja meter otra cosa que no lo sea)
	 * @throws IndexOutOfBoundsException	Los cuatro arrays props, mensajes, defecto, carpetas deben tener la misma longitud, con
	 * 		correspondencia uno a uno. Si alguno tiene longitud diferente no se crea el objeto y se lanza esta excepción
	 */
	public VentanaEdicionGenericaConfig ( String nomFic, String[] props, String[] mensajes, String[] defecto, String[] tipos ) throws IndexOutOfBoundsException {
		if (props.length != defecto.length || defecto.length != tipos.length || tipos.length != mensajes.length) {
			System.err.println( "Props " + props.length + " - Defecto " + defecto.length + " - Tipos " + tipos.length + " - Mensajes " + mensajes.length );
			throw new IndexOutOfBoundsException( "AEFicConfiguracion: Inicialización incorrecta");
		}
		this.nomFic = nomFic;
		setModal(true);
		setTitle( "Valores de configuración" );
		setDefaultCloseOperation(JDialog.HIDE_ON_CLOSE);
		getContentPane().setLayout(new BorderLayout());
		JPanel contentPanel = new JPanel();
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
		for (int i = 0; i < props.length; i++ ) {
			propiedad.add( props[i] );
			valorDefecto.add( (defecto[i]==null)?"":defecto[i] );
			JPanel panel1 = new JPanel();
			((FlowLayout) panel1.getLayout()).setAlignment(FlowLayout.LEFT);  // El Layout de JPanel ya es Flow por defecto
			contentPanel.add(panel1);
			JLabel lbl = new JLabel( mensajes[i] );
				panel1.add(lbl);
			boolean hayMasTF = false;
			do {
				JTextComponent tf = new JTextField();
					panel1.add( tf );
					if (tipos[i]!=null && tipos[i].equals("INT")) {  // TextField que obliga a entero
						((JTextField)tf).setColumns( 5 );
						// tf.setEditable( true );
						tf.addKeyListener(new KeyAdapter() {
							@Override
							public void keyTyped(KeyEvent e) {
								if (e.getKeyChar()<'0' || e.getKeyChar()>'9') e.consume(); 
								else hayCambios = true;
							}
						});
						tf.addFocusListener( new FocusAdapter() {
							@Override
							public void focusLost(FocusEvent e) {
								if (hayCambios && !tf.getText().equals( misProps.getProperty( propiedad.get( listaTfPropiedad.indexOf( tf ) ) ) ) ) {
									for (EventoCambio ec : listaEventos) {
										ec.hayCambio( propiedad.get( listaTfPropiedad.indexOf( tf ) ), tf.getText() );
									}
								}
							}
						});
					} else if (tipos[i]==null || tipos[i].equals("") || tipos[i].startsWith("L")) {  // TextField normal
						int cols = 20;
						if (tipos[i]!=null && tipos[i].startsWith("L"))
							try {
								cols = Integer.parseInt( tipos[i].substring(1) );
							} catch (Exception e) {}
						((JTextField)tf).setColumns( cols );
						// tf.setEditable( true );
						tf.addKeyListener(new KeyAdapter() {
							@Override
							public void keyTyped(KeyEvent e) {
								hayCambios = true;
							}
						});
						tf.addFocusListener( new FocusAdapter() {
							@Override
							public void focusLost(FocusEvent e) {
								if (hayCambios && !tf.getText().equals( misProps.getProperty( propiedad.get( listaTfPropiedad.indexOf( tf ) ) ) ) ) {
									for (EventoCambio ec : listaEventos) {
										ec.hayCambio( propiedad.get( listaTfPropiedad.indexOf( tf ) ), tf.getText() );
									}
								}
							}
						});
					} else {
						((JTextField)tf).setColumns(30);
						tf.setEditable( false );
						JButton btn = new JButton("Cambiar");
						if (tipos[i].equals("DIR")) {
							btn.addActionListener( new BotonCambiarPath( ((JTextField)tf), true ) );
						} else if (tipos[i].equals("FIC")) {
							btn.addActionListener( new BotonCambiarPath( ((JTextField)tf), false ) );
						} else {
							// error - tipo aún no implementado - no se tiene en cuenta
							btn.setVisible( false );
						}
						panel1.add(btn);
					}
					listaTfPropiedad.add( tf );
				hayMasTF = (i<mensajes.length-1 && mensajes[i+1]==null);
				if (hayMasTF) {
					i++;
					propiedad.add( props[i] );
					valorDefecto.add( (defecto[i]==null)?"":defecto[i] );
				}
			} while (hayMasTF);
			contentPanel.add( panel1 );
		}
		JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.CENTER));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("Confirmar");
				okButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						botonAceptar();
					}
				});
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancelar");
				cancelButton.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						botonCancelar();
					}
				});
				buttonPane.add(cancelButton);
			}
		getProps();  // Inicializa las propiedades desde el fichero
		pack();
	}
	
	/** Construye un diálogo con definición particular de configuración de propiedades
	 * @param nomFic	Nombre del fichero en el que se guardarán/cargarán las propiedades
	 * 		Cada vez que se abra el diálogo interactivo, se guardarán el fichero si hay cualquier cambio
	 * @param props	Clase enum con los nombres de las propiedades
	 * @param mensajes	Array de mensajes a mostrar al usuario con cada propiedad
	 * @param defecto	Array de strings de valores por defecto de las propiedades
	 * @param tipos	Array de Strings con los siguientes valores: null o "" si es un texto normal
	 *      "FIC" si corresponde a un path de fichero, "DIR" si corresponde a un path de carpeta 
	 * @throws IndexOutOfBoundsException	Los cuatro arrays props, mensajes, defecto, carpetas deben tener la misma longitud, con
	 * 		correspondencia uno a uno. Si alguno tiene longitud diferente no se crea el objeto y se lanza esta excepción
	 */
	public VentanaEdicionGenericaConfig ( String nomFic, Class<?> props, String[] mensajes, String[] defecto, String[] tipos ) throws IndexOutOfBoundsException {
		this( nomFic, enumAArray(props), mensajes, defecto, tipos );
	}
		private static String[] enumAArray( Class<?> props ) {
			try {
				Object[] vals = (Object[]) props.getMethod( "values" ).invoke( null );
				String[] ret = new String[ vals.length ];
				for (int i=0; i<vals.length; i++) ret[i] = vals[i].toString();
				return ret;
			} catch (Exception e) {
				e.printStackTrace();
				return new String[0];
			}
		}


	// Eventos de cambio
	public static interface EventoCambio {
		public void hayCambio( String propiedadCambiada, String valorNuevo );
	}
	
	public void addEventoDeCambioInteractivo( EventoCambio evento ) {
		listaEventos.add( evento );
	}
		
	// Guarda los cambios en el fichero de configuración
		private boolean aceptar = false;
	private void botonAceptar() {
		aceptar = true;
		if (hayCambios) {
			for (int i=0; i < propiedad.size(); i++) {
				misProps.setProperty( propiedad.get(i), listaTfPropiedad.get(i).getText() );
			}
			saveProps();
		}
		hayCambios = false;
		setVisible(false);
	}
	
	private void botonCancelar() {
		hayCambios = false;
		setVisible(false);
	}
	
	
	/** Muestra el cuadro de diálogo y devuelve la información de cierre
	 * @return	true si se ha cerrado con el botón "Confirmar", false si se ha cancelado o cerrado la ventana
	 */
	public boolean mostrar() {
		refrescaPropsVentana();
		aceptar = false;
		setVisible( true );
		dispose();
		return aceptar;
	}
	
	private class BotonCambiarPath implements ActionListener {
		private JTextField tf;
		private boolean soloDirs;
		BotonCambiarPath( JTextField tf, boolean soloDirs ) {
			this.tf = tf;
			this.soloDirs = soloDirs;
		}
		@Override
		public void actionPerformed(ActionEvent e) {
			JFileChooser fc = new JFileChooser( tf.getText() );
			String mens = "Elige nuevo fichero";
			if (soloDirs) { 
				fc.setFileSelectionMode( JFileChooser.DIRECTORIES_ONLY );
				mens = "Elige nueva carpeta";
			}
			int valor = fc.showDialog( VentanaEdicionGenericaConfig .this, mens );
			if (valor == JFileChooser.APPROVE_OPTION) {
				boolean avisoDeCambio = (!tf.getText().replaceAll("/", "\\\\").equalsIgnoreCase( fc.getSelectedFile().getPath().replaceAll("/", "\\\\") ) );
				tf.setText( fc.getSelectedFile().getPath() );
				hayCambios = true;
				if (avisoDeCambio) {
					for (EventoCambio ec : listaEventos) {
						ec.hayCambio( propiedad.get( listaTfPropiedad.indexOf( tf ) ), tf.getText() );
					}
				}
			}
		}
	}

	/** Recupera las propiedades de configuración, cargando el fichero de propiedades si es necesario.
	 * @return	objeto Properties que guarda las propiedades de configuración 
	 */
	public Properties getProps() {
		if (misProps == null) initProps();
		return misProps;
	}
	
	/** Devuelve el valor de la propiedad indicada. Funciona como una llamada a getProperty sobre getProps().
	 * @param propName	Nombre de la propiedad que se busca
	 * @return	Valor de la propiedad, null si esta propiedad no existe
	 */
	public String getProp( String propName ) {
		if (misProps == null) initProps();
		return misProps.getProperty( propName );
	}
	
	/** Cambia el valor de la propiedad indicada. Funciona como una llamada a setProperty sobre getProps().
	 * @param propName	Nombre de la propiedad que se quiere modificar
	 * @param valor	Valor a poner
	 */
	public void setProp( String propName, String valor ) {
		if (misProps == null) initProps();
		misProps.setProperty( propName, valor );
	}
	
	/** Devuelve el valor de la propiedad indicada. Funciona como una llamada a getProperty sobre getProps().
	 * @param propName	Enumerado con el nombre de la propiedad que se busca
	 * @return	Valor de la propiedad, null si esta propiedad no existe
	 */
	public String getProp( Enum<?> propName ) {
		if (misProps == null) initProps();
		return misProps.getProperty( propName.toString() );
	}
	
	/** Cambia el valor de la propiedad indicada. Funciona como una llamada a setProperty sobre getProps().
	 * @param propName	Enumerado con el nombre de la propiedad que se quiere modificar
	 * @param valor	Valor a poner
	 */
	public void setProp( Enum<?> propName, String valor ) {
		if (misProps == null) initProps();
		misProps.setProperty( propName.toString(), valor );
	}
	
	/** Devuelve el valor de la propiedad indicada como un entero. Funciona como una llamada a getProperty sobre getProps().
	 * @param propName	Nombre de la propiedad que se busca
	 * @param def	Valor por defecto que se devuelve en lugar de -1, si se indica, cuando el la propiedad no existe o no es un entero correcto
	 * @return	Valor de la propiedad, -1 si esta propiedad no existe o su valor no es un entero correcto
	 */
	public int getPropInt( String propName, int... def ) {
		int ret = -1;
		if (def.length>0) ret = def[0];
		if (misProps == null) initProps();
		try {
			ret = Integer.parseInt( misProps.getProperty( propName ) );
		} catch (Exception e) {}
		return ret;
	}
	
	/** Devuelve el valor de la propiedad indicada como un entero. Funciona como una llamada a getProperty sobre getProps().
	 * @param propName	Enumerado con el nombre de la propiedad que se busca
	 * @return	Valor de la propiedad, -1 si esta propiedad no existe o su valor no es un entero correcto
	 */
	public int getPropInt( Enum<?> propName ) {
		int ret = -1;
		if (misProps == null) initProps();
		try {
			ret = Integer.parseInt( misProps.getProperty( propName.toString() ) );
		} catch (Exception e) {}
		return ret;
	}
	
	/** Devuelve el valor de la propiedad indicada como un double. Funciona como una llamada a getProperty sobre getProps().
	 * @param propName	Nombre de la propiedad que se busca
	 * @param def	Valor por defecto que se devuelve en lugar de Double.MAX_VALUE, si se indica, cuando el la propiedad no existe o no es un double correcto
	 * @return	Valor de la propiedad, Double.MAX_VALUE si esta propiedad no existe o su valor no es un double correcto
	 */
	public double getPropDouble( String propName, double... def ) {
		double ret = Double.MAX_VALUE;
		if (def.length>0) ret = def[0];
		if (misProps == null) initProps();
		try {
			ret = Double.parseDouble( misProps.getProperty( propName ) );
		} catch (Exception e) {}
		return ret;
	}
	
	/** Devuelve el valor de la propiedad indicada como un double. Funciona como una llamada a getProperty sobre getProps().
	 * @param propName	Enumerado con el nombre de la propiedad que se busca
	 * @return	Valor de la propiedad, Double.MAX_VALUE si esta propiedad no existe o su valor no es un double correcto
	 */
	public double getPropDouble( Enum<?> propName ) {
		double ret = Double.MAX_VALUE;
		if (misProps == null) initProps();
		try {
			ret = Double.parseDouble( misProps.getProperty( propName.toString() ) );
		} catch (Exception e) {}
		return ret;
	}
	
	/** Devuelve el valor de la propiedad indicada. Funciona como una llamada a getProperty sobre getProps().
	 * Genera una excepción si la propiedad no existe
	 * @param propName	Nombre de la propiedad que se busca
	 * @return	Valor de la propiedad
	 */
	public String getPropEx( String propName ) throws NullPointerException {
		if (misProps == null) initProps();
		String ret = misProps.getProperty( propName );
		if (ret==null) throw new NullPointerException( "Propiedad no existente: " + propName );
		return ret;
	}
	
	/** Devuelve el valor de la propiedad indicada. Funciona como una llamada a getProperty sobre getProps().
	 * Genera una excepción si la propiedad no existe
	 * @param propName	Enumerado con el nombre de la propiedad que se busca
	 * @return	Valor de la propiedad
	 */
	public String getPropEx( Enum<?> propName ) throws NullPointerException {
		if (misProps == null) initProps();
		String ret = misProps.getProperty( propName.toString() );
		if (ret==null) throw new NullPointerException( "Propiedad no existente: " + propName );
		return ret;
	}
	
	// Crea las propiedades, cargándolos de fichero si existe
	private void initProps() {
		misProps = new Properties();
		try {
			misProps.loadFromXML( new FileInputStream( nomFic ) );
		} catch (Exception e) { // Valores por defecto
			for (int i=0; i < propiedad.size(); i++) {
				misProps.setProperty( propiedad.get(i), valorDefecto.get(i) );
			}
			hayCambios = true;
		}
		refrescaPropsVentana();
	}
	
	/** Refresca explícitamente los valores de propiedad actual en la ventana de edición de propiedades
	 * (adecuado si se cambia programáticamente algún valor mientras la ventana está activa)
	 */
	public void refrescaPropsVentana() {
		for (int i=0; i < propiedad.size(); i++) {
			listaTfPropiedad.get(i).setText( misProps.getProperty( propiedad.get(i) ));
		}
	}
	
	/** Refresca explícitamente el valor de propiedad actual en la ventana de edición de propiedades
	 * (adecuado si se cambia programáticamente algún valor mientras la ventana está activa)
	 * @param propARefrescar	Propiedad a refrescar (solo se refresca esa)
	 */
	public void refrescaPropsVentana( String propARefrescar ) {
		for (int i=0; i < propiedad.size(); i++) {
			if (propiedad.get(i).equals(propARefrescar)) {
				listaTfPropiedad.get(i).setText( misProps.getProperty( propiedad.get(i) ));
			}
		}
	}
	
	/** Guarda el fichero de propiedades con los valores que estén actualmente definidos
	 */
	public void saveProps() {
		try {
			BackupFiles.makeBakFiles( nomFic, 5, false );
			misProps.storeToXML( new PrintStream( nomFic ), "Propiedades de AEFicConfiguracion" );
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	

	public static enum A { b, c, d, e };
	
	/** Main de prueba, genera un fichero de configuración con unas pocas propiedades
	 */
	public static void main(String[] args) {
		String[] props = { "UltimoUsuario", "UltimaFecha", "UltimoFic", "DirDatos" };
		String[] mensajes = { "Último usuario que ha accedido: ", "Fecha de último acceso: ", "Último fichero abierto: ", "Directorio de datos: " };
		String[] defecto = { "admin", null, ".", "c:\\Users\\" };
		String[] tipos = { "", "", "FIC", "DIR" };
		try {
			VentanaEdicionGenericaConfig  dialogo = new VentanaEdicionGenericaConfig ( "prueba-properties.xml", props, mensajes, defecto, tipos );
			boolean confirmado = dialogo.mostrar();  // Edición interactiva de configuración (hasta que no confirma o cancela el usuario no se devuelve el control)
			System.out.println( confirmado );
			// Otra manera de hacerlo sin valor de retorno:
			// dialogo.setVisible(true);  // Edición interactiva de configuración (hasta que no confirma o cancela el usuario no se devuelve el control)
			// dialogo.dispose();  // Cierra la ventana para que swing acabe
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
