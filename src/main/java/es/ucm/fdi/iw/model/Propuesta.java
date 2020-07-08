package es.ucm.fdi.iw.model;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.OneToMany;


@Entity
@NamedQueries({
	
	@NamedQuery(name="Propuesta.getAllProposals",
	query="SELECT p FROM Propuesta p WHERE p.activa = true AND p.tipo = 'PROPUESTA'"),
	
	@NamedQuery(name="Propuesta.searchByNombre",
	query="SELECT p FROM Propuesta p "
		+ "WHERE upper(p.nombre) LIKE :nombre AND p.activa = true"),
	
	@NamedQuery(name="Propuesta.searchByTag",
	query="SELECT p FROM Propuesta p "
			+ "WHERE upper(p.tags) LIKE :tag AND p.activa = true")
})
public class Propuesta {

	private long id;
	private List<Candidatura> candidaturas;
	private Usuario empresa;
	private String nombre;
	private String tags;
	private String descripcion;
	public enum Tipo_propuesta {PROPUESTA, ULTIMATUM};
	private String tipo;
	private int sueldo;
	private LocalDateTime fechaInicio;
	private LocalDateTime fechaFin;
	private LocalDateTime fechaSubida;
	private Boolean activa;
	private Boolean verificado;
	private int edadMinPublico;
	private int edadMaxPublico;

	
	public Boolean isActiva() {
		return this.activa;
	}

	public void setActiva(Boolean activa) {
		this.activa = activa;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}
	
	
	public String getTipo() {
		return tipo;
	}

	public void setTipo(Tipo_propuesta tipo) {
		this.tipo = tipo.toString();
	}
	
	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public LocalDateTime getFechaInicio() {
		return fechaInicio;
	}
	
	public void setFechaInicio(LocalDateTime fechaInicio) {
		this.fechaInicio = fechaInicio;
	}

	public LocalDateTime getFechaFin() {
		return fechaFin;
	}

	public void setFechaFin(LocalDateTime fechaFin) {
		this.fechaFin = fechaFin;
	}


	@OneToMany(targetEntity=Candidatura.class)
	@JoinColumn(name="propuesta_id")
	public List<Candidatura> getCandidaturas() {
		return candidaturas;
	}

	public void setCandidaturas(List<Candidatura> candidaturas) {
		this.candidaturas = candidaturas;
	}

	@ManyToOne(targetEntity = Usuario.class)
	public Usuario getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Usuario empresa) {
		this.empresa = empresa;
	}

	public String getNombre() {
		return nombre;
	}

	public void setNombre(String nombre) {
		this.nombre = nombre;
	}

	public String getDescripcion() {
		return descripcion;
	}

	public void setDescripcion(String descripcion) {
		this.descripcion = descripcion;
	}

	public int getSueldo() {
		return sueldo;
	}

	public void setSueldo(int sueldo) {
		this.sueldo = sueldo;
	}

	public String getTags() {
		return tags;
	}

	public void setTags(String tags) {
		this.tags = tags;
	}
	
	public boolean hasTag(String tag) {
		return Arrays.stream(tags.split(","))
				.anyMatch(r -> r.equals(tag));
	}

	public int getEdadMinPublico() {
		return edadMinPublico;
	}

	public void setEdadMinPublico(int edadMinPublico) {
		this.edadMinPublico = edadMinPublico;
	}

	public int getEdadMaxPublico() {
		return edadMaxPublico;
	}

	public void setEdadMaxPublico(int edadMaxPublico) {
		this.edadMaxPublico = edadMaxPublico;
	}

	public LocalDateTime getFechaSubida() {
		return fechaSubida;
	}

	public void setFechaSubida(LocalDateTime fechaSubida) {
		this.fechaSubida = fechaSubida;
	}

	public Boolean getVerificado(){
		return this.verificado;
	}

	public void setVerificado(Boolean verificado){
		this.verificado = verificado;
	}


}
