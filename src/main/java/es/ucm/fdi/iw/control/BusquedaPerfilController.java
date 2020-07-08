package es.ucm.fdi.iw.control;

import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.ucm.fdi.iw.model.Usuario;

@Controller()
@RequestMapping("busquedaPerfil")
public class BusquedaPerfilController {

	@Autowired
	private EntityManager entityManager;
	private final int NUM_ELEMENTOS_PAGINA = 4;

	// Metodos nuevos
	@GetMapping("")
	public String busquedaPerfil(HttpSession session, Model model) {
		model.addAttribute("mensaje", session.getAttribute("mensajeInfo"));
		session.removeAttribute("mensajeInfo");
		List<Usuario> usuarios = entityManager.createNamedQuery("Usuario.getAllUsers", Usuario.class).getResultList();
		model.addAttribute("numeroPaginas", Math.ceil((double) usuarios.size() / NUM_ELEMENTOS_PAGINA));
		if (NUM_ELEMENTOS_PAGINA <= usuarios.size())
			usuarios = usuarios.subList(0, NUM_ELEMENTOS_PAGINA);
		model.addAttribute("usuarios", usuarios);
		model.addAttribute("numNotificaciones", entityManager.createNamedQuery("Evento.getNotificacionesUnread")
				.setParameter("idUsuario", ((Usuario) session.getAttribute("u")).getId()).getResultList().size());

		return "busquedaPerfil";
	}

	@PostMapping("")
	public String busquedaInicio(HttpSession session, Model model, @RequestParam String nombrePerfilBusqueda) {
		String patronParaLike = "%" + nombrePerfilBusqueda + "%";
		List<Usuario> usuarios = null;
		usuarios = entityManager.createNamedQuery("Usuario.searchByNombre", Usuario.class)
				.setParameter("nombre", patronParaLike.toUpperCase()).getResultList();
		model.addAttribute("numeroPaginas", Math.ceil((double) usuarios.size() / NUM_ELEMENTOS_PAGINA));
		if (NUM_ELEMENTOS_PAGINA <= usuarios.size())
			usuarios = usuarios.subList(0, NUM_ELEMENTOS_PAGINA);
		model.addAttribute("usuarios", usuarios);
		model.addAttribute("numNotificaciones", entityManager.createNamedQuery("Evento.getNotificacionesUnread")
				.setParameter("idUsuario", ((Usuario) session.getAttribute("u")).getId()).getResultList().size());
		return "busquedaPerfil";

	}

	
	 private static class TransferBusqueda{
		 public String patron;
		 public int indice;
	 }
	 
	 
	@PostMapping("/busca")
	public String postSearch(Model model, HttpSession session, @RequestBody TransferBusqueda tb) {
		String patronParaLike = "%" + tb.patron + "%";
		List<Usuario> usuarios = null;
		usuarios = entityManager.createNamedQuery("Usuario.searchByNombre", Usuario.class)
				.setParameter("nombre", patronParaLike.toUpperCase()).getResultList();
		model.addAttribute("numeroPaginas", Math.ceil((double) usuarios.size() / NUM_ELEMENTOS_PAGINA));
		if (tb.indice * NUM_ELEMENTOS_PAGINA <= usuarios.size())
			usuarios = usuarios.subList((tb.indice - 1) * NUM_ELEMENTOS_PAGINA, tb.indice * NUM_ELEMENTOS_PAGINA);
		else
			usuarios = usuarios.subList((tb.indice - 1) * NUM_ELEMENTOS_PAGINA, usuarios.size());

		model.addAttribute("resultadoBusqueda", usuarios);
		model.addAttribute("patron", tb.patron);

		return "fragments/resultadoBusquedaPerfiles";
	}

	@GetMapping("/tags")
	public String postSearchByTag(Model model, HttpSession session,
			@RequestParam(required = true, defaultValue = "1") int indicePagina, @RequestParam String tag) {
		String tagsLike;
		if (tag.equals(" ALL"))
			tagsLike = "%%";
		else
			tagsLike = "%" + tag + "%";
		List<Usuario> usuarios = null;
		if (tag.isEmpty()) {
			usuarios = entityManager.createQuery("select u from Usuario u", Usuario.class).getResultList();
		} else {
			usuarios = entityManager.createNamedQuery("Usuario.searchByTag", Usuario.class)
					.setParameter("tag", tagsLike.toUpperCase()).getResultList();
		}
		model.addAttribute("numeroPaginas", Math.ceil((double) usuarios.size() / NUM_ELEMENTOS_PAGINA));
		if (indicePagina * NUM_ELEMENTOS_PAGINA <= usuarios.size())
			usuarios = usuarios.subList((indicePagina - 1) * NUM_ELEMENTOS_PAGINA, indicePagina * NUM_ELEMENTOS_PAGINA);
		else
			usuarios = usuarios.subList((indicePagina - 1) * NUM_ELEMENTOS_PAGINA, usuarios.size());

		model.addAttribute("resultadoBusqueda", usuarios);
		model.addAttribute("patron", tag);
		return "fragments/resultadoBusquedaPerfiles";
	}

}
