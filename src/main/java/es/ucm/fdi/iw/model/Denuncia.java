package es.ucm.fdi.iw.model;

import java.time.LocalDateTime;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id; 
import javax.persistence.ManyToOne;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@NamedQueries({
	@NamedQuery(name="Denuncia.getAllDenuncias", query="SELECT d FROM Denuncia d")
})
public class Denuncia {

    private long id;
    private Usuario denunciante;
    private Usuario denunciado;
    private String titulo;
    private String descripcion;
    private boolean tramitada;
    private LocalDateTime fecha;


    public String getTitulo() {
        return this.titulo;
    }

    public void setTitulo(String titulo) {
        this.titulo = titulo;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    public long getId() {
        return this.id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @ManyToOne(targetEntity = Usuario.class)
    public Usuario getDenunciante() {
        return this.denunciante;
    }

    public void setDenunciante(Usuario denunciante) {
        this.denunciante = denunciante;
    }

    @ManyToOne(targetEntity = Usuario.class)
    public Usuario getDenunciado() {
        return this.denunciado;
    }

    public void setDenunciado(Usuario denunciado) {
        this.denunciado = denunciado;
    }

    public String getDescripcion() {
        return this.descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public boolean isTramitada() {
        return this.tramitada;
    }

    public void setTramitada(boolean tramitada) {
        this.tramitada = tramitada;
    }

	public LocalDateTime getFecha() {
		return this.fecha;
	}

	public void setFecha(LocalDateTime fecha) {
		this.fecha = fecha;
	}

}
