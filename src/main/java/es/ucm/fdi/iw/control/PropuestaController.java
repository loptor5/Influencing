package es.ucm.fdi.iw.control;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.FileCopyUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import es.ucm.fdi.iw.LocalData;
import es.ucm.fdi.iw.model.Candidatura;
import es.ucm.fdi.iw.model.Candidatura.Estado;
import es.ucm.fdi.iw.model.Evento;
import es.ucm.fdi.iw.model.Propuesta;
import es.ucm.fdi.iw.model.Usuario;
import es.ucm.fdi.iw.model.Evento.Tipo;
import es.ucm.fdi.iw.model.Evento.TransferChat;
import es.ucm.fdi.iw.model.Propuesta.Tipo_propuesta;
import es.ucm.fdi.iw.model.Usuario.Rol;

@Controller()
@RequestMapping("propuesta")
public class PropuestaController {

	private static final Logger log = LogManager.getLogger(PropuestaController.class);

	@Autowired
	private LocalData localData;

	@Autowired
	private SimpMessagingTemplate messagingTemplate;

	@Autowired
	private EntityManager entityManager;

	@GetMapping("")
	public String propuesta(Model model, HttpSession session, @RequestParam long idPropuesta) {
		Propuesta p = entityManager.find(Propuesta.class, idPropuesta);
		model.addAttribute("propuesta", p);
		model.addAttribute("modo", "VISTA");
		model.addAttribute("apuntadoPreviamente",
				!entityManager.createNamedQuery("Candidatura.byCandidatoAndPropuesta")
						.setParameter("idCandidato", ((Usuario) session.getAttribute("u")).getId())
						.setParameter("idPropuesta", idPropuesta).getResultList().isEmpty());

		return "modals/propuesta";
	}

	@GetMapping("/creacion")
	public String creacion(Model model, HttpSession session) {
		if (((Usuario) session.getAttribute("u")).hasRole(Rol.EMPRESA)) {
			model.addAttribute("modo", "CREACION");
			return "modals/propuesta";
		}
		return "error";
	}

	@GetMapping("/vistaUltimatum")
	public String vistaUltimatum(Model model, HttpSession session, @RequestParam long idCandidatura) {
		Candidatura c = entityManager.find(Candidatura.class, idCandidatura);
		List<Evento> chat = entityManager.createNamedQuery("Evento.getChat", Evento.class)
				.setParameter("idCandidatura", idCandidatura).getResultList();
		boolean emisorUltimatum = chat.get(chat.size() - 1).getEmisor().getId() == ((Usuario) session.getAttribute("u"))
				.getId();
		model.addAttribute("propuesta", c.getPropuesta());
		model.addAttribute("modo", "VISTA-ULTIMATUM");
		model.addAttribute("propio", emisorUltimatum);
		return "modals/propuesta";
	}

	@GetMapping("/eliminaCandidatura")
	@Transactional
	public void eliminaCandidaturaChat(HttpServletResponse response, Model model, HttpSession session,
			@RequestParam long idCandidatura) {
		Candidatura c = entityManager.find(Candidatura.class, idCandidatura);
		if (c.getCandidato().getId() != ((Usuario) session.getAttribute("u")).getId()
				&& c.getPropuesta().getEmpresa().getId() != ((Usuario) session.getAttribute("u")).getId()) {
			try {
				response.sendRedirect("/error");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.info("Error al redireccionar");
			}
		}

		c.setEstado(Estado.RECHAZADA.toString());
		entityManager.persist(c);

		// Se crea el evento de notificación para el otro usuario
		Usuario emisor = entityManager.find(Usuario.class, ((Usuario) session.getAttribute("u")).getId());
		Usuario receptor = null;
		if (emisor.hasRole(Rol.EMPRESA)) {
			receptor = c.getCandidato();
		} else {
			receptor = c.getPropuesta().getEmpresa();
		}
		Evento e = new Evento();
		e.setTipo(Tipo.NOTIFICACION);
		e.setEmisor(emisor);
		e.setReceptor(receptor);
		e.setFechaEnviado(LocalDateTime.now());
		e.setLeido(false);
		e.setDescripcion("El usuario " + emisor.getNombre() + " " + emisor.getApellidos()
				+ " ha terminado la negociación de la propuesta " + c.getPropuesta().getNombre());
		entityManager.persist(e);

		try {
			response.sendRedirect("/negociacion");
		} catch (IOException error) {
			// TODO Auto-generated catch block
			error.printStackTrace();
		}

	}

