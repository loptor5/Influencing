package es.ucm.fdi.iw.model;

import javax.persistence.*;

@Entity
@NamedQueries({
	@NamedQuery(name="Valoraciones.getByUser",
	query="SELECT v.candidatura.id FROM Valoracion v WHERE v.emisor.id = :idUsuario"),
	
	@NamedQuery(name="Valoraciones.getByEmisorAndCandidatura",
	query="SELECT v FROM Valoracion v WHERE v.emisor.id = :idUsuario AND v.candidatura.id = :idCandidatura"),
	
	@NamedQuery(name="Valoraciones.getByCandidatura",
	query="SELECT v FROM Valoracion v WHERE v.candidatura.id = :idCandidatura")
})
public class Valoracion {

	private long id;
	private Usuario emisor;
	private Candidatura candidatura;
	private String valoracion;
	private float puntuacion;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@OneToOne(targetEntity = Usuario.class)
	public Usuario getEmisor() {
		return emisor;
	}

	public void setEmisor(Usuario emisor) {
		this.emisor = emisor;
	}

	@ManyToOne(targetEntity = Candidatura.class)
	public Candidatura getCandidatura() {
		return candidatura;
	}

	public void setCandidatura(Candidatura candidatura) {
		this.candidatura = candidatura;
	}

	public String getValoracion() {
		return valoracion;
	}

	public void setValoracion(String valoracion) {
		this.valoracion = valoracion;
	}

	public float getPuntuacion() {
		return puntuacion;
	}

	public void setPuntuacion(float puntuacion) {
		this.puntuacion = puntuacion;
	}
}
