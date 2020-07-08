package es.ucm.fdi.iw.model;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries({


	@NamedQuery(name="Candidatura.chatsByCandidato", 
	query="SELECT c FROM Candidatura c WHERE (c.candidato.id = :idUsuario OR c.propuesta.empresa.id = :idUsuario) AND (c.estado = 'EN_ULTIMATUM' OR c.estado = 'NEGOCIANDO')"),

	@NamedQuery(name="Candidatura.byCandidatoAndPropuesta", 
	query="SELECT c.propuesta FROM Candidatura c WHERE c.candidato.id = :idCandidato AND c.propuesta.id = :idPropuesta"),
	
	@NamedQuery(name="Candidatura.searchByName",
	query="SELECT c FROM Candidatura c WHERE (c.candidato.id = :idUsuario OR c.propuesta.empresa.id = :idUsuario) "
			+ "AND (c.estado = 'EN_CURSO' OR c.estado = 'EN_VALORACION' OR c.estado = 'FINALIZADA') AND upper(c.propuesta.nombre) LIKE :patron"),
	
	@NamedQuery(name="Candidatura.searchByEstado",
	query="SELECT c FROM Candidatura c WHERE c.estado LIKE :estado AND (c.candidato.id = :idUsuario OR c.propuesta.empresa.id = :idUsuario)"
			+ " ORDER BY c.propuesta.fechaInicio"),
	
	@NamedQuery(name="Candidatura.getByUser",
		query="SELECT c FROM Candidatura c WHERE (c.candidato.id = :idUsuario OR c.propuesta.empresa.id = :idUsuario) AND c.estado != 'RECHAZADA' ORDER BY c.propuesta.fechaInicio"),
	
	@NamedQuery(name="Candidatura.getContrataciones",
	query="SELECT c FROM Candidatura c WHERE (c.candidato.id = :idUsuario OR c.propuesta.empresa.id = :idUsuario) "
			+ "AND (c.estado = 'EN_CURSO' OR c.estado = 'EN_VALORACION' OR c.estado = 'FINALIZADA') ORDER BY c.propuesta.fechaInicio"),
	
	@NamedQuery(name="Candidatura.getByPropuesta",
		query="SELECT c FROM Candidatura c WHERE c.propuesta.id = :idPropuesta AND c.estado != 'RECHAZADA'")
	
})


public class Candidatura {

	private long id;
	private Propuesta propuesta;
	private Usuario candidato;
	private boolean aceptada;
	public enum Estado {NEGOCIANDO, EN_ULTIMATUM, EN_CURSO, EN_VALORACION, FINALIZADA, RECHAZADA};
	private String estado;

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	@ManyToOne(targetEntity = Propuesta.class)
	public Propuesta getPropuesta() {
		return propuesta;
	}

	public void setPropuesta(Propuesta propuesta) {
		this.propuesta = propuesta;
	}

	@ManyToOne(targetEntity = Usuario.class)
	public Usuario getCandidato() {
		return candidato;
	}

	public void setCandidato(Usuario candidato) {
		this.candidato = candidato;
	}

	public Boolean getAceptada() {
		return aceptada;
	}

	public void setAceptada(Boolean aceptada) {
		this.aceptada = aceptada;
	}

	public String getEstado() {
		return estado;
	}

	public void setEstado(String estado) {
		this.estado = estado;
	}

	@Override
	public String toString() {
		return "Candidatura #" + id;
	}

}
