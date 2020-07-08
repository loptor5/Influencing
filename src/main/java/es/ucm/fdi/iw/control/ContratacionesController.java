package es.ucm.fdi.iw.control;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.ucm.fdi.iw.model.Candidatura;
import es.ucm.fdi.iw.model.Evento;
import es.ucm.fdi.iw.model.Evento.Tipo;
import es.ucm.fdi.iw.model.Propuesta;
import es.ucm.fdi.iw.model.Usuario;
import es.ucm.fdi.iw.model.Valoracion;

@Controller()
@RequestMapping("contrataciones")
public class ContratacionesController {
	private static final Logger log = LogManager.getLogger(ContratacionesController.class);
	@Autowired
	private EntityManager entityManager;
	private final int NUM_ELEMENTOS_PAGINA = 3;

	@GetMapping("")
	public String getContrataciones(Model model, HttpSession session) {
		List<Candidatura> candidaturas;
		candidaturas = entityManager.createNamedQuery("Candidatura.getContrataciones", Candidatura.class)
				.setParameter("idUsuario", ((Usuario) session.getAttribute("u")).getId()).getResultList();
		model.addAttribute("mensaje", session.getAttribute("mensajeInfo"));
		session.removeAttribute("mensajeInfo");
		model.addAttribute("numeroPaginas", Math.ceil((double) candidaturas.size() / NUM_ELEMENTOS_PAGINA));
		if (NUM_ELEMENTOS_PAGINA <= candidaturas.size())
			candidaturas = candidaturas.subList(0, NUM_ELEMENTOS_PAGINA);
		model.addAttribute("valoradas", devuelveContratacionesValoradas(((Usuario) session.getAttribute("u")).getId()));
		model.addAttribute("modo", "Contrataciones");
		model.addAttribute("resultadoBusqueda", candidaturas);
		model.addAttribute("numNotificaciones", entityManager.createNamedQuery("Evento.getNotificacionesUnread")
				.setParameter("idUsuario", ((Usuario) session.getAttribute("u")).getId()).getResultList().size());

		return "contrataciones";
	}

	private List<Long> devuelveContratacionesValoradas(long idUsuario) {
		List<Long> retorno = new ArrayList<>();
		retorno = entityManager.createNamedQuery("Valoraciones.getByUser", Long.class)
				.setParameter("idUsuario", idUsuario).getResultList();
		return retorno;
	}

	@GetMapping("/busca")
	public String postSearch(Model model, HttpSession session,
			@RequestParam(required = true, defaultValue = "1") int indicePagina, @RequestParam String patron) {
		String patronParaLike = "%" + patron + "%";
		List<Candidatura> candidaturas = null;
		candidaturas = entityManager.createNamedQuery("Candidatura.searchByName", Candidatura.class)
				.setParameter("patron", patronParaLike.toUpperCase())
				.setParameter("idUsuario", ((Usuario) session.getAttribute("u")).getId()).getResultList();
		model.addAttribute("numeroPaginas", Math.ceil((double) candidaturas.size() / NUM_ELEMENTOS_PAGINA));
		if (indicePagina * NUM_ELEMENTOS_PAGINA <= candidaturas.size())
			candidaturas = candidaturas.subList((indicePagina - 1) * NUM_ELEMENTOS_PAGINA,
					indicePagina * NUM_ELEMENTOS_PAGINA);
		else
			candidaturas = candidaturas.subList((indicePagina - 1) * NUM_ELEMENTOS_PAGINA, candidaturas.size());

		model.addAttribute("valoradas", devuelveContratacionesValoradas(((Usuario) session.getAttribute("u")).getId()));
		model.addAttribute("modo", "Resultados de la búsqueda");
		model.addAttribute("patron", patron);
		model.addAttribute("resultadoBusqueda", candidaturas);
		return "fragments/resultadoBusquedaContrataciones";
	}