	@GetMapping("/modifica")
	public String modifica(Model model, HttpSession session, @RequestParam long idPropuesta) {
		model.addAttribute("modo", "EDICION");
		Propuesta propuesta = entityManager.find(Propuesta.class, idPropuesta);
		if (propuesta.getEmpresa().getId() != ((Usuario) session.getAttribute("u")).getId())
			return "error";
		model.addAttribute("propuesta", propuesta);
		model.addAttribute("fechaInicio", DateTimeFormatter.ofPattern("yyyy-MM-dd").format(propuesta.getFechaInicio()));
		model.addAttribute("fechaFin", DateTimeFormatter.ofPattern("yyyy-MM-dd").format(propuesta.getFechaFin()));

		return "modals/propuesta";
	}

	@PostMapping("/modifica")
	@Transactional
	public void guardaModificacion(HttpSession session, HttpServletResponse response,
			RedirectAttributes redirectAttributes, Model model, @RequestParam String nombre,
			@RequestParam String descripcion, @RequestParam String sueldo, @RequestParam String edades,
			@RequestParam String fechaInicio, @RequestParam String fechaFin,
			@RequestParam MultipartFile imagenPropuesta, @RequestParam String tags, @RequestParam long idPropuesta) {

		String mensaje = "";
		Propuesta p = entityManager.find(Propuesta.class, idPropuesta);
		if (p.getEmpresa().getId() != ((Usuario) session.getAttribute("u")).getId()) {
			try {
				response.sendRedirect("/error");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.info("Error al redireccionar");
			}
		} else {
			LocalDateTime fechaIni = LocalDate.parse(fechaInicio).atTime(LocalTime.now());
			LocalDateTime fechaFinal = LocalDate.parse(fechaFin).atTime(LocalTime.now());
			if (fechaIni.isBefore(LocalDateTime.now())) {
				mensaje = "Error. Las fechas deben ser como mínimo las actuales"; // Comprobar fecha inicio
			} else {
				p.setActiva(true);
				p.setDescripcion(descripcion);
				p.setNombre(nombre);
				p.setEdadMinPublico(Integer.valueOf(edades.split("-")[0]));
				p.setEdadMaxPublico(Integer.valueOf(edades.split("-")[1]));
				p.setSueldo(Integer.valueOf(sueldo));
				p.setFechaSubida(LocalDateTime.now());
				p.setFechaInicio(fechaIni);
				p.setFechaFin(fechaFinal);
				p.setTags(tags.toUpperCase());
				p.setVerificado(false);
				entityManager.persist(p);
				entityManager.flush();
				insertaImagenPropuesta(imagenPropuesta, p.getId());
				mensaje = "Propuesta editada correctamente";
			}

			if (mensaje.equals("Propuesta editada correctamente")) {
				Evento e = new Evento();
				e.setEmisor(entityManager.find(Usuario.class, ((Usuario) session.getAttribute("u")).getId()));
				e.setDescripcion("Se ha editado la propuesta " + p.getNombre());
				e.setFechaEnviado(LocalDateTime.now());
				e.setLeido(false);
				e.setTipo(Evento.Tipo.ADMINISTRACION);
				entityManager.persist(e);
				ObjectMapper mapper = new ObjectMapper();
				ObjectNode rootNode = mapper.createObjectNode();
				rootNode.put("text", "La propuesta " + p.getNombre() + " se ha editado");
				String json = "";
				try {
					json = mapper.writeValueAsString(rootNode);
				} catch (JsonProcessingException error) {
					// TODO Auto-generated catch block
					error.printStackTrace();
				}
				messagingTemplate.convertAndSend("/topic/admin", json);
			}

			session.setAttribute("mensajeInfo", mensaje);
			try {
				response.sendRedirect("/busquedaPropuesta");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.info("Error al redireccionar");
			}
		}
	}

	@GetMapping("/ultimatum")
	public String ultimatum(Model model, HttpSession session, @RequestParam long idCandidatura) {
		Candidatura c = entityManager.find(Candidatura.class, idCandidatura);
		if (c.getCandidato().getId() != ((Usuario) session.getAttribute("u")).getId()
				&& c.getPropuesta().getEmpresa().getId() != ((Usuario) session.getAttribute("u")).getId())
			return "error";
		model.addAttribute("modo", "ULTIMATUM");
		model.addAttribute("propuesta", c.getPropuesta());
		model.addAttribute("idCandidatura", idCandidatura);
		return "modals/propuesta";
	}

	// Ruta que maneja cuando un usuario se apunta a una propuesta. Tiene que crear
	// una candidatura
	@PostMapping("/solicitaPropuesta")
	@Transactional
	public void solicitaPropuesta(Model model, HttpSession session, HttpServletResponse response,
			@RequestParam long idPropuesta) {

		String mensaje = "";
		if (((Usuario) session.getAttribute("u")).hasRole(Rol.INFLUENCER)) {
			try {
				response.sendRedirect("/error");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.info("Error al redireccionar");
			}
		}
		// Comprobar que el usuario no esté apuntado previamente
		if (entityManager.createNamedQuery("Candidatura.byCandidatoAndPropuesta")
				.setParameter("idCandidato", ((Usuario) session.getAttribute("u")).getId())
				.setParameter("idPropuesta", idPropuesta).getResultList().isEmpty()) {
			Usuario usuarioLoggeado = entityManager.find(Usuario.class, ((Usuario) session.getAttribute("u")).getId());
			int edad = usuarioLoggeado.getEdad();
			Propuesta p = entityManager.find(Propuesta.class, idPropuesta);
			if (p.getEdadMinPublico() > edad || p.getEdadMaxPublico() < edad) {
				mensaje = "La edad de tu público no corresponde con la propuesta";
			} else {
				Candidatura c = new Candidatura();
				c.setAceptada(false);
				c.setCandidato(usuarioLoggeado);
				c.setEstado(Candidatura.Estado.NEGOCIANDO.toString());
				c.setPropuesta(p);
				entityManager.persist(c);
				// Enviar mensaje de usuario se ha apuntado a la propuesta como emisor el
				// usuario y receptor la empresa

				Evento e = new Evento();
				e.setDescripcion("El usuario " + usuarioLoggeado.getNombre() + " se ha apuntado a la propuesta");
				e.setCandidatura(c);
				e.setEmisor(usuarioLoggeado);
				e.setFechaEnviado(LocalDateTime.now());
				e.setLeido(false);
				e.setTipo(Tipo.CHAT);
				e.setReceptor(p.getEmpresa());
				entityManager.persist(e);

				e = new Evento();
				e.setDescripcion("El usuario " + usuarioLoggeado.getNombre() + " se ha apuntado a la propuesta "
						+ p.getNombre());
				e.setEmisor(usuarioLoggeado);
				e.setFechaEnviado(LocalDateTime.now());
				e.setLeido(false);
				e.setTipo(Tipo.NOTIFICACION);
				e.setReceptor(p.getEmpresa());
				entityManager.persist(e);
				mensaje = "Te has apuntado correctamente a la propuesta";

			}

		}

		else {
			mensaje = "Ya estabas apuntado a esta propuesta";
		}

		try {
			session.setAttribute("mensajeInfo", mensaje);
			response.sendRedirect("/busquedaPropuesta");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.info("Error al redireccionar");
		}
	}

