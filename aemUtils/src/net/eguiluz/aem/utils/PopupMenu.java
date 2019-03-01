package net.eguiluz.aem.utils;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import javax.swing.*;
import java.util.Hashtable;


/**
 *
 * @author andoni
 */
public class PopupMenu implements ActionListener, MouseListener {
    private JPopupMenu popup;
    private ArrayList<Component> myComponents = new ArrayList<Component>();
    private ActionListener myActionListener;
    private String[] myOptions;
    private JMenuItem[] myMenuItems;

    /** Crea un nuevo objeto de menú popup
     * @param options   Array de textos de cada opción, de arriba abajo. null indica separador
     * @param commands  Array de textos comandos para cada opción (debe coincidir en tamaño con options)
     * @param actionCommandListener Escuchador al que se llamará al activar cada opción con el comando de acción correspondiente
     *      Se ejecutará el método actionPerformed(ActionEvent e) de este escuchador. Ese evento e recibirá el getSource()
     *      desde el componente al cual vaya asociado el popup
     */
    public PopupMenu( String[] options, String[] commands, ActionListener actionCommandListener ) {
        popup = new JPopupMenu();
        myActionListener = actionCommandListener;
        if (options.length != commands.length) throw new IllegalArgumentException( "Different number of strings on PopupMenu creation: " + 
                options.length + " options vs. " + commands.length + " commands." );
        myOptions = options;
        myMenuItems = new JMenuItem[ myOptions.length ];
        for (int i = 0; i<options.length; i++) {
            if (options[i] == null) {
                myMenuItems[i] = null;
                popup.addSeparator();
            } else {
                myMenuItems[i] = new JMenuItem( options[i] );
                myMenuItems[i].addActionListener(this);
                myMenuItems[i].setActionCommand( commands[i] );
                popup.add(myMenuItems[i]);
            }
        }
    }

    /** Pone activada o desactivada la opción de menú indicada con su texto
     * @param option
     * @param enabledOn
     */
    public void setEnabled( String option, boolean enabledOn ) {
        for (int i = 0; i < myOptions.length; i++) {
            if (myOptions[i] != null && myOptions[i].equals(option)) {
                myMenuItems[i].setEnabled( enabledOn );
                return;
            }
        }
    }

    /** Pone activada o desactivada la opción de menú indicada con su posición (de 0 a n-1)
     * @param optionIndex
     * @param enabledOn
     */
    public void setEnabled( int optionIndex, boolean enabledOn ) {
        if (optionIndex >= 0 && optionIndex < myOptions.length && myMenuItems[optionIndex] != null) {
            myMenuItems[optionIndex].setEnabled( enabledOn );
        }
    }

    /** Pone activada o desactivada las opciones de menú
     * @param enabledOn Array de booleanos indicando la activación/desactivación de cada opción (de arriba abajo)
     */
    public void setEnabled( boolean... enabledOn ) {
        for (int i = 0; i < enabledOn.length; i++) {
            if (i < myMenuItems.length && myMenuItems[i] != null) { myMenuItems[i].setEnabled( enabledOn[i] ); }
        }
    }

    public void actionPerformed(ActionEvent e) {
        e.setSource( lastComponent );
        myActionListener.actionPerformed( e );
        // System.out.println("actionPerformed, event=" + e + ", mod=" + getMods(e));
        // System.out.println(" command=" + e.getActionCommand());
        // System.out.println(" param=" + e.paramString());
        // System.out.println(" source=" + e.getSource());
    }

  private String getMods(ActionEvent e) {
    return getMods(e.getModifiers());
  }

  private String getMods(MouseEvent e) {
    return getMods(e.getModifiers());
  }

