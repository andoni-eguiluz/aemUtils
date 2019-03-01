package net.eguiluz.aem.utils;

import java.util.ArrayList;
import java.util.StringTokenizer;

/** Calcula distancia entre dos strings
 * Obtenida en mayo 2016 de:  http://stackoverflow.com/questions/955110/similarity-string-comparison-in-java
 */
public class StringSimilarity {

	  /**
	   * Calculates the similarity (a number within 0 and 1) between two strings.
	   */
	  public static double similarity(String s1, String s2) {
	    String longer = s1, shorter = s2;
	    if (s1.length() < s2.length()) { // longer should always have greater length
	      longer = s2; shorter = s1;
	    }
	    int longerLength = longer.length();
	    if (longerLength == 0) { return 1.0; /* both strings are zero length */ }
	    /* // If you have StringUtils, you can use it to calculate the edit distance:
	    return (longerLength - StringUtils.getLevenshteinDistance(longer, shorter)) /
	                               (double) longerLength; */
	    return (longerLength - editDistance(longer, shorter)) / (double) longerLength;

	  }

	  // Example implementation of the Levenshtein Edit Distance
	  // See http://rosettacode.org/wiki/Levenshtein_distance#Java
	  public static int editDistance(String s1, String s2) {
	    s1 = s1.toLowerCase();
	    s2 = s2.toLowerCase();

	    int[] costs = new int[s2.length() + 1];
	    for (int i = 0; i <= s1.length(); i++) {
	      int lastValue = i;
	      for (int j = 0; j <= s2.length(); j++) {
	        if (i == 0)
	          costs[j] = j;
	        else {
	          if (j > 0) {
	            int newValue = costs[j - 1];
	            if (s1.charAt(i - 1) != s2.charAt(j - 1))
	              newValue = Math.min(Math.min(newValue, lastValue),
	                  costs[j]) + 1;
	            costs[j - 1] = lastValue;
	            lastValue = newValue;
	          }
	        }
	      }
	      if (i > 0)
	        costs[s2.length()] = lastValue;
	    }
	    return costs[s2.length()];
	  }

	  public static void printSimilarity(String s, String t) {
	    System.out.println( String.format( "%.3f is the similarity between \"%s\" and \"%s\"", similarity(s, t), s, t) );
	  }


	  
	  // Métodos añadidos por Andoni
	  
	  /** Calcula la similitud media de un string al resto
	   * @param setStrings	Set de todos los strings
	   * @param numString	�ndice del string a chequear (0 - n-1)
	   * @return	similitud media (de 0.0 -ninguna- a 1.0 -iguales-)
	   */
	  public static double calcSimilitudMedia( String[] setStrings, int numString ) {
		  double distancia = 0.0;
		  for (int i=0; i<setStrings.length; i++) {
			  if (i!= numString) {
				  distancia += similarity( setStrings[numString], setStrings[i] );
			  }
		  }
		  return distancia/(setStrings.length-1);
	  }
	  
	  /** Calcula la similitud media de todos los strings
	   * @param setStrings	Set de todos los strings
	   * @return	similitud media (de 0.0 -ninguna- a 1.0 -iguales-)
	   */
	  public static double calcSimilitudMedia( String[] setStrings ) {
		  double distancia = 0.0;
		  int cont = 0;
		  for (int i=0; i<setStrings.length; i++) {
			  for (int j=i+1; j<setStrings.length; j++) {
				  distancia += similarity( setStrings[i], setStrings[j] );
				  cont++;
			  }
		  }
		  return distancia/cont;
	  }
	  
	  /** Calcula el string intermedio de una lista de strings (el que mayor similitud media tiene con el resto)
	   * @param setStrings	Set de todos los strings
	   * @return	�ndice del String con mayor similitud media
	   */
	  public static int calcStringIntermedio( String[] setStrings ) {
		  double mayorSimilitud = 0.0;
		  int indMayorSimilitud = -1;
		  for (int i=0; i<setStrings.length; i++) {
			  double similitud = calcSimilitudMedia( setStrings, i );
			  // System.out.println( "Similitud media de " + setStrings[i] + " al resto = " + similitud );
			  if (similitud > mayorSimilitud) {
				  mayorSimilitud = similitud;
				  indMayorSimilitud = i;
			  }
		  }
		  return indMayorSimilitud;
	  }
	  
	  /** Aproxima los strings de una lista, convirti�ndolos a may�sculas y quitando espacios iniciales y finales
	   * @param setStrings
	   */
	  public static void aproximaStrings( String[] setStrings ) {
		  for (int i=0; i<setStrings.length; i++) {
			  setStrings[i] = setStrings[i].trim().toUpperCase();
		  }
	  }
	  
