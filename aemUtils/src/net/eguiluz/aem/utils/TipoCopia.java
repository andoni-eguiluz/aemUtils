package net.eguiluz.aem.utils;

/** Clase de tipo de copia, asociada a DirProcess
 * @author andoni
 */
public enum TipoCopia {
    TIPO_COPIA_SINCRONIZACION,   // Borra antiguos, reescribe modificados
    TIPO_COPIA_BACKUP,           // Lleva antiguos y modificados a carpeta de respaldo
    TIPO_COPIA_RESTORE,          // Lleva antiguos y modificados a carpeta de respaldo
    TIPO_COMPARACION,            // Compara e informa de las diferencias entre los dos lugares sin cambiar nada
    TIPO_REVISION                // Revisa los ficheros que hay en el fuente
    ;
            /**
             *
             * @return
             */
    public String getDescription() {
        switch (this) {
            case TIPO_COPIA_SINCRONIZACION: { return "Borra antiguos, reescribe modificados"; }
            case TIPO_COPIA_BACKUP: { return "Lleva antiguos y modificados a carpeta de respaldo"; }
            case TIPO_COPIA_RESTORE: { return "Lleva modificados a carpeta de respaldo, mantiene los antiguos"; }
            case TIPO_COMPARACION: { return "Compara origen y destino e informa de las diferencias, sin hacer cambios"; }
            case TIPO_REVISION: { return "Revisa ficheros en origen, sin considerar destino ni hacer ningún cambio"; }
            default: return "";
        }
    }
    public static String getAllDescriptions() {
        String ret = "";
        for (TipoCopia tc : TipoCopia.values()) {
            ret = ret + tc.toString() + " - " + tc.getDescription() + "\n";
        }
        return ret;
    }
}
