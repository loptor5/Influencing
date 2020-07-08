document.addEventListener("DOMContentLoaded", () => {
	
	sendMessage(document.getElementById("sendmsg"));
	
	let propuesta = document.querySelectorAll('.propuesta')	
	propuesta.onclick = (e) => {
		e.preventDefault();
		go("/devuelveChatNegociacion", 'POST', {nombre: propuesta.find("span").value})
		.then()
		.catch()
		}	
	});

function sendMessage(element) {
	element.onclick = (e) => {
		e.preventDefault();
		console.log(element, element.parentNode);
		go(element.parentNode.action, 'POST', {message: 
				document.getElementById("message").value})
			.then(d => console.log("happy", d))
			.catch(e => console.log("sad", e))
	}
}

function formatDate(d) {
	// 2020-03-23T10:48:11.074 => 23/3/2020@10:48:18
	return new Date(d).toLocaleString("es-ES").split(" ").join("@")
}

new simpleDatatables.DataTable(
			'#datatable', { 
			    ajax: {
			        url: config.rootUrl + "message/received", // empieza siempre por config.rootUrl
			        load: xhr => {
			        	let data = JSON.parse(xhr.responseText);
			        	for (let i=0; i<data.length; i++) {
			        		let row = data[i];
			        		row.sent = formatDate(row.sent);
			        		if (row.received) {
			        			row.received = formatDate(row.received);
			        		}
			        	}

			        	return JSON.stringify(data);
			       }
			   }
});