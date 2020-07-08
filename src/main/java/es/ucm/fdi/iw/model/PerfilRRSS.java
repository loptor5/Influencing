package es.ucm.fdi.iw.model;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;


@NamedQueries({
	@NamedQuery(name="PerfilRRSS.byInfluencer",
	query="SELECT p FROM PerfilRRSS p "
			+ "WHERE p.influencer.id = :idUsuario"),
	
	@NamedQuery(name="PerfilRRSS.byNombre",
	query="SELECT p FROM PerfilRRSS p "
			+ "WHERE p.nombre = :nombre AND p.rrss = :rrss")
})


@Entity
public class PerfilRRSS {

	private long id;
	private Usuario influencer;
	private String nombre;
	private int numSeguidores;
	public enum RRSS {Twitter, Facebook, Instagram, Youtube};
	private String rrss;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long getId() {
		return this.id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@ManyToOne(targetEntity = Usuario.class)
	public Usuario getInfluencer() {
		return this.influencer;
	}

	public void setInfluencer(Usuario influencer) {
		this.influencer = influencer;
	}

	public String getNombre() {
		return this.nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public int getNumSeguidores() {
		return this.numSeguidores;
	}

	public void setNumSeguidores(int numSeguidores) {
		this.numSeguidores = numSeguidores;
	}

	public String getRrss() {
		return rrss;
	}

	public void setRrss(String rrss) {
		this.rrss = rrss;
	}
	
	public void setRRSS(RRSS rrss) {
		this.rrss = rrss.toString();
	}
	
}