	// Ruta que maneja cuando un usuario acepta/rechaza ultimatum. Tiene que
	// aceptar/rechazar candidatura
	@PostMapping("/decideUltimatum")
	@Transactional
	public void decideUltimatum(RedirectAttributes redirectAttributes, HttpSession session,
			HttpServletResponse response, Model model, @RequestParam long idPropuesta, @RequestParam boolean decision) {

		String mensaje = "";
		Candidatura candidatura = entityManager.createNamedQuery("Candidatura.getByPropuesta", Candidatura.class)
				.setParameter("idPropuesta", idPropuesta).getSingleResult();
		if (candidatura.getCandidato().getId() != ((Usuario) session.getAttribute("u")).getId()
				&& candidatura.getPropuesta().getEmpresa().getId() != ((Usuario) session.getAttribute("u")).getId()) {
			try {
				response.sendRedirect("/error");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.info("Error al redireccionar");
			}
		}

		Usuario emisor = entityManager.find(Usuario.class, ((Usuario) session.getAttribute("u")).getId());
		Usuario receptor;
		if (emisor.hasRole(Rol.EMPRESA)) {
			receptor = candidatura.getCandidato();
		} else {
			receptor = candidatura.getPropuesta().getEmpresa();
		}
		Evento e = new Evento();
		e.setTipo(Tipo.NOTIFICACION);
		e.setEmisor(emisor);
		e.setReceptor(receptor);
		e.setFechaEnviado(LocalDateTime.now());
		e.setLeido(false);
		if (decision) {
			candidatura.setAceptada(true);
			candidatura.setEstado(Estado.EN_CURSO.toString());
			e.setDescripcion(
					emisor.getNombre() + " " + emisor.getApellidos() + " ha aceptado el ultimátum que enviaste "
							+ "sobre la propuesta " + candidatura.getPropuesta().getNombre()
							+ ".\n La candidatura ha pasado a la sección de contrataciones");
			mensaje = "Has aceptado el ultimátum. Se ha completado la negociación. Puedes ver la propuesta en la sección de contrataciones";

		} else {
			candidatura.setEstado(Estado.RECHAZADA.toString());
			e.setDescripcion(emisor.getNombre() + " " + emisor.getApellidos()
					+ " ha rechazado el ultimátum que enviaste sobre la propuesta "
					+ candidatura.getPropuesta().getNombre() + ".\n La negociación ha finalizado.");
			mensaje = "Has rechazado el ultimátum. Se ha eliminado la negociación.";
		}

		entityManager.persist(e);
		entityManager.persist(candidatura);
		try {
			session.setAttribute("mensajeInfo", mensaje);
			response.sendRedirect("/negociacion");
		} catch (IOException error) {
			// TODO Auto-generated catch block
			error.printStackTrace();
		}

	}

	private static class UltimatumTransfer {
		public String edades;
		public String sueldo;
		public String fechaInicio;
		public String fechaFin;
		public long idPropuesta;
		public long idCandidatura;
	}

	// Manejador para cuando se manda un ultimatum al otro usuario
	// Tiene que registrar la propuesta con los datos y añadir un mensaje para
	// enviarle el ultimatum.
	@PostMapping("/enviaUltimatum")
	@Transactional
	@ResponseBody
	public Evento.TransferChat enviaUltimatum2(HttpSession session, RedirectAttributes redirectAttributes, Model model,
			@RequestBody UltimatumTransfer ut) {

		String mensaje = ""; // Este mensaje que es???
		Candidatura candidatura = entityManager.find(Candidatura.class, ut.idCandidatura);
		Propuesta ultimatum = new Propuesta();
		Propuesta original = entityManager.find(Propuesta.class, ut.idPropuesta);
		LocalDateTime fechaIni = LocalDate.parse(ut.fechaInicio).atTime(LocalTime.now());
		LocalDateTime fechaFinal = LocalDate.parse(ut.fechaFin).atTime(LocalTime.now());
		if (fechaIni.isBefore(LocalDateTime.now())) {
			mensaje = "Error. Las fechas deben ser como mínimo las actuales"; // Comprobar fecha inicio
		} else {
			ultimatum.setActiva(true);
			ultimatum.setCandidaturas(new ArrayList<Candidatura>());
			ultimatum.setDescripcion(original.getDescripcion());
			ultimatum.setNombre(original.getNombre());
			ultimatum.setEdadMinPublico(Integer.valueOf(ut.edades.split("-")[0]));
			ultimatum.setEdadMaxPublico(Integer.valueOf(ut.edades.split("-")[1]));
			ultimatum.setSueldo(Integer.valueOf(ut.sueldo));
			ultimatum.setFechaSubida(LocalDateTime.now());
			ultimatum.setFechaInicio(fechaIni);
			ultimatum.setFechaFin(fechaFinal);
			ultimatum.setTags(original.getTags());
			ultimatum.setEmpresa(original.getEmpresa());
			ultimatum.setVerificado(false);
			ultimatum.setTipo(Tipo_propuesta.ULTIMATUM);
			entityManager.persist(ultimatum);
			insertaImagenUltimatum(ut.idPropuesta, ultimatum.getId());
			candidatura.setEstado(Estado.EN_ULTIMATUM.toString());
			candidatura.setPropuesta(ultimatum);
			entityManager.persist(candidatura);
			return creaEventosUltimatum(candidatura,
					entityManager.find(Usuario.class, ((Usuario) session.getAttribute("u")).getId()), ultimatum);
		}
		return null;
	}

	@Transactional
	private TransferChat creaEventosUltimatum(Candidatura candidatura, Usuario emisor, Propuesta ultimatum) {
		// Se crea el evento para el chat de la negociación
		Evento e = new Evento();
		e.setDescripcion("Se ha enviado un ultimátum");
		e.setCandidatura(candidatura);
		e.setEmisor(emisor);
		e.setFechaEnviado(LocalDateTime.now());
		e.setLeido(false);
		e.setTipo(Tipo.CHAT);
		Usuario receptor;
		if (emisor.hasRole(Rol.EMPRESA)) {
			receptor = candidatura.getCandidato();
		} else {
			receptor = ultimatum.getEmpresa();
		}
		e.setReceptor(receptor);
		entityManager.persist(e);
		Evento.TransferChat transfer = Evento.asTransferObject(e, emisor);
		messagingTemplate.convertAndSend("/user/" + e.getReceptor().getNombreCuenta() + "/queue/updates",
				Evento.asTransferObject(e, e.getReceptor()));
		log.info("Enviado mensaje via WS a {}", e.getReceptor().getNombreCuenta());

		// Se crea el evento de notificación para el usuario que recibe el ultimátum
		e = new Evento();
		e.setTipo(Tipo.NOTIFICACION);
		e.setEmisor(emisor);
		e.setReceptor(receptor);
		e.setFechaEnviado(LocalDateTime.now());
		e.setLeido(false);
		e.setDescripcion("El usuario " + emisor.getNombre() + " " + emisor.getApellidos()
				+ " te ha enviado un ultimátum relativo " + "a la negociación de la propuesta "
				+ candidatura.getPropuesta().getNombre());
		entityManager.persist(e);

		return transfer;
	}

	// Manejador para cuando se crea una propuesta
	// Tiene que registrar la propuesta en la BDD
	@PostMapping("/registraPropuesta")
	@Transactional
	public void registraPropuesta(HttpSession session, HttpServletResponse response,
			RedirectAttributes redirectAttributes, Model model, @RequestParam String nombre,
			@RequestParam String descripcion, @RequestParam String sueldo, @RequestParam String edades,
			@RequestParam String fechaInicio, @RequestParam String fechaFin,
			@RequestParam MultipartFile imagenPropuesta, @RequestParam String tags) {

		String mensaje = "";
		LocalDateTime fechaIni = LocalDate.parse(fechaInicio).atTime(LocalTime.now());
		LocalDateTime fechaFinal = LocalDate.parse(fechaFin).atTime(LocalTime.now());
		Propuesta p = new Propuesta();

		if (!((Usuario) session.getAttribute("u")).hasRole(Rol.EMPRESA)) {
			try {
				response.sendRedirect("/error");
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.info("Error al redireccionar");
			}
		}
		if (fechaIni.isBefore(LocalDateTime.now())) {
			mensaje = "Error. Las fechas deben ser como mínimo las actuales"; // Comprobar fecha inicio
		} else {
			p.setActiva(true);
			p.setCandidaturas(new ArrayList<Candidatura>());
			p.setDescripcion(descripcion);
			p.setNombre(nombre);
			p.setEdadMinPublico(Integer.valueOf(edades.split("-")[0]));
			p.setEdadMaxPublico(Integer.valueOf(edades.split("-")[1]));
			p.setSueldo(Integer.valueOf(sueldo));
			p.setFechaSubida(LocalDateTime.now());
			p.setFechaInicio(fechaIni);
			p.setFechaFin(fechaFinal);
			tags = tags.toUpperCase();
			p.setTags(tags);
			Usuario usuarioLoggeado = entityManager.find(Usuario.class, ((Usuario) session.getAttribute("u")).getId());
			p.setEmpresa(usuarioLoggeado);
			p.setVerificado(false);
			p.setTipo(Tipo_propuesta.PROPUESTA);
			entityManager.persist(p);
			entityManager.flush();
			insertaImagenPropuesta(imagenPropuesta, p.getId());
			mensaje = "Propuesta insertada correctamente";
		}

		if (mensaje.equals("Propuesta insertada correctamente")) {
			Evento e = new Evento();
			e.setEmisor(entityManager.find(Usuario.class, ((Usuario) session.getAttribute("u")).getId()));
			e.setDescripcion("Se ha registrado la propuesta " + p.getNombre());
			e.setFechaEnviado(LocalDateTime.now());
			e.setLeido(false);
			e.setTipo(Evento.Tipo.ADMINISTRACION);
			entityManager.persist(e);
			ObjectMapper mapper = new ObjectMapper();
			ObjectNode rootNode = mapper.createObjectNode();
			rootNode.put("text", "La propuesta " + p.getNombre() + " se ha registrado");
			String json = "";
			try {
				json = mapper.writeValueAsString(rootNode);
			} catch (JsonProcessingException error) {
				// TODO Auto-generated catch block
				error.printStackTrace();
			}
			messagingTemplate.convertAndSend("/topic/admin", json);
		}

		session.setAttribute("mensajeInfo", mensaje);
		try {
			response.sendRedirect("/inicio");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.info("Error al redireccionar");
		}
	}

	private void insertaImagenUltimatum(long idPropuesta, long idUltimatum) {
		File in = localData.getFile("propuesta", "" + idPropuesta);
		File out = localData.getFile("propuesta", String.valueOf(idUltimatum));
		if (in.exists()) {
			System.out.println("El fichero existía" + in.getAbsolutePath());
			try {
				FileCopyUtils.copy(in, out);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				log.warn("Error uploading " + String.valueOf(idPropuesta) + " ", e);
			}
		}
	}

	@GetMapping(value = "/{id}/photo")
	public StreamingResponseBody getPhotoPropuesta(@PathVariable long id, Model model) throws IOException {
		File f = localData.getFile("propuesta", "" + id);
		InputStream in;
		if (f.exists()) {
			in = new BufferedInputStream(new FileInputStream(f));
		} else {
			in = new BufferedInputStream(getClass().getClassLoader().getResourceAsStream("static/img/propuesta.png"));
		}
		return new StreamingResponseBody() {
			@Override
			public void writeTo(OutputStream os) throws IOException {
				FileCopyUtils.copy(in, os);
			}
		};
	}

	private void insertaImagenPropuesta(MultipartFile imagenPropuesta, long idPropuesta) {
		File f = localData.getFile("propuesta", String.valueOf(idPropuesta));
		if (!imagenPropuesta.isEmpty()) {
			// Subir imagen por defecto al perfil
			try (BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(f))) {
				byte[] bytes = imagenPropuesta.getBytes();
				stream.write(bytes);
			} catch (Exception e) {
				log.warn("Error uploading " + String.valueOf(idPropuesta) + " ", e);
			}
		}
	}

}
