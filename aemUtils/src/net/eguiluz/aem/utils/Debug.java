package net.eguiluz.aem.utils;

import java.io.*;
import java.text.DateFormat;
import java.util.*;
import javax.swing.*;
import javax.swing.text.BadLocationException;

/** Class used for debugging.<p>
 * Usage: not for instances, only static methods
 * By default, debug is ON
 * By default, debug is also in window
 * By default, debug is not logged on file  (use activateDebugLog)
 * By default, the prefix to debug messages is [DEBUG]
 * To show some debugging info, use show( message )
 * @author andoni
 *
 */
public class Debug {
	static private boolean debugActivated = true;
	static private boolean debugInWindow = true;
	static private boolean debugInConsole = true;
	static private boolean debugLogging = false;
	static private PrintStream logFile = null;
	static private String prefix = "[DEBUG] ";
	static private DebugWindow debugWindow = new DebugWindow();
	static private int MAXLINES = 200;
	static public void setDebug( boolean on ) {
		debugActivated = on;
		if (!debugActivated && debugWindow != null) debugWindow.setVisible( false );
	}
	static public boolean isActivated( ) {
		return debugActivated;
	}
	static public void setWindowView( boolean on ) {
		debugInWindow = on;
		if (debugInWindow) {
			if (debugWindow == null) debugWindow = new DebugWindow();
			if (debugActivated) debugWindow.setVisible( true );
		}
	}
	static public void setWindowMaxLines( int maxNumLinesOnWindow ) {
		if (maxNumLinesOnWindow > 2) MAXLINES = maxNumLinesOnWindow;
	}
	static public void setWindowPos( int xPos, int yPos ) {
		debugWindow.setLocation(xPos, yPos);
	}
	static public void showWindow( boolean visible ) {
		if (debugActivated) debugWindow.setVisible( true );
	}
	static public void setConsoleView( boolean on ) {
		debugInConsole = on;
	}
	static public void activateDebugLog( String fileName ) {
		debugLogging = true;
		try {
			logFile = new PrintStream( new FileOutputStream( fileName + ".log", true ) );
			logFile.println( "==========================");
			logFile.println( "Nueva sesión " + fileName + " - " + DateFormat.getDateTimeInstance().format(new Date(System.currentTimeMillis())) );
			logFile.println( "==========================");
		} catch (FileNotFoundException e2) {
			debugLogging = false;
			show( "Couldn't create log file " + fileName + ".log" );
		}
	}
	static public void setPrefix( String debugPrefix ) {
		prefix = debugPrefix + " ";
	}
	static public void show( String mens ) {
		if (!debugActivated) return;
		if (debugInConsole) System.out.println( prefix + mens );
		if (debugLogging) logFile.println( prefix + mens );
		if (debugInWindow) { 
			if (!debugWindow.isVisible()) debugWindow.setVisible(true); 
			debugWindow.taMens.append( prefix + mens + "\n" ); 
			if (debugWindow.taMens.getLineCount() > MAXLINES) {
				try {
					int offsetToCut = debugWindow.taMens.getLineEndOffset( MAXLINES/2 );
					debugWindow.taMens.select(0, offsetToCut);
					debugWindow.taMens.cut();
					int offsetToPos = debugWindow.taMens.getLineEndOffset( debugWindow.taMens.getLineCount()-1 );
					debugWindow.taMens.select(offsetToPos, offsetToPos);
				} catch (BadLocationException e) { }
			}
		}
	}
}

// Private class for use only from Debug class
class DebugWindow extends JFrame {
	private static final long serialVersionUID = -2460316474270817065L;
	JTextArea taMens = new JTextArea(100,50);
	JScrollPane taSc = new JScrollPane( taMens );
	DebugWindow ( ) {
		setSize( 300, 200 );
		setTitle( "Debug Window" );
		getContentPane().add( taSc );
	}
}	
