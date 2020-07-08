package es.ucm.fdi.iw.control;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import es.ucm.fdi.iw.model.Propuesta;
import es.ucm.fdi.iw.model.Usuario;

import java.util.List;

import javax.persistence.EntityManager;
import javax.servlet.http.HttpSession;

@Controller()
@RequestMapping("busquedaPropuesta")
public class BusquedaPropuestaController {
    
    @Autowired 
    private EntityManager entityManager;
	private final int NUM_ELEMENTOS_PAGINA=4;

    
    @GetMapping("")
	public String getPropuestas(Model model, HttpSession session) {
		List<Propuesta> propuestas = entityManager.createNamedQuery("Propuesta.getAllProposals", Propuesta.class).getResultList();
		model.addAttribute("mensaje",session.getAttribute("mensajeInfo"));
	    session.removeAttribute("mensajeInfo");
		model.addAttribute("numeroPaginas", Math.ceil((double)propuestas.size()/NUM_ELEMENTOS_PAGINA));
		if (NUM_ELEMENTOS_PAGINA <= propuestas.size())
			propuestas=propuestas.subList(0,NUM_ELEMENTOS_PAGINA);
		
	    model.addAttribute("propuestas", propuestas);
	    model.addAttribute("numNotificaciones", entityManager.createNamedQuery("Evento.getNotificacionesUnread")
				  .setParameter("idUsuario", ((Usuario)session.getAttribute("u")).getId()).getResultList().size());

        return "busquedaPropuesta";
    }
    
    
	  @PostMapping("")
	  public String busquedaInicio(HttpSession session, Model model, @RequestParam String nombrePropuestaBusqueda, @RequestParam(required = true, defaultValue = "1") int indicePagina) {
		  String patronParaLike = "%"+nombrePropuestaBusqueda+"%";
			List<Propuesta> propuestas = null;
			propuestas = entityManager.createNamedQuery("Propuesta.searchByNombre", Propuesta.class)
					.setParameter("nombre", patronParaLike.toUpperCase()).getResultList();	
			model.addAttribute("numeroPaginas", Math.ceil((double)propuestas.size()/NUM_ELEMENTOS_PAGINA));
			if (indicePagina*NUM_ELEMENTOS_PAGINA <= propuestas.size())
				propuestas=propuestas.subList((indicePagina-1)*NUM_ELEMENTOS_PAGINA, indicePagina*NUM_ELEMENTOS_PAGINA);
			else 
				propuestas=propuestas.subList((indicePagina-1)*NUM_ELEMENTOS_PAGINA, propuestas.size());

		    model.addAttribute("propuestas", propuestas);
			model.addAttribute("numNotificaciones", entityManager.createNamedQuery("Evento.getNotificacionesUnread")
					  .setParameter("idUsuario", ((Usuario)session.getAttribute("u")).getId()).getResultList().size());
			model.addAttribute("patron", nombrePropuestaBusqueda);

			return "busquedaPropuesta";

	  
	  }
    
	  
	 private static class TransferBusqueda{
		 public String patron;
		 public int indice;
	 }
	  
    @PostMapping("/busca")
	public String postSearch(Model model, HttpSession session, @RequestBody TransferBusqueda tb) {
		String patronParaLike = "%"+tb.patron+"%";
		List<Propuesta> propuestas = null;
		propuestas = entityManager.createNamedQuery("Propuesta.searchByNombre", Propuesta.class)
				.setParameter("nombre", patronParaLike.toUpperCase()).getResultList();		
		model.addAttribute("numeroPaginas", Math.ceil((double)propuestas.size()/NUM_ELEMENTOS_PAGINA));
		if (tb.indice*NUM_ELEMENTOS_PAGINA <= propuestas.size())
			propuestas=propuestas.subList((tb.indice-1)*NUM_ELEMENTOS_PAGINA, tb.indice*NUM_ELEMENTOS_PAGINA);
		else 
			propuestas=propuestas.subList((tb.indice-1)*NUM_ELEMENTOS_PAGINA, propuestas.size());

		
		model.addAttribute("patron", tb.patron);
		model.addAttribute("resultadoBusqueda", propuestas);
		return "fragments/resultadoBusquedaPropuestas";
	}
    
    @GetMapping("/tags")
	public String postSearchByTag(Model model, HttpSession session, @RequestParam(required = true, defaultValue = "1") int indicePagina, @RequestParam String tag ) {
		String tagsLike;
		if (tag.equals(" ALL"))
			tagsLike = "%%";
		else
			tagsLike = "%"+tag+"%";
		List<Propuesta> propuestas = null;
		if (tag.isEmpty()) { 
			propuestas = entityManager.createQuery("select p from Propuesta p", Propuesta.class).getResultList();
		} else {
			propuestas = entityManager.createNamedQuery("Propuesta.searchByTag", Propuesta.class)
					.setParameter("tag", tagsLike.toUpperCase()).getResultList();			
		}
		model.addAttribute("numeroPaginas", Math.ceil((double)propuestas.size()/NUM_ELEMENTOS_PAGINA));
		if (indicePagina*NUM_ELEMENTOS_PAGINA <= propuestas.size())
			propuestas=propuestas.subList((indicePagina-1)*NUM_ELEMENTOS_PAGINA, indicePagina*NUM_ELEMENTOS_PAGINA);
		else 
			propuestas=propuestas.subList((indicePagina-1)*NUM_ELEMENTOS_PAGINA, propuestas.size());

				
		model.addAttribute("resultadoBusqueda", propuestas);
		model.addAttribute("patron", tag);
		return "fragments/resultadoBusquedaPropuestas";
	}
  
    @GetMapping("/propuesta")
    public String propuesta(Model model, HttpSession session, @RequestParam long idPropuesta) {
        Propuesta p = entityManager.find(Propuesta.class, idPropuesta);
        model.addAttribute("propuesta", p);
        return "modals/propuesta";
    } // copiado

}
