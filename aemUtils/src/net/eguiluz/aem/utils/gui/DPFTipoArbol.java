package net.eguiluz.aem.utils.gui;

/** Enumerado de tipo de árbol a visualizar
 * @author andoni
 */
public enum DPFTipoArbol {
    DPF_IGNORADOS_EN_FUENTE, DPF_IGNORADOS_EN_DESTINO, DPF_SOLO_EN_FUENTE, DPF_SOLO_EN_DESTINO, DPF_MODERNO_EN_FUENTE, DPF_MODERNO_EN_DESTINO, DPF_IGUALES, DPF_FICHEROS_GRANDES, DPF_ERRORES_COPIA;
    public static final String[] nombreTipo = { "Ignorados en origen", "Ignorados en destino", "Sólo en origen", "Sólo en destino", "Más moderno en origen", "Más moderno en destino", "Iguales", "Ficheros grandes", "Errores de copia" };
    public static final boolean[] sonDeFuente = { true, false, true, false, true, false, true, true, false };
    public static final boolean[] sonDeDestino = { false, true, false, true, false, true, true, true, true };
    public String getDescription() { return nombreTipo[ this.ordinal() ]; }
    public static int valueOfDescription( String desc ) {
        for (int i = 0; i < nombreTipo.length; i++) {
            if (nombreTipo[i].equals(desc)) return i;
        }
        return -1;
    }
}
