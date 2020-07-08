function prepareListeners(tipo) {
	let inputBusqueda =  document.getElementById("cuadroBusquedaTagBar")

	inputBusqueda.addEventListener("keyup", function(event) {
		if (event.keyCode === 13) {
		    // Cancel the default action, if needed
		    event.preventDefault();
		    // Trigger the button element with a click
		    cargaBusquedas(inputBusqueda.value);
		 }
	}); 
	if (tipo == "tag"){
		for (let boton of document.getElementsByClassName("botonPaginacion")) {
			boton.onclick = p => botonListaTags(boton.dataset.propPatron,boton.dataset.propIndice);			
		}
	}
	else{
	for (let boton of document.getElementsByClassName("botonPaginacion")) {
			boton.onclick = p => botonLista(boton.dataset.propPatron,boton.dataset.propIndice);
	}
	}
	
	for (let p of document.getElementsByClassName("imagen")) {
		p.onclick = c => cargaModalPerfil(p.dataset.id)
	}
	
	for (let p of document.getElementsByClassName("btnDetalles")) {
		p.onclick = c => cargaModalPerfil(p.dataset.id)
	}


	for (let p of document.getElementsByClassName("tagFilter")) {
		p.onclick = c => cargaBusquedasPorTag(p.dataset.id)
	}
	
}


document.addEventListener("DOMContentLoaded", () => {
	lanzaAlert();
	prepareListeners();
})

function cargaBusquedas(patron){
	let param = {
		patron:patron,
		indice:1
	}
	
	return go2(config.rootUrl + "busquedaPerfil/busca", 'POST', param)
		.then(html => { 
			var  div = document.getElementById("divPerfiles");
			div.innerHTML = html;
			prepareListeners();
		})
		.catch(e => console.log(e))

}


function cargaBusquedasPorTag(tag){
	return go2(config.rootUrl + "busquedaPerfil/tags?tag=" + tag, 'GET')
	.then(html => { 
	var  div = document.getElementById("divPerfiles");
		div.innerHTML = html;
		prepareListeners("tag");
	})
		.catch(e => console.log(e))

}


function botonLista(patron="", indice){
	let param = {
		patron:patron,
		indice:indice
	}
	return go2(config.rootUrl + "busquedaPerfil/busca", 'POST', param)
		.then(html => { 
			var  div = document.getElementById("divPerfiles");
			div.innerHTML = html;
			prepareListeners();
		})
		.catch(e => console.log(e))
}

function botonListaTags(tag="", indice){
	return go2(config.rootUrl + "busquedaPerfil/tags?tag=" + tag+"&indicePagina="+indice, 'GET')
		.then(html => { 
			var  div = document.getElementById("divPerfiles");
			div.innerHTML = html;
			prepareListeners("tag");
		})
		.catch(e => console.log(e))
}