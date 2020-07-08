document.addEventListener("DOMContentLoaded", () => {
	document.getElementById("botonNotificacionesNav").onclick = b => cargaModalNotificacionesNav();
	document.getElementById("botonPerfilNav").onclick = b => cargaModalPerfilNav();
	if (document.getElementById("btnNumeroNotificaciones").innerText == "0"){
		document.getElementById("indiceNumNotificaciones").style.display = "none";
	}

	// Boton notificaciones INICIO	
	let botonNotificacionesIni = document.getElementById("botonNotificacionesIni");
	if (botonNotificacionesIni !== null) {
		botonNotificacionesIni.onclick = b => cargaModalNotificacionesNav();
	}
});

// Boton guardar cambios perfil (EDICION)
function asignaFuncionBotonGuardarCambios() {
	document.getElementById("botonGuardarCambios").onclick = b => {
		validaPassword(document.getElementsByName("pass1")[0], document.getElementsByName("pass2")[0]);
		validaTexto(document.getElementsByName("nombre")[0]);
		validaTexto(document.getElementsByName("apellidos")[0]);
		validaDatosPerfil("Twitter");
		validaDatosPerfil("Facebook");
		validaDatosPerfil("Instagram");
		validaDatosPerfil("Youtube");
		validaTexto(document.getElementsByName("tags")[0]);
		validaTags(document.getElementsByName("tags")[0]);
	}
}

// Boton enviar denuncia
function asignaFuncionBotonEnviarDenuncia() {
	document.getElementById("botonGuardarCambios").onclick = b => {
		validaTexto(document.getElementsByName("titulo")[0]);
		validaTexto(document.getElementsByName("descripcion")[0]);
	}
}

// Validacion
function validaPassword(pass1, pass2){
	pass1.setCustomValidity(pass1.value === pass2.value ?"" : "Las contraseñas no coinciden")
	pass2.setCustomValidity(pass1.value === pass2.value ?"" : "Las contraseñas no coinciden")
}

function validaTexto(campo){
	campo.setCustomValidity(campo.value === campo.value.trim()?"":"No se pueden introducir espacios al comienzo ni al final");
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

function configuraCierreNotificaciones(){
	 for (let p of document.getElementsByClassName("eliminaNotificacion")){
        p.onclick =  c => { 
			marcaNotificacionLeida(p.dataset.id);
		}
    }
}

function marcaNotificacionLeida(idNotificacion){
		let data = {
		idNotificacion: idNotificacion
	};
	
	return go2(config.rootUrl + "notificaciones/elimina", 'POST', data)
	.then(numNotificaciones => {
		if (numNotificaciones > 0){
			numNotificaciones = numNotificaciones;
			document.getElementById("btnNumeroNotificaciones").innerText = numNotificaciones;
		}
		else{
			document.getElementById("indiceNumNotificaciones").style.display = "none";
		}
		cargaModalNotificacionesNav();
	});
}

function parseaEstrellasValoracion(inputPuntuacion){
	pintaEstrellasValoracion(inputPuntuacion.target.value);
}

function pintaEstrellasValoracion(puntuacion) {
	let parteEntera = puntuacion[0];
	let parteDecimal = puntuacion[2];

	tablinks = document.getElementsByClassName("estrella");
	for (i = 0; i < parteEntera; i++) {
		tablinks[i].className = "estrella fa fa-star";
	}
	if (parteDecimal >= 5) {
		tablinks[parteEntera].className = "estrella fa fa-star-half-alt";
		for (i = parseInt(parteEntera) + 1; i < 5; i++) {
			tablinks[i].className = "estrella far fa-star";
		}
	}
	else {
		for (i = parteEntera; i < 5; i++) {
			tablinks[i].className = "estrella far fa-star";
		}
	}
}


// Desplegable RRSS
function abreEmpresa(evt, nombreEmpresa) {
	let i, x, tablinks;
	x = document.getElementsByClassName("empresa");
	for (i = 0; i < x.length; i++) {
		x[i].style.display = "none";
	}
	tablinks = document.getElementsByClassName("tablink");
	for (i = 0; i < x.length; i++) {
		tablinks[i].className = tablinks[i].className.replace(" w3-border-red", "");
	}
	document.getElementById(nombreEmpresa).style.display = "block";
	evt.currentTarget.firstElementChild.className += " w3-border-red";
}

// Lanza Alert
function lanzaAlert() {
	if (document.getElementById("mensajeInfo").textContent !== ""){
		document.getElementById("contenidoModal").innerHTML= "<h2 class='divCentrado w3-padding-64'>" + document.getElementById("mensajeInfo").textContent + "</h2>";
		document.getElementById('modal').style.display='block';
		document.getElementById('modal').onclick = c=> { document.getElementById('modal').style.display = "none"};
	}
}


// Carga Modal
function cargaModalPerfilNav() {
	document.getElementById('modal').style.display='block';
	return go2(config.rootUrl + "perfil/edicion", 'GET')
		.then(html => {
			document.getElementById("contenidoModal").innerHTML=html;
			asignaFuncionBotonGuardarCambios();
		});
}

function cargaModalNotificacionesNav(){
	document.getElementById('modal').style.display='block';
	return go2(config.rootUrl + "notificaciones", 'GET')
		.then(html => {
			document.getElementById("contenidoModal").innerHTML=html;
			configuraCierreNotificaciones();
		});
}

function cargaModalPerfil(idPerfil){
	document.getElementById('modal').style.display='block';
	return go2(config.rootUrl + "perfil?idUsuario=" + idPerfil, 'GET')
		.then(html => {
			document.getElementById("contenidoModal").innerHTML=html;

			let botonDenuncia = document.getElementById("btnDenunciar");
			let botonEditar = document.getElementById("btnEditarPerfil");
			
			if(botonDenuncia !== null)
				botonDenuncia.onclick = b => cargaModalDenuncia(document.getElementById("btnDenunciar").dataset.id);
			else if(botonEditar !== null)
				botonEditar.onclick = b => cargaModalPerfilNav();

			for (let p of document.getElementsByClassName("propuestaAntigua")){
				p.onclick = c => cargaModalPropuesta(p.dataset.id);
			}
		})
}

function cargaModalPropuesta(idPropuesta){
	document.getElementById('modal').style.display='block';
	return go2(config.rootUrl + "propuesta?idPropuesta=" + idPropuesta, 'GET')
	.then(html => {
		document.getElementById("contenidoModal").innerHTML=html;

		let botonDenuncia = document.getElementById("btnDenunciar");
		let botonEditarPropuesta = document.getElementById("btnEditarPropuestaPropia");

		if (botonDenuncia !== null) 
			botonDenuncia.onclick = b => cargaModalDenuncia(botonDenuncia.dataset.id);
		else
			botonEditarPropuesta.onclick = b => cargaModalEdicionPropuesta(botonEditarPropuesta.dataset.id);

	})
}
function cargaModalEdicionPropuesta(idPropuesta){
	return go2(config.rootUrl + "propuesta/modifica?idPropuesta=" + idPropuesta, 'GET')
		.then(html => {
			document.getElementById("contenidoModal").innerHTML=html
		});
}


function cargaModalDenuncia(idDenunciado){
	let url = window.location.href.toString().split(window.location.host)[1];
	document.getElementById('modal').style.display='block';
	return go2(config.rootUrl + "denuncia?idDenunciado=" + idDenunciado+"&ruta="+url, 'GET')
		.then(html => {
			document.getElementById("contenidoModal").innerHTML=html
			asignaFuncionBotonEnviarDenuncia();
		});
}