document.addEventListener("DOMContentLoaded", () => {
	for (let p of document.getElementsByClassName("propuesta")) {
		p.onclick = c => {
			setConfigs(p);
			cargaChat(p.dataset.id, p.dataset.propId, p.dataset.otroNombre, p.dataset.propioId, p.dataset.otroId, p.dataset.propNombre, document.getElementById("contenidoChat"));
		}
	}
	asignaListenerBarraEnvio();
	ws.receive = json => {
		if (pertenecePropuestaSeleccionada(json.nombrePropuesta, document.getElementsByClassName("nombre")[0].innerHTML)) {
			insertaMensaje(json);
		}
	}
	lanzaAlert();
});

function pertenecePropuestaSeleccionada(propuestaMensajes, propuestaSeleccionada) {
	return propuestaMensajes === propuestaSeleccionada;
}

function setConfigs(propuesta) {
	config.propId = propuesta.dataset.propId;
	config.receptorId = propuesta.dataset.otroId;
	config.emisorId = propuesta.dataset.propioId;
	config.candidaturaId = propuesta.dataset.id;
	config.estadoCandidatura = propuesta.dataset.estadoCandidatura;
}

function asignaListenerBarraEnvio() {
	document.getElementById("botonEnviar").addEventListener("keyup", function(event) {
		if (event.keyCode === 13) {
			// Cancel the default action, if needed
			event.preventDefault();
			// Trigger the button element with a click
			//Comprueba que el mensaje no esté vacío ni que solo contenga espacios en blanco
			if (this.value.length !== 0 && this.value.trim())
				enviarMensajeChatNegociacion(this.value, config.candidaturaId, config.receptorId, config.emisorId);
			this.value = "";

		}
	});
}


function parseaFecha(fecha) {
	let array = fecha.split("T");
	let dias = array[0].split("-");
	let minutos = array[1].split(":");
	return dias[2] + "/" + dias[1] + "/" + dias[0] + "  " + minutos[0] + ":" + minutos[1];
}

function insertaEnDiv(json, contenido) {
	let html = []

	json.forEach(msg => {
		let clase = msg.propio ? 'mensaje enviado' : 'mensaje recibido';
		html.push("<p class='" + clase + " msg'> " + parseaFecha(msg.sent) + " - " + msg.text + "</p>");
	})

	contenido.innerHTML = html.join("\n");
	document.getElementById("eliminaNegociacion").style.display="block";
	document.getElementById("eliminaNegociacion").value = config.candidaturaId;

	if (config.estadoCandidatura == "EN_ULTIMATUM" || (json.length > 0 && json[0].ultimatum)) {
		document.getElementById("botonUltimatum").innerText = "Visualizar Ultimatum";
		document.getElementById("botonUltimatum").onclick = b => cargaUltimatumVisualizacionModal(config.candidaturaId);
		document.getElementById("botonUltimatum").classList.remove("w3-black");
		document.getElementById("botonUltimatum").classList.add("w3-aqua");
		document.getElementById("botonEnviar").style.display = "none";
	}
	else {
		document.getElementById("botonUltimatum").innerText = "Enviar Ultimatum";
		document.getElementById("botonUltimatum").onclick = b => cargaUltimatumModal(config.candidaturaId);
		document.getElementById("botonEnviar").style.display = "block";
		document.getElementById("botonUltimatum").classList.remove("w3-aqua");
		document.getElementById("botonUltimatum").classList.add("w3-black");
	}
}



function cargaUltimatumModal(idCandidatura) {
	document.getElementById('modal').style.display = 'block';
	return go2(config.rootUrl + "propuesta/ultimatum?idCandidatura=" + idCandidatura, 'GET')
		.then(html => {
			document.getElementById("contenidoModal").innerHTML = html;
			document.getElementById("btnEnviarUltimatum").onclick = b => enviaUltimatum();
		});
}

function cargaUltimatumVisualizacionModal(idCandidatura) {
	document.getElementById('modal').style.display = 'block';
	return go2(config.rootUrl + "propuesta/vistaUltimatum?idCandidatura=" + idCandidatura, 'GET')
		.then(html => {
			document.getElementById("contenidoModal").innerHTML = html;
			document.getElementById("btnEnviarUltimatum").onclick = b => enviaUltimatum();
		});
}

