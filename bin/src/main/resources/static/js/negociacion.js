// function update(a) {
// 	let c = document.getElementById("c");
// 	a.innerHTML = "<i>hola mundo </i>";
// 	a.style = "color: " + c.value + ";"
// }


function insertaEnDiv(json, contenido, usuario, nombrePropuesta, idPropuesta){
	let html = []
	
	json.forEach(msg => {
		let clase = msg.propio ? 'mensaje enviado' : 'mensaje recibido';
		html.push("<p class='" + clase + "'> " + msg.text + "</p>");
	})
	
	contenido.innerHTML = html.join("\n");
	usuario.innerHTML = json[0].nombreUsuario;
	nombrePropuesta.innerHTML = json[0].nombrePropuesta;	

	document.getElementById("botonUltimatum").onclick = b => cargaModal(idPropuesta);
}

function cargaModal(idPropuesta){

	return go(config.rootUrl + "propuesta/" + idPropuesta, 'GET')
		.then(json => console.log(json));
}

function cargaChat(idCandidatura) {
	let usuario = document.getElementsByClassName("perfil")[0];
	let nombrePropuesta = document.getElementsByClassName("nombre")[0];
	
	let contenido = document.getElementById("contenidoChat");
	
	let idPropuesta = 1; // <--- mejorar

	return go(config.rootUrl + "message/getChat?idCandidatura=" + idCandidatura, 'GET')
		.then(json => insertaEnDiv(json, contenido, usuario, nombrePropuesta, idPropuesta));
}

document.addEventListener("DOMContentLoaded", () => {
	for (let p of document.getElementsByClassName("propuesta")) {
		p.onclick = c => cargaChat(p.dataset.id)
	}
})


// envía json, espera json de vuelta; lanza error si status != 200
function go(url, method, data = {}) {
	let params = {
		method: method, // POST, GET, POST, PUT, DELETE, etc.
		headers: {
			"Content-Type": "application/json; charset=utf-8",
		},
		body: JSON.stringify(data)
	};
	if (method === "GET") {
		// GET requests cannot have body
		delete params.body;
	}
	console.log("sending", url, params)
	return fetch(url, params).then(response => {
		if (response.ok) {
			return data = response.json();
		} else {
			response.text().then(t => { throw new Error(t + ", at " + url) });
		}
	})
}