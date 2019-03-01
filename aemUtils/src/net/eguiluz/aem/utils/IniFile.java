package net.eguiluz.aem.utils;

import java.awt.*;
import java.io.*;
import java.util.*;

/**
 * @author andoni
 * Process an ini file (.ini)
 * Format of an ini file:
 *  [SectionName]
 *  varName=varValue
 *  varName=varValue
 *  ...
 *  If there is no section, "default" is used.
 *  File is loaded at creation, and saved into a persistant file only when save() method is called.
 *  Manipulation methods act on memory data.
 */
public class IniFile implements Enumeration<IniFile.IniData> {
	private String fileName;
	private File iniFile;
	private Vector<IniData> data;
	private String lastSection = "default";
	private boolean spaces = false;
	private boolean duplicates = false;
	private boolean changed = false;  // true when memory changes are not saved on .ini file
	// Enumeration attributes
	enum EnumerationType {EN_NONE, EN_COMPLETE, EN_SECTION, EN_VARIABLE};
	private EnumerationType enumType = EnumerationType.EN_NONE;
	private int nextEnumElement = -1;  // -1 if there is not next element

	/**
	 * Creates a new ini file object, and loads from disk if related file existed.
	 * Path is actual dir, leading and trailing spaces are not considered in variables and values.
	 * @param validFileName      Filename to load/save the file
	 * @param duplicatesAllowed  if false, only one value per section/variable is saved.
	 * @throws IOException    Thrown if any I/O error (caution: changes will not be saved)
	 */
	public IniFile( String validFileName, boolean duplicatesAllowed ) throws IOException {
		this( validFileName, "", false, false, false, duplicatesAllowed );
	}
	
	/**
	 * Creates a new ini file object, and loads from disk if related file existed.
	 * @param validFileName     Filename to load/save the file
	 * @param optionalPath      Path of that file (current if null or "")
	 * @param fileMustExist     if true, file must exist before. If false, file will be created.
	 * @param interactiveFileSelIfDoesntExists   if true and file does not exist, a dialog is showed to choose another one.
	 * @param spacesAllowed     if false, leading and trailing spaces are removed from variable names and values.
	 * @param duplicatesAllowed if false, only one value per section/variable is saved.
	 * @throws IOException    Thrown if file doesn't exist (and fileMustExist=true), or any I/O error (caution: changes will not be saved)
	 */
	public IniFile( String validFileName, String optionalPath, boolean fileMustExist, boolean interactiveFileSelIfDoesntExists, boolean spacesAllowed, boolean duplicatesAllowed ) throws IOException {
		spaces = spacesAllowed;
		duplicates = duplicatesAllowed;
		if (optionalPath == null) optionalPath = "";
		fileName = optionalPath + validFileName;
		if (!fileName.endsWith(".INI") && !fileName.endsWith(".ini")) fileName = fileName + ".ini";
    	iniFile = new File(fileName);
    	if (!iniFile.exists() && interactiveFileSelIfDoesntExists) {
            FileDialog f = new FileDialog( (Dialog)null, "Selecciona fichero ini (" + validFileName + ".ini)", FileDialog.LOAD );
            f.setVisible(true);
            if (f.getFile() == null) {
            	if (fileMustExist) {
            		throw new IOException( "ini file does not exist: " + fileName );
            	}
            } else {
            	fileName = f.getDirectory() + f.getFile();
            	iniFile = new File( fileName );
            }
    	}
        data = new Vector<IniData>();
    	if (!iniFile.exists()) {  // If it doesn't exist, try to create
    		try {
    			PrintStream outFile = new PrintStream( new FileOutputStream( fileName, true ) );
    			outFile.close();
    		} catch (FileNotFoundException e) {
    			throw new IOException( "invalid ini file name: " + fileName );
    		}
    	} else {  // If it exists, load values
    	    try {
		        BufferedReader input =  new BufferedReader(new FileReader(iniFile));
		        String line = null; //not declared within while loop
		        /* readLine is a bit quirky :
		         *  it returns the content of a line MINUS the newline.
		         *  it returns null only for the END of the stream.
		         *  it returns an empty String if two newlines appear in a row.
		        */
		        int equalPos = 0;
		        String lastSection = "default";
		        while ((line = input.readLine()) != null) {
		        	// Ini line process
		        	// System.out.println( line );
					if (line.startsWith("[") && line.endsWith("]")) {   // Section
						lastSection = line.substring(1, line.length()-1);
					} else if ((equalPos = line.indexOf("=")) > 0) {    // Variable = value
						String var = line.substring(0,equalPos);
						String val = line.substring(equalPos+1);
						IniData newData = new IniData( lastSection, noSpaces(var), noSpaces(val) );
						data.add( newData );
						// System.out.println( "  Added: " + newData );
					} else {  // Sintactically bad line
						// Do nothing
					}
		        }
    	    } 
    	    catch (IOException ex){
        		throw new IOException( "ini file does not exist: " + fileName );
    	    }
    	}
	}

	/** Sets actual section
	 * @param section
	 */
	public void setSection( String section ) {
		lastSection = section;
	}
	/** Adds a variable=value pair in actual section.
	 * @param variable
	 * @param value
	 */
	public void add( String variable, String value ) {
		add( lastSection, variable, value );
	}
	/** Adds a variable=value pair in actual section.
	 * @param variable
	 * @param value
	 * @param replaceIfExists   if true, if variable exists it is replaced, otherwise duplicated.
	 */
	public void add( String variable, String value, boolean replaceIfExists ) {
		add( lastSection, variable, value, replaceIfExists );
	}
	/** Adds a variable=value pair in a section. Doesn't change actual section.
	 * @param section
	 * @param variable
	 * @param value
	 */
	public void add( String section, String variable, String value ) {
		add( section, variable, value, !duplicates );
	}
	/** Adds a variable=value pair in a section. Doesn't change actual section.
	 * @param section
	 * @param variable
	 * @param value
	 * @param replaceIfExists   if true, if variable exists it is replaced, otherwise duplicated.
	 */
	public void add( String section, String variable, String value, boolean replaceIfExists ) {
		// System.out.println( data + "size: " + data.size() );
		changed = true;
		int insertPoint = data.size();
		for (int i = 0; i < data.size(); i++) {
			IniData id = data.get(i);
			if (id.getSection().equalsIgnoreCase( section )) {  // Inserts all variables in same section positions
				insertPoint = i+1;
				if (id.getVariable().equalsIgnoreCase( variable )) {
					if (!replaceIfExists) { 
						insertPoint = i; 
						i = data.size(); 
					} else {  // replace instead of insert
						id.setValue( value );
						return;
					}
				}
			}
		}
		// System.out.println( data + "insert at: " + insertPoint );
		data.insertElementAt( new IniData( section, variable, value ), insertPoint );
		// System.out.println( data );
	}

	/** Deletes a variable in a section. All values if many.
	 * @param section
	 * @param variable
	 * @param value
	 */
	public void delete( String section, String variable ) {
		int i = 0; 
		while (i < data.size()) { 
			IniData id = data.get(i);
			if (id.getSection().equalsIgnoreCase( section ) &&
				id.getVariable().equalsIgnoreCase( variable )) {
				data.removeElementAt(i);
				changed = true;
			} else i++;
		}
	}

	/** Deletes a variable=value pair in a section. Doesn't change actual section.
	 * @param section
	 * @param variable
	 * @param value
	 */
	public void delete( String section, String variable, String value ) {
		int i = 0; 
		while (i < data.size()) { 
			IniData id = data.get(i);
			if (id.getSection().equalsIgnoreCase( section ) &&
				id.getVariable().equalsIgnoreCase( variable ) &&
				id.getValue().equalsIgnoreCase( value )) {
				data.removeElementAt(i);
				changed = true;
				i = data.size();
			} else i++;
		}
	}
	
	/** Returns first value of pair section - variable, null if it doesn't exist.
	 * @param section
	 * @param variable
	 * @return	Value / null
	 */
	public String getValue( String section, String variable ) {
		for (int i = 0; i < data.size(); i++) {
			IniData id = data.get(i);
			if (id.getSection().equalsIgnoreCase( section ) && id.getVariable().equalsIgnoreCase( variable )) { 
				return id.getValue();
			}
		}
		return null;
	}
	
	/** Returns first value of variable, null if it doesn't exist  (the section doesn't matter)
	 * @param variable
	 * @return  Value / null
	 */
	public String getValue( String variable ) {
		for (int i = 0; i < data.size(); i++) {
			IniData id = data.get(i);
			if (id.getVariable().equalsIgnoreCase( variable )) {
				return id.getValue();
			}
		}
		return null;
	}
	
	/** Same as getValue, but translating value into an int. Returns Integer.MIN_VALUE if doesn't exist or it is not a number
	 * @param section
	 * @param variable
	 * @return  int value / Integer.MIN_VALUE
	 */
	public int getIntValue( String section, String variable ) {
		String v = getValue( section, variable );
		try {
			return Integer.parseInt( v );
		} catch (NumberFormatException e) {
		}
		return Integer.MIN_VALUE;
	}
	
	/** Same as getValue, but translating value into an int. Returns Integer.MIN_VALUE if doesn't exist or it is not a number
	 * @param variable
	 * @return  int value / Integer.MIN_VALUE
	 */
	public int getIntValue( String variable ) {
		String v = getValue( variable );
		try {
			return Integer.parseInt( v );
		} catch (NumberFormatException e) {
		}
		return Integer.MIN_VALUE;
	}
	
	/** Same as getValue, but translating value into an boolean. Returns defaultValue if doesn't exist or it is not a boolean
	 * @param section
	 * @param variable
	 * @param defaultValue
	 * @return  boolean value
	 */
	public boolean getBoolValue( String section, String variable, boolean defaultValue ) {
		String v = getValue( section, variable );
		if (v == null) return defaultValue;
		if (v.equalsIgnoreCase( "false" )) return false;
		if (v.equalsIgnoreCase( "true" )) return true;
		return defaultValue;
	}
	