	  /** Calcula la palabra m�s frecuente
	   * @param setStrings
	   * @return
	   */
	  public static String palabraMasFrecuente( String[] setStrings, boolean convertirAMayusculas ) {
		  ArrayList<String> palabras = new ArrayList<>();
		  for (int i=0; i<setStrings.length; i++) {
			  StringTokenizer st = new StringTokenizer( setStrings[i], " \t\n-" );
			  if (convertirAMayusculas)
				  while (st.hasMoreTokens()) palabras.add( st.nextToken().toUpperCase() );
			  else
				  while (st.hasMoreTokens()) palabras.add( st.nextToken() );
		  }
		  int posiPalMasFrec = calcStringIntermedio( palabras.toArray( new String[0] ) );
		  if (posiPalMasFrec==-1) return "";
		  return palabras.get(posiPalMasFrec);
	  }
	  
	  /** Devuelve la similitud media de una palabra con la palabra m�s aproximada de cada string
	   * @param setStrings
	   * @param palabra
	   * @return
	   */
	  public static double calcSimilitudMediaPalabra( String[] setStrings, String palabra, boolean convertirAMayusculas ) {
		  if (palabra==null || palabra.isEmpty()) return 0.0; // Si la palabra es vac�a la similitud es cero
		  ArrayList<String> palabras = new ArrayList<>();
		  for (int i=0; i<setStrings.length; i++) {
			  StringTokenizer st = new StringTokenizer( setStrings[i], " \t\n-" );
			  String palabraMasSimil = "";
			  double similitud = 0.0;
			  while (st.hasMoreTokens()) {
				  String nuevaPalabra = st.nextToken();
				  if (convertirAMayusculas) nuevaPalabra = nuevaPalabra.toUpperCase();
				  double nuevaSimilitud = similarity( palabra, nuevaPalabra );
				  if (nuevaSimilitud > similitud) {
					  similitud = nuevaSimilitud;
					  palabraMasSimil = nuevaPalabra;
				  }
			  }
			  palabras.add( palabraMasSimil );
		  }
		  // System.out.println( palabras );
		  return calcSimilitudMedia( palabras.toArray( new String[0] ) );
	  }
	  
	  public static void main(String[] args) {
		/*
	    printSimilarity("", "");
	    printSimilarity("1234567890", "1");
	    printSimilarity("1234567890", "123");
	    printSimilarity("1234567890", "1234567");
	    printSimilarity("1234567890", "1234567890");
	    printSimilarity("1234567890", "1234567980");
	    printSimilarity("47/2010", "472010");
	    printSimilarity("47/2010", "472011");
	    printSimilarity("47/2010", "AB.CDEF");
	    printSimilarity("47/2010", "4B.CDEFG");
	    printSimilarity("47/2010", "AB.CDEFG");
	    printSimilarity("The quick fox jumped", "The fox jumped");
	    printSimilarity("The quick fox jumped", "The fox");
	    printSimilarity("kitten", "sitting");
	    */

		String[] testStrings = {
		  "dolore ibarruri",
		  "IES Dolores Ibarruri BHI",
		  "Dolores Ibarruri",
		  "IES Dolores Ibarruri BHI",
		  "Dolores Ibarruri",
		  "dolores ibarruri",
		  "Dolores Ibarruri BHI",
		  "IES Dolores Ibarruri BHI",
		  "Dolores Ibarruri",
		  "Dolores Ibarruri",
		  "DI",
		  "Dolores Ibarruri"
		};

		String[] testStrings2 = {
				"Maristas",
				"maristas",
				"Maristas bilbao",
				"marisatas",
				"Maristas Bilbao el salvador",
				"maristas bilbao",
				"Maristas",
				"Maristas",
				"Maristas",
				"maristas bilbao",
				"Maristas el salbador",
				"Maristas El Salvador ikastetxea",
				"el salbador maristas bilbao",
				"El Salvador Maristas Bilbao",
				"Maristas",
				"maristas",
				" El Salbador Maristas",
				"maristas bilbao",
				"El Salvador Maristas",
				"El Salvador Maristas Bilbao",
				"maristas bilbao",
				"Maristas",
				"El Salvador Maristas Bilbao",
				"el salavador maristas",
				"Maristas",
				"maristas",
				"el salavador maristas"
		};
		
		aproximaStrings( testStrings );
		System.out.println( "String intermedio: " + testStrings[ calcStringIntermedio( testStrings ) ] );
		System.out.println( "Similitud media: " + calcSimilitudMedia(testStrings ) );
		
		aproximaStrings( testStrings2 );
		System.out.println( "String intermedio: " + testStrings2[ calcStringIntermedio( testStrings2 ) ] );
		System.out.println( "Similitud media: " + calcSimilitudMedia(testStrings2 ) );
		
		String palabra = palabraMasFrecuente( testStrings2, true );
		System.out.println( "Palabra m�s frecuente: " + palabra );
		System.out.println( "Similitud media de palabra m�s frecuente: " + calcSimilitudMediaPalabra( testStrings2, palabra, true ) );

	  }
}