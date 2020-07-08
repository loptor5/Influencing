package es.ucm.fdi.iw.control;

import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import es.ucm.fdi.iw.model.Evento;
import es.ucm.fdi.iw.model.Usuario;
import es.ucm.fdi.iw.model.Usuario.Rol;

@Controller()
@RequestMapping("notificaciones")
public class NotificacionController {

	@Autowired
	private EntityManager entityManager;

	@GetMapping("")
	public String notificaciones(HttpSession session, Model model) {
		List<Evento> listaNotificaciones = null;
		if (((Usuario) session.getAttribute("u")).hasRole(Rol.ADMIN)) {
			listaNotificaciones = entityManager.createNamedQuery("Evento.adminEventsByDate", Evento.class).getResultList();
		} else {
			listaNotificaciones = entityManager.createNamedQuery("Evento.getNotificacionesUnread", Evento.class)
					.setParameter("idUsuario", ((Usuario) session.getAttribute("u")).getId()).getResultList();

		}
		model.addAttribute("notificaciones", listaNotificaciones);
		return "modals/notificaciones";

	}

	private static class TransferNotificacion {
		public long idNotificacion;
	}
	
	@PostMapping("/elimina")
	@Transactional
	@ResponseBody
	public String eliminaNotificacion(HttpSession session, @RequestBody TransferNotificacion tn) {
		Evento notificacion = entityManager.find(Evento.class, tn.idNotificacion);
		if (((Usuario)session.getAttribute("u")).getId() == notificacion.getReceptor().getId()) {
			notificacion.setLeido(true);
			entityManager.persist(notificacion);
			return String.valueOf(entityManager.createNamedQuery("Evento.adminEventsByDate").getResultList().size()); 
		}
		else return "error";
	}

}