	/**
	 * Saves actual object into its .ini file
	 * @return   true if correctly saved
	 */
	public boolean save() {
		boolean ok = true;
		if (iniFile != null) {   // If correctly created and not closed
    		try {
    			// Backsup old ini file
    			PrintStream bakFile = new PrintStream( new FileOutputStream( fileName+".bak", false ) );
		        BufferedReader input =  new BufferedReader(new FileReader(iniFile));
		        String line = null; //not declared within while loop
		        while ((line = input.readLine()) != null) {
		        	bakFile.println( line );
		        }
    			// Saves new ini file
    			PrintStream outFile = new PrintStream( new FileOutputStream( fileName, false ) );
    			String lastSection="";
    			for (int i = 0; i < data.size(); i++) {
    				IniData id = data.get(i);
    				if (!lastSection.equals(id.getSection())) {  // New section
    					lastSection = id.getSection();
    					outFile.println( "[" + lastSection + "]" );
    				}
    				outFile.println( noSpaces(id.getVariable()) + "=" + noSpaces(id.getValue()) );
    			}
    			outFile.close();
    			changed = false;
    		} catch (FileNotFoundException e) {
    			ok = false;
    		} catch (IOException e) {
    			ok = false;
    		}
		}
		return ok;
	}
	
	/**
	 * Closes actual object, saving contents if changed. Object must not be used from now on.
	 */
	public void close() {
		if (changed) save();
		iniFile = null;  // Avoiding further manipulation
		// Rest of close actions not actually needed
	}
	
	/** Inits enumeration of all elements
	 */
	public void initCompleteEnumeration() {
		enumType = EnumerationType.EN_COMPLETE;
		nextEnumElement = (data.size() == 0) ? -1 : 0;  // -1 if there is not next element
	}
	
	/** Inits enumeration of all elements of indicated section
	 */
	public void initSectionEnumeration( String section ) {
		enumType = EnumerationType.EN_SECTION;
		int i = 0; 
		while (i < data.size() && !data.get(i).getSection().equalsIgnoreCase(section))
			i++;
		nextEnumElement = (i >= data.size()) ? -1 : i;  // -1 if there is not next element
	}
	
	/** Inits enumeration of all elements of indicated variable (& section)
	 */
	public void initVariableEnumeration( String section, String variable ) {
		enumType = EnumerationType.EN_VARIABLE;
		int i = 0; 
		while (i < data.size() && (!data.get(i).getSection().equalsIgnoreCase(section)
				                   || !data.get(i).getVariable().equalsIgnoreCase(variable)))
			i++;
		nextEnumElement = (i >= data.size()) ? -1 : i;  // -1 if there is not next element
	}
	
	/** Inits enumeration of all elements of indicated variable (independently of section)
	 */
	public void initVariableEnumeration( String variable ) {
		enumType = EnumerationType.EN_VARIABLE;
		int i = 0; 
		while (i < data.size() && !data.get(i).getVariable().equalsIgnoreCase(variable))
			i++;
		nextEnumElement = (i >= data.size()) ? -1 : i;  // -1 if there is not next element
	}
	
	/* (non-Javadoc)
	 * @see java.util.Enumeration#hasMoreElements()
	 */
	public boolean hasMoreElements() {
		return (nextEnumElement != -1);
	}

	/* (non-Javadoc)
	 * @see java.util.Enumeration#nextElement()
	 */
	public IniData nextElement() {
		if (nextEnumElement != -1) {
			int i = nextEnumElement;
			nextEnumElement++;
			switch (enumType) {
				case EN_SECTION: {
					if (nextEnumElement < data.size() && 
						!data.get(nextEnumElement).getSection().equalsIgnoreCase(data.get(i).getSection()))
						nextEnumElement = -1;
					break; }
				case EN_VARIABLE: {
					if (nextEnumElement < data.size() && 
						!data.get(nextEnumElement).getVariable().equalsIgnoreCase(data.get(i).getVariable()))
						nextEnumElement = -1;
					break; }
			}
			if (nextEnumElement >= data.size()) nextEnumElement = -1;
			return data.get(i); 
		} else
			throw new NoSuchElementException( "No next element");
	}
	
	// Utilitary method
	private String noSpaces( String st ) {
		if (spaces) return st.trim(); else return st;
	}

	public class IniData {
		private String section;
		private String variable;
		private String value;
		public IniData( String pSection, String pVariable, String pValue ) {
			section = pSection; variable = pVariable; value = pValue;
		}
		public String getSection() { return section; }
		public String getVariable() { return variable; }
		public String getValue() { return value; }
		/** Same as getValue, but translating value into an int. Returns Integer.MIN_VALUE if doesn't exist or it is not a number
		 * @return  int value / Integer.MIN_VALUE
		 */
		public int getIntValue( ) {
			try {
				return Integer.parseInt( value );
			} catch (NumberFormatException e) {
				return Integer.MIN_VALUE;
			}
		}
		public void setSection( String pSection ) { section = pSection; }
		public void setVariable( String pVariable ) { variable = pVariable; }
		public void setValue( String pValue ) { value = pValue; }
		public String toString() { return "["+section+"] "+variable+"="+value; }
	}

}

