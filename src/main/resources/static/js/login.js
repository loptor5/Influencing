document.addEventListener("DOMContentLoaded", () => {
	document.getElementById("registroLogin").onclick = c => cargaModalPerfilRegistro();
	lanzaAlert();
})


function cargaModalPerfilRegistro(){
	document.getElementById('modal').style.display='block';
	return go2(config.rootUrl + "perfil/creacion", 'GET')
		.then(html => {
			document.getElementById("contenidoModal").innerHTML=html;
			asignaFuncionBotonRegistro();
			document.getElementById("tipoCuenta").addEventListener('change', compruebaPerfiles);
		});		
}


function compruebaPerfiles(){
		if (this.value === 'Empresa'){
			document.getElementById("divPerfilesCreacion").style.display = "none";
			document.getElementById("divNombreEmpresa").style.display = "block";
			document.getElementById("divNombreInfluencer").style.display = "none";
			document.getElementById("nombreInfluencer").required = false;
			document.getElementById("nombreEmpresa").required = true;
			document.getElementsByName("apellidos")[0].required = false;

		}
		else{
			document.getElementById("divPerfilesCreacion").style.display = "block";
			document.getElementById("divNombreInfluencer").style.display = "block";
			document.getElementById("divNombreEmpresa").style.display = "none";
			document.getElementById("nombreInfluencer").required = true;
			document.getElementById("nombreEmpresa").required = false;
			document.getElementsByName("apellidos")[0].required = true;
		}
		
}

function asignaFuncionBotonRegistro(){
	let b = document.getElementById("botonRegistrarseUsuario");
			b.onclick = c => {
				if (document.getElementsByName("tipoCuenta")[0].value == 'Influencer') {
					var element = document.getElementById("nombreEmpresa");
					    element.parentNode.removeChild(element);
				}
				else{
					var element = document.getElementById("nombreInfluencer");
					    element.parentNode.removeChild(element);

				}
				validaPassword(document.getElementsByName("pass1")[0], document.getElementsByName("pass2")[0]);
				validaTexto(document.getElementsByName("nombre")[0]);
				validaTexto(document.getElementsByName("apellidos")[0]);
				validaTexto(document.getElementsByName("nombreCuenta")[0]);
				validaDatosPerfil("Twitter");
				validaDatosPerfil("Facebook");
				validaDatosPerfil("Instagram");
				validaDatosPerfil("Youtube");
				validaTexto(document.getElementsByName("tags")[0]);
				validaTags(document.getElementsByName("tags")[0]);
				if (document.getElementsByName("tipoCuenta")[0].value == 'Influencer')
					document.getElementsByName("edad")[0].required=true;
			}
	}

function validaDatosPerfil(perfil){
	let nombre = document.getElementsByName("nombre"+perfil)[0];
	let seguidores = document.getElementsByName("seguidores"+perfil)[0];
	if (nombre.value !== ""  || seguidores.value !== ""){
		seguidores.required = true;
		nombre.required = true;
	}
	
}

function validaTags(tags){
	let valores = tags.value;
	let valido = true;
	let largo = false;
	for (p of valores.split(",")){
		if (p !== p.trim())
			valido = false;
		else if(p.length > 30){
			largo = true;
		}
		
	}
	tags.setCustomValidity(!valido?"No se pueden introducir espacios al principio ni al final del tag":
						  (!largo)?"":"La longitud máxima de un tag es de 30 caracteres");
}

function validaTexto(campo){
	campo.setCustomValidity(campo.value === campo.value.trim()?"":"No se pueden introducir espacios al comienzo ni al final");
}

function validaPassword(pass1, pass2){
	pass1.setCustomValidity(pass1.value === pass2.value ?"" : "Las contraseñas no coinciden")
	pass2.setCustomValidity(pass1.value === pass2.value ?"" : "Las contraseñas no coinciden")
}



