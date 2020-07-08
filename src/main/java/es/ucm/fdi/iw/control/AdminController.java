package es.ucm.fdi.iw.control;

import java.io.File;
import java.time.LocalDateTime;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;
import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import es.ucm.fdi.iw.LocalData;
import es.ucm.fdi.iw.model.Candidatura;
import es.ucm.fdi.iw.model.Denuncia;
import es.ucm.fdi.iw.model.Evento;
import es.ucm.fdi.iw.model.Evento.Tipo;
import es.ucm.fdi.iw.model.Usuario.Rol;
import es.ucm.fdi.iw.model.Propuesta;
import es.ucm.fdi.iw.model.Usuario;

/**
 * Admin-only controller
 * 
 * @author mfreire
 */
@Controller()
@RequestMapping("admin")
public class AdminController {

	@Autowired
	private EntityManager entityManager;

	@Autowired
	private LocalData localData;

	@Autowired
	private Environment env;

	@GetMapping("/")
	public String index(HttpSession session, Model model) {
		if (!((Usuario) session.getAttribute("u")).hasRole(Rol.ADMIN)) 
			return "error";
		
		model.addAttribute("activeProfiles", env.getActiveProfiles());
		model.addAttribute("basePath", env.getProperty("es.ucm.fdi.base-path"));

		model.addAttribute("Usuarios", entityManager.createQuery("SELECT u FROM Usuario u").getResultList());

		return "admin";
	}

	@PostMapping("/toggleUsuario")
	@Transactional
	public String delUsuario(Model model, HttpSession session, @RequestParam long id) {
		Usuario target = entityManager.find(Usuario.class, id);
		if (((Usuario) session.getAttribute("u")).hasRole(Rol.ADMIN)) {
			if (target.getActivo() == true) {
				// disable
				File f = localData.getFile("Usuario", "" + id);
				if (f.exists()) {
					f.delete();
				}
				// disable Usuario
				target.setActivo(false);
			} else {
				// enable Usuario
				target.setActivo(true);
			}
			return index(session, model);
		}
		return "error";
	}

	@GetMapping("/influencers")
	public String searchInfluencers(Model model) {
		model.addAttribute("modo", "INFLUENCER");
		model.addAttribute("resultado",
				entityManager.createNamedQuery("Usuario.getAllInfluencersForAdmin", Usuario.class).getResultList());
		return "fragments/tablaAdmin";
	}

	@GetMapping("/empresas")
	public String searchEmpresas(Model model) {
		model.addAttribute("modo", "EMPRESA");
		model.addAttribute("resultado",
				entityManager.createNamedQuery("Usuario.getAllEmpresasForAdmin", Usuario.class).getResultList());
		return "fragments/tablaAdmin";
	}

	@GetMapping("/denuncias")
	public String searchDenuncias(Model model) {
		model.addAttribute("modo", "DENUNCIA");
		model.addAttribute("resultado",
				entityManager.createNamedQuery("Denuncia.getAllDenuncias", Denuncia.class).getResultList());
		return "fragments/tablaAdmin";
	}

	@GetMapping("/propuestas")
	public String searchPropuestas(Model model) {
		model.addAttribute("modo", "PROPUESTA");
		model.addAttribute("resultado",
				entityManager.createNamedQuery("Propuesta.getAllProposals", Propuesta.class).getResultList());
		return "fragments/tablaAdmin";
	}

	@GetMapping("/eliminaInfluencer")
	@Transactional
	public String eliminaInfluencer(Model model, HttpSession session, @RequestParam long id) {
		if (((Usuario) session.getAttribute("u")).hasRole(Rol.ADMIN)) {
			Usuario u = entityManager.find(Usuario.class, id);
			u.setActivo(false);
			entityManager.persist(u);
			return searchInfluencers(model);
		}

		return "error";
	}

	@GetMapping("/eliminaEmpresa")
	@Transactional
	public String eliminaEmpresa(Model model, HttpSession session, @RequestParam long id) {
		if (((Usuario) session.getAttribute("u")).hasRole(Rol.ADMIN)) {

			Usuario u = entityManager.find(Usuario.class, id);
			u.setActivo(false);
			entityManager.persist(u);
			return searchEmpresas(model);
		}
		return "error";
	}
	