  // a convenience routine for printing the Modifier keys
  private String getMods(int mods) {
    String modstr = "";
    if ((mods & ActionEvent.SHIFT_MASK) == ActionEvent.SHIFT_MASK)
      modstr += (" SHIFT");
    if ((mods & ActionEvent.ALT_MASK) == ActionEvent.ALT_MASK)
      modstr += (" ALT");
    if ((mods & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK)
      modstr += (" CTRL");
    if ((mods & ActionEvent.META_MASK) == ActionEvent.META_MASK)
      modstr += (" META");
    return modstr;
  }

  public void mouseClicked(MouseEvent e) {
    mouseAction("mouseClicked", e);
  }

  public void mouseEntered(MouseEvent e) {
  }

  public void mouseExited(MouseEvent e) {
  }

  public void mousePressed(MouseEvent e) {
    mouseAction("mousePressed", e);
  }

  public void mouseReleased(MouseEvent e) {
    mouseAction("mouseReleased", e);
  }

  private Component lastComponent = null;
    private void mouseAction(String which, MouseEvent e) {
System.out.println( "##" + e.getComponent() );
        Component c = e.getComponent();
        // System.out.println(which + "e=" + e + ", mods=" + getMods(e) + ", component=" + c);
        if (e.isPopupTrigger()) {
            // System.out.println("isPopup");
            lastComponent = e.getComponent();
            popup.show( e.getComponent(), e.getX(), e.getY() );
        }
    }

    /** Asocia este popup al componente indicado para su lanzamiento automático
     * @param c
     * @param name
     */
    public void addToComponent(Component c, String name) {
        myComponents.add( c );
        //Add listener to components that can bring up popup menus.
        c.addMouseListener( this );
    }

    public void removeFromAllComponents() {
        while (!myComponents.isEmpty()) {
            myComponents.get(0).removeMouseListener( this );
            myComponents.remove( 0 );
        }
    }

    public void removeFromComponent( Component c ) {
        for (Component comp : myComponents) {
            if (comp==c) {
                comp.removeMouseListener( this );
                myComponents.remove( comp );
                return;
            }
        }
    }

    public void manualShow( Component c, int x, int y ) {
        lastComponent = c;
        popup.show( c, x, y );
    }

//  Hashtable popupTable = new Hashtable();
//
//  void setHash(Component c, PopupMenu p) {
//    popupTable.put(c, p);
//  }
//
//  PopupMenu getHash(Component c) {
//    return (PopupMenu) (popupTable.get(c));
//  }
//

    // Método de test
    public static void main(String argv[]) {
        new PopupDemo().setVisible(true);
    }

}

class PopupDemo extends JFrame implements ActionListener {
    private PopupMenu pm2;
    private Panel p;

    public PopupDemo() {
        MenuBar mb = new MenuBar();
        setMenuBar(mb);
        Menu m = new Menu("cambio");
        mb.add(m);
        MenuItem item = new MenuItem("Poner popup en panel");
        item.setActionCommand( "poner" );
        item.addActionListener(this);
        m.add(item);
        item = new MenuItem("Quitar popup de panel");
        item.setActionCommand( "quitar" );
        item.addActionListener(this);
        m.add(item);

        setSize(100, 100);
        setLayout(new BorderLayout());

        Label l = new Label("label");
        PopupMenu pm1 = new PopupMenu( new String[] { "Hola", "Adiós" }, new String[] { "Hola", "Adiós" }, this
                );
        pm1.addToComponent( l, "label" );
        add( l, "North" );

        p = new Panel();
        pm2 = new PopupMenu( new String[] { "Abrir", null, "Cerrar", "Copiar" }, new String[] { "COM_ABRIR", null, "COM_CERRAR", "COM_COPIAR" }, this );
        pm2.addToComponent( p, "Panel" );
        pm2.setEnabled( "Cerrar", false );
        add( p, "Center" );

        Button b = new Button("button");
        pm2.addToComponent( b, "button" );
        add(b, "South");
        b.addActionListener( new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                pm2.manualShow( p, 100, 50 );
            }
        });

        setSize(500,300);
        setDefaultCloseOperation( JFrame.EXIT_ON_CLOSE );
    }

    public void actionPerformed(ActionEvent e) {
        if (e.getActionCommand().equals("poner")) {
            pm2.addToComponent( p, "Panel" );
        } else if (e.getActionCommand().equals("quitar")) {
            pm2.removeFromComponent( p );
        } else {
            System.out.println( e.getActionCommand() + " desde -> " + e.getSource() );
        }
    }

}
