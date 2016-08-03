package idare.imagenode.Properties;

/**
 * A Class encapsulating the Localisation information (i.e. CENTER//EDGE/FREE) and flexibility information.
 * @author Thomas Pfau
 *
 */
public class Localisation {
	public static enum Position {CENTER, EDGE, FREE};
	
	public boolean Flexible;
	public Position pos;
	/**
	 * DEfault constructor using a position and a flexibility idicator
	 * @param pos
	 * @param flexibility
	 */
	public Localisation(Position pos, boolean flexibility )
	{
		this.pos = pos;
		Flexible = flexibility;
	}
}