function enviaUltimatum() {
	let data = {
		edades: document.getElementsByName("edades")[0].value,
		sueldo: document.getElementsByName("sueldo")[0].value,
		fechaInicio: document.getElementsByName("fechaInicio")[0].value,
		fechaFin: document.getElementsByName("fechaFin")[0].value,
		idPropuesta: document.getElementsByName("idPropuesta")[0].value,
		idCandidatura: document.getElementsByName("idCandidatura")[0].value
	};

	return go(config.rootUrl + "propuesta/enviaUltimatum", 'POST', data)
		.then(json => {
			insertaMensaje(json);
			document.getElementById('modal').style.display = 'none';
			config.estadoCandidatura = "EN_ULTIMATUM";
		});


}

function insertaMensaje(json) {
	let clase = json.propio ? 'mensaje enviado' : 'mensaje recibido';
	document.getElementById("contenidoChat").innerHTML += "\n <p class='" + clase + " msg'> " + parseaFecha(json.sent) + " - " + json.text + "</p>";
	if (json.ultimatum) {
		document.getElementById("botonUltimatum").innerText = "Visualizar Ultimatum";
		document.getElementById("botonUltimatum").onclick = b => cargaUltimatumVisualizacionModal(config.candidaturaId);
		document.getElementById("botonUltimatum").classList.remove("w3-black");
		document.getElementById("botonUltimatum").classList.add("w3-aqua");
		document.getElementById("botonEnviar").style.display = "none";
	}
	else {
		document.getElementById("botonUltimatum").innerText = "Enviar Ultimatum";
		document.getElementById("botonUltimatum").onclick = b => cargaUltimatumModal(config.candidaturaId);
		document.getElementById("botonEnviar").style.display = "block";
		document.getElementById("botonUltimatum").classList.remove("w3-aqua");
		document.getElementById("botonUltimatum").classList.add("w3-black");
	}

}

function cargaPerfilModal(idUsuario) {
	document.getElementById('modal').style.display   = 'block';
	return go2(config.rootUrl + "perfil?idUsuario=" + idUsuario, 'GET')
		.then(html => {
			document.getElementById("contenidoModal").innerHTML   = html;
			document.getElementById("btnDenunciar").onclick = b > cargaModalDenuncia(document.getElementById("btnDenunciar").dataset.id);
		})
}


function cargaPropuestaModal(idPropuesta){ 
	document.getElementById('modal').style.display = 'block';
	return go2(config.rootUrl + "propuesta?idPropuesta=" + idPropuesta, 'GET')
		.then(html => {
			document.getElementById("contenidoModal").innerHTML = html;
			document.getElementById("btnDenunciar").onclick = b => cargaModalDenuncia(document.getElementById("btnDenunciar").dataset.id);
		})
}

function cargaChat(idCandidatura, idPropuesta, nombreUsuario, idEmisor, idReceptor, nombrePropuesta, contenido) {
	document.getElementsByClassName("perfil")[0].innerHTML = nombreUsuario;
	document.getElementsByClassName("perfil")[0].onclick = b => cargaPerfilModal(idReceptor);
	document.getElementsByClassName("nombre")[0].innerHTML = nombrePropuesta;
	document.getElementsByClassName("nombre")[0].onclick = b => cargaPropuestaModal(idPropuesta);
	document.getElementById("botonUltimatum").style.display = "block";
	document.getElementById("botonEnviar").style.display = "block";

	return go(config.rootUrl + "message/getChat?idCandidatura=" + idCandidatura + "&idUsuario=" + idEmisor, 'GET')
		.then(json => insertaEnDiv(json, contenido));
}


function enviarMensajeChatNegociacion(mensaje, idCandidatura, idReceptor, idEmisor) {
	let data = {
		idCandidatura: idCandidatura,
		mensaje: mensaje,
		idEmisor: idEmisor,
		idReceptor: idReceptor
	};

	
	return go(config.rootUrl + "message/insertaMsg", 'POST', data)
		.then(json => insertaMensaje(json));

}


