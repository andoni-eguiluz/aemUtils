package net.eguiluz.aem.utils;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

public class BackupFiles {

	/** Crea un fichero de seguridad para un fichero dado, renombrándolo con extensión adicional .bak
	 * @param rutaFich	Ruta del fichero del que crear backup. Si no existe no se hace nada
	 * @param numBackups	Número máximo de backups que se quieren gestionar (>=1). Si es uno, se genera solo un .bak y se borra el anterior. Si es >1, se generan .n.bak sucesivos
	 * @param mueve	Si es true, el fichero original se mueve al de bak (y por tanto no queda ningún fichero con el nombre original). Si es false, se copia (se duplica)
	 * @return	true si el backup ha sido correcto, false si ha habido algún error de copia
	 */
	public static void makeBakFiles( String rutaFich, int numBackups, boolean mueve ) {
		makeBakFiles( new File( rutaFich ), numBackups, mueve );
	}
	/** Crea un fichero de seguridad para un fichero dado, renombrándolo con extensión adicional .bak
	 * @param f	Fichero del que crear backup. Si no existe no se hace nada
	 * @param numBackups	Número máximo de backups que se quieren gestionar (>=1). Si es uno, se genera solo un .bak y se borra el anterior. Si es >1, se generan .n.bak sucesivos
	 * @param mueve	Si es true, el fichero original se mueve al de bak (y por tanto no queda ningún fichero con el nombre original). Si es false, se copia (se duplica)
	 * @return	true si el backup ha sido correcto, false si ha habido algún error de copia
	 */
	public static boolean makeBakFiles( File fInicial, int numBackups, boolean mueve ) {
		if (fInicial.exists()) {
			File fBackupMasAnt = null;
			int numBackup = 0;
			File f = fInicial;
			if (numBackups==1) {
				f = new File( fInicial.getAbsolutePath() + ".bak" );
			} else {
				while (f.exists() && numBackup < numBackups) {
					f = new File( fInicial.getAbsolutePath() + "." + numBackup + ".bak" );
					if (f.exists() && (fBackupMasAnt==null || f.lastModified() < fBackupMasAnt.lastModified())) fBackupMasAnt = f;
					numBackup++;
				}
				if (numBackup==numBackups && f.exists()) f = fBackupMasAnt;  // Si hay el número máximo de backups se coge el más antiguo
			}
			try {
				if (mueve)
					Files.move( fInicial.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING );
				else
					Files.copy( fInicial.toPath(), f.toPath(), StandardCopyOption.REPLACE_EXISTING );
				return true;
			} catch (Exception e) {
				e.printStackTrace();
				return false;
			}
		}
		return true;
	}
	
	/*
	public static void main(String[] args) {
		int NUM_BAKS = 2;
		makeBakFiles( new File( "d:/t/temp/Dormi-1.jpg" ), NUM_BAKS, false );
		new File( "d:/t/temp/Dormi-1.jpg" ).setLastModified( System.currentTimeMillis() );
		try { Thread.sleep( 1000 ); } catch (Exception e) {}
		makeBakFiles( new File( "d:/t/temp/Dormi-1.jpg" ), NUM_BAKS, false );
		new File( "d:/t/temp/Dormi-1.jpg" ).setLastModified( System.currentTimeMillis() );
		try { Thread.sleep( 1000 ); } catch (Exception e) {}
		makeBakFiles( new File( "d:/t/temp/Dormi-1.jpg" ), NUM_BAKS, false );
		new File( "d:/t/temp/Dormi-1.jpg" ).setLastModified( System.currentTimeMillis() );
		try { Thread.sleep( 1000 ); } catch (Exception e) {}
		makeBakFiles( new File( "d:/t/temp/Dormi-1.jpg" ), NUM_BAKS, false );
		new File( "d:/t/temp/Dormi-1.jpg" ).setLastModified( System.currentTimeMillis() );
		try { Thread.sleep( 1000 ); } catch (Exception e) {}
		makeBakFiles( new File( "d:/t/temp/Dormi-1.jpg" ), NUM_BAKS, false );
		new File( "d:/t/temp/Dormi-1.jpg" ).setLastModified( System.currentTimeMillis() );
		try { Thread.sleep( 1000 ); } catch (Exception e) {}
		makeBakFiles( new File( "d:/t/temp/Dormi-1.jpg" ), NUM_BAKS, false );
		new File( "d:/t/temp/Dormi-1.jpg" ).setLastModified( System.currentTimeMillis() );
		try { Thread.sleep( 1000 ); } catch (Exception e) {}
		makeBakFiles( new File( "d:/t/temp/Dormi-1.jpg" ), NUM_BAKS, false );
		new File( "d:/t/temp/Dormi-1.jpg" ).setLastModified( System.currentTimeMillis() );
		try { Thread.sleep( 1000 ); } catch (Exception e) {}
		makeBakFiles( new File( "d:/t/temp/Dormi-1.jpg" ), NUM_BAKS, false );
		new File( "d:/t/temp/Dormi-1.jpg" ).setLastModified( System.currentTimeMillis() );
		try { Thread.sleep( 1000 ); } catch (Exception e) {}
		makeBakFiles( new File( "d:/t/temp/Dormi-1.jpg" ), NUM_BAKS, false );
		new File( "d:/t/temp/Dormi-1.jpg" ).setLastModified( System.currentTimeMillis() );
		try { Thread.sleep( 1000 ); } catch (Exception e) {}
		makeBakFiles( new File( "d:/t/temp/Dormi-1.jpg" ), NUM_BAKS, false );
		new File( "d:/t/temp/Dormi-1.jpg" ).setLastModified( System.currentTimeMillis() );
		try { Thread.sleep( 1000 ); } catch (Exception e) {}
		makeBakFiles( new File( "d:/t/temp/Dormi-1.jpg" ), NUM_BAKS, false );
		try { Thread.sleep( 1000 ); } catch (Exception e) {}
	}
	*/
	
}