	@GetMapping("/estado")
	public String postSearchByTag(Model model, HttpSession session,
			@RequestParam(required = true, defaultValue = "1") int indicePagina, @RequestParam String estado) {
		String estadoLike;
		List<Candidatura> candidaturas = null;

		if (estado.equals(" ALL")) {
			candidaturas = entityManager.createNamedQuery("Candidatura.getContrataciones", Candidatura.class)
					.setParameter("idUsuario", ((Usuario) session.getAttribute("u")).getId()).getResultList();
		} else {
			estadoLike = "%" + estado + "%";
			candidaturas = entityManager.createNamedQuery("Candidatura.searchByEstado", Candidatura.class)
					.setParameter("estado", estadoLike)
					.setParameter("idUsuario", ((Usuario) session.getAttribute("u")).getId()).getResultList();
		}

		model.addAttribute("numeroPaginas", Math.ceil((double) candidaturas.size() / NUM_ELEMENTOS_PAGINA));
		if (indicePagina * NUM_ELEMENTOS_PAGINA <= candidaturas.size())
			candidaturas = candidaturas.subList((indicePagina - 1) * NUM_ELEMENTOS_PAGINA,
					indicePagina * NUM_ELEMENTOS_PAGINA);
		else
			candidaturas = candidaturas.subList((indicePagina - 1) * NUM_ELEMENTOS_PAGINA, candidaturas.size());

		String tipoBusqueda = "";
		switch (estado) {
		case " ALL":
			model.addAttribute("valoradas",
					devuelveContratacionesValoradas(((Usuario) session.getAttribute("u")).getId()));
			break;
		case "EN_CURSO":
			tipoBusqueda = "en curso";
			break;
		case "EN_VALORACION":
			tipoBusqueda = "en valoración";
			model.addAttribute("valoradas",
					devuelveContratacionesValoradas(((Usuario) session.getAttribute("u")).getId()));
			break;
		case "FINALIZADA":
			tipoBusqueda = "finalizadas";
			break;
		}

		model.addAttribute("modo", "Contrataciones " + tipoBusqueda);
		model.addAttribute("resultadoBusqueda", candidaturas);
		model.addAttribute("patron", estado);
		return "fragments/resultadoBusquedaContrataciones";
	}

	@GetMapping("/valorar")
	public String valorarContratacion(Model model, HttpSession session, @RequestParam long idCandidatura,
			@RequestParam long idPropuesta) {
		Propuesta p = entityManager.find(Propuesta.class, idPropuesta);
		model.addAttribute("modo", "CREACION");
		model.addAttribute("propuesta", p);
		model.addAttribute("idCandidatura", idCandidatura);
		model.addAttribute("idUsuario", ((Usuario) session.getAttribute("u")).getId());
		return "modals/valoracion";
	}

	@GetMapping("/verValoracion")
	public String verValoracion(Model model, HttpSession session, @RequestParam long idCandidatura,
			@RequestParam long idPropuesta) {
		Propuesta p = entityManager.find(Propuesta.class, idPropuesta);
		model.addAttribute("modo", "VISTA");
		Valoracion v = entityManager.createNamedQuery("Valoraciones.getByEmisorAndCandidatura", Valoracion.class)
				.setParameter("idUsuario", ((Usuario) session.getAttribute("u")).getId())
				.setParameter("idCandidatura", idCandidatura).getSingleResult();
		model.addAttribute("valoracion", v);
		model.addAttribute("propuesta", p);
		model.addAttribute("idCandidatura", idCandidatura);
		model.addAttribute("idUsuario", ((Usuario) session.getAttribute("u")).getId());
		return "modals/valoracion";
	}

	@GetMapping("/verValoracionInicio")
	public String verValoracionInicio(Model model, HttpSession session, @RequestParam long idValoracion) {
		model.addAttribute("modo", "VISTA");
		Valoracion v = entityManager.find(Valoracion.class, idValoracion);
		model.addAttribute("valoracion", v);
		model.addAttribute("propuesta", v.getCandidatura().getPropuesta());
		model.addAttribute("idCandidatura", v.getCandidatura().getId());
		model.addAttribute("idUsuario", ((Usuario) session.getAttribute("u")).getId());
		return "modals/valoracion";
	}

	@PostMapping("/valorar")
	@Transactional
	public void valoraContratacion(Model model, HttpServletResponse response, HttpSession session,
			@RequestParam long idCandidatura, @RequestParam String valoracion, @RequestParam float puntuacion) {

		String mensaje = "";
		if (!(entityManager.createNamedQuery("Valoraciones.getByEmisorAndCandidatura", Valoracion.class)
				.setParameter("idUsuario", ((Usuario) session.getAttribute("u")).getId())
				.setParameter("idCandidatura", idCandidatura).getResultList().isEmpty())) {
			mensaje = "Ya has valorado esta candidatura";
		} else if (puntuacion > 5 || puntuacion < 0) {
			mensaje = "Puntuación introducida no válida (debe situarse entre 0 y 5)";
		} else {
			Candidatura c = entityManager.find(Candidatura.class, idCandidatura);
			if (c.getCandidato().getId() == ((Usuario) session.getAttribute("u")).getId()
					|| c.getPropuesta().getEmpresa().getId() == ((Usuario) session.getAttribute("u")).getId()) {

				Valoracion v = new Valoracion();
				v.setCandidatura(c);
				v.setEmisor(entityManager.find(Usuario.class, ((Usuario) session.getAttribute("u")).getId()));
				v.setPuntuacion(puntuacion);
				v.setValoracion(valoracion);
				entityManager.persist(v);
				entityManager.flush();
				Usuario valorado;
				if (((Usuario) session.getAttribute("u")).hasRole(Usuario.Rol.EMPRESA)) {
					valorado = entityManager.find(Usuario.class, c.getCandidato().getId());
					valorado.updatePuntuacion(puntuacion);
					entityManager.persist(valorado);

				} else {
					valorado = entityManager.find(Usuario.class, c.getPropuesta().getEmpresa().getId());
					valorado.updatePuntuacion(puntuacion);
					entityManager.persist(valorado);
				}

				if (entityManager.createNamedQuery("Valoraciones.getByCandidatura", Valoracion.class)
						.setParameter("idCandidatura", idCandidatura).getResultList().size() == 2) {
					c.setEstado(Candidatura.Estado.FINALIZADA.toString());
					entityManager.persist(c);
				}
				enviaValoracionUsuario(valorado, v.getEmisor(),
						"El usuario " + v.getEmisor().getNombre() + " ha valorado tu contratación en la propuesta "
								+ v.getCandidatura().getPropuesta().getNombre() + " con " + v.getPuntuacion()
								+ " puntos.",
						v);
				mensaje = "Valoración insertada correctamente";
			}
			session.setAttribute("mensajeInfo", mensaje);
			try {
				response.sendRedirect("/contrataciones");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.info("Error al redireccionar");
			}
		}
	}

	@Transactional
	private void enviaValoracionUsuario(Usuario valorado, Usuario emisor, String mensaje, Valoracion valoracion) {
		// TODO Auto-generated method stub
		Evento e = new Evento();
		e.setEmisor(emisor);
		e.setReceptor(valorado);
		e.setValoracion(valoracion);
		e.setDescripcion(mensaje);
		e.setFechaEnviado(LocalDateTime.now());
		e.setLeido(false);
		e.setTipo(Tipo.VALORACION);
		entityManager.persist(e);

	}

	@GetMapping("/vista")
	public String vistaContratacion(Model model, HttpSession session, @RequestParam long idPropuesta) {
		Propuesta p = entityManager.find(Propuesta.class, idPropuesta);
		model.addAttribute("modo", "CONTRATACION");
		model.addAttribute("propuesta", p);
		return "modals/propuesta";
	}

}
