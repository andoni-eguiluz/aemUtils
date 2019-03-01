package net.eguiluz.aem.utils;

import java.util.StringTokenizer;

public class Codificador {
	/** Codifica un valor dada una serie de valores de referencia (no se consideran mayúsculas o minúsculas)
	 * @param valor	Valor a codificar
	 * @param refCodigos	Array de valores de referencia. Cada uno de ellos puede tener varias posibilidades, separadas por comas
	 * @param sinDigitosAlrededor	true si se quiere forzar a que en los extremos del valor de referencia no pueda haber dígitos
	 * @return	valor codificado: 1-n si corresponde a uno de los valores de referencia (0-n-1), 0 si el valor está vacío, n+1 si el valor no se ha podido corresponder a las referencias ("otros")
	 */
	public static int codifica( String valor, String[] refCodigos, boolean sinDigitosAlrededor ) {
		int ret = 0;
		if (valor!=null && !valor.isEmpty()) {
			valor = valor.toUpperCase();
			int codigo = 1;
			for (String referencias : refCodigos) {
				StringTokenizer st = new StringTokenizer( referencias, "," );
				while (st.hasMoreTokens()) {
					String ref = st.nextToken().toUpperCase();
					int posi = valor.indexOf( ref );
					if (posi!=-1) {
						if (sinDigitosAlrededor) {
							char antes = (posi==0) ? '#' : valor.charAt(posi-1);
							char despues = (posi+ref.length()>=valor.length()) ? '#' : valor.charAt(posi+ref.length());
							if (Character.isDigit(antes) || Character.isDigit(despues)) posi = -1;
						}
						if (posi!=-1) {
							if (ret==0) { ret = codigo; break; }  // Marca el código y obvia el resto de referencias del mismo código
							else return refCodigos.length+1;  // Si hay dos códigos distintos es que no se puede reconocer: devuelve "otros"
						}
					}
				}
				codigo++;
			}
			if (ret==0) ret = refCodigos.length + 1;
		}
		return ret;
	}
	
	/** Codifica un valor dada una serie de valores de referencia exactos
	 * @param valor	Valor a codificar
	 * @param refCodigos	Array de valores de referencia. Cada uno de ellos puede tener varias posibilidades, separadas por comas
	 * @param conMayusc	true si no se quieren considerar mayúsculas y minúsculas, false en caso contrario
	 * @return	valor codificado: 1-n si corresponde a uno de los valores de referencia (0-n-1), 0 si el valor está vacío, n+1 si el valor no se ha podido corresponder a las referencias ("otros")
	 */
	public static int codificaExacto( String valor, String[] refCodigos, boolean conMayusc ) {
		int ret = 0;
		if (valor!=null && !valor.isEmpty()) {
			if (!conMayusc) valor = valor.toUpperCase();
			int codigo = 1;
			for (String referencias : refCodigos) {
				StringTokenizer st = new StringTokenizer( referencias, "," );
				while (st.hasMoreTokens()) {
					String ref = st.nextToken();
					if (!conMayusc) ref = ref.toUpperCase();
					if (valor.equals(ref)) {
						if (ret==0) { ret = codigo; break; }  // Marca el código y obvia el resto de referencias del mismo código
						else return refCodigos.length+1;  // Si hay dos códigos distintos es que no se puede reconocer: devuelve "otros"
					}
				}
				codigo++;
			}
			if (ret==0) ret = refCodigos.length + 1;
		}
		return ret;
	}
}