	@GetMapping("/reactivaInfluencer")
	@Transactional
	public String reactivaInfluencer(Model model, HttpSession session, @RequestParam long id) {
		if (((Usuario) session.getAttribute("u")).hasRole(Rol.ADMIN)) {
			Usuario u = entityManager.find(Usuario.class, id);
			u.setActivo(true);
			entityManager.persist(u);
			return searchInfluencers(model);
		}

		return "error";
	}

	@GetMapping("/reactivaEmpresa")
	@Transactional
	public String reactivaEmpresa(Model model, HttpSession session, @RequestParam long id) {
		if (((Usuario) session.getAttribute("u")).hasRole(Rol.ADMIN)) {
			Usuario u = entityManager.find(Usuario.class, id);
			u.setActivo(true);
			entityManager.persist(u);
			return searchEmpresas(model);
		}
		return "error";
	}
	

	@GetMapping("/eliminaPropuesta")
	@Transactional
	public String eliminaPropuesta(Model model, HttpSession session, @RequestParam long id) {
		Usuario admin = entityManager.find(Usuario.class, ((Usuario) session.getAttribute("u")).getId());
		if (admin.hasRole(Rol.ADMIN)) {
			Propuesta p = entityManager.find(Propuesta.class, id);
			p.setActiva(false);
			entityManager.persist(p);
			insertaNotificacion("Se ha eliminado la propuesta" + p.getNombre(), p.getEmpresa(),
					entityManager.find(Usuario.class, ((Usuario) session.getAttribute("u")).getId()));
			for (Candidatura c : p.getCandidaturas()) {
				insertaNotificacion("Se ha eliminado la propuesta" + p.getNombre(), c.getCandidato(), admin);
			}

			return searchPropuestas(model);
		}

		return "error";
	}

	@GetMapping("/verificaInfluencer")
	@Transactional
	public String verificaInfluencer(Model model, HttpSession session, @RequestParam long id) {
		Usuario admin = entityManager.find(Usuario.class, ((Usuario) session.getAttribute("u")).getId());
		if (admin.hasRole(Rol.ADMIN)) {
			Usuario u = entityManager.find(Usuario.class, id);
			u.setVerificado(true);
			entityManager.persist(u);
			insertaNotificacion("Se ha verificado tu perfil", u, admin);
			return searchInfluencers(model);
		}
		return "error";
	}

	@GetMapping("/verificaEmpresa")
	@Transactional
	public String verificaEmpresa(Model model, HttpSession session, @RequestParam long id) {
		Usuario admin = entityManager.find(Usuario.class, ((Usuario) session.getAttribute("u")).getId());
		if (admin.hasRole(Rol.ADMIN)) {
			Usuario u = entityManager.find(Usuario.class, id);
			u.setVerificado(true);
			entityManager.persist(u);
			insertaNotificacion("Se ha verificado tu perfil", u, admin);

			return searchEmpresas(model);
		}

		return "error";
	}

	@GetMapping("/verificaPropuesta")
	@Transactional
	public String verificaPropuesta(Model model, HttpSession session, @RequestParam long id) {
		Usuario admin = entityManager.find(Usuario.class, ((Usuario) session.getAttribute("u")).getId());
		if (admin.hasRole(Rol.ADMIN)) {

			Propuesta p = entityManager.find(Propuesta.class, id);
			p.setVerificado(true);
			entityManager.persist(p);
			insertaNotificacion("Se ha verificado la propuesta " + p.getNombre(), p.getEmpresa(), admin);
			return searchPropuestas(model);
		}
		return "error";
	}

	@Transactional
	public void insertaNotificacion(String mensaje, Usuario receptor, Usuario emisor) {
		Evento e = new Evento();
		e.setTipo(Tipo.NOTIFICACION);
		e.setEmisor(emisor);
		e.setReceptor(receptor);
		e.setDescripcion(mensaje);
		e.setFechaEnviado(LocalDateTime.now());
		e.setLeido(false);
		entityManager.persist(e);
	}
}
