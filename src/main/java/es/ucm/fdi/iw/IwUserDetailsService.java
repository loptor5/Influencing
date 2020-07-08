package es.ucm.fdi.iw;

import java.util.ArrayList;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import es.ucm.fdi.iw.model.Usuario;

public class IwUserDetailsService implements UserDetailsService {

	private static Logger log = LogManager.getLogger(IwUserDetailsService.class);

    private EntityManager entityManager;
    
    @PersistenceContext
    public void setEntityManager(EntityManager em){
    	this.entityManager = em;
    }

    public UserDetails loadUserByUsername(String nombre){
    	try {
    		Usuario u = entityManager.createNamedQuery("Usuario.byNombreCuenta", Usuario.class)
                    .setParameter("nombreCuenta", nombre)
                    .getSingleResult();
	        // build UserDetails object
	        ArrayList<SimpleGrantedAuthority> roles = new ArrayList<>();
	        for (String r : u.getRoles().split("[,]")) {
	        	roles.add(new SimpleGrantedAuthority("ROLE_" + r));
		        log.info("Roles for " + nombre + " include " + roles.get(roles.size()-1));
	        }
	        return new org.springframework.security.core.userdetails.User(
	        		u.getNombreCuenta(), u.getPassword(), roles); 
	    } catch (Exception e) {
    		log.info("No such user: " + nombre + "(e = " + e.getMessage() + ")");
    		throw new UsernameNotFoundException(nombre);
    	}
    }
}