-- 
-- El contenido de este fichero se cargará al arrancar la aplicación, suponiendo que uses
-- 		application-default ó application-externaldb en modo 'create'
--

-- Usuario de ejemplo con nombre = a y contraseña = aa  

INSERT INTO usuario(id,activo,nombre_cuenta,nombre,password,roles,apellidos,edad,score,num_contrataciones,verificado,fecha_registro) VALUES (
	1, true, 'a','Administrador A',
	'{bcrypt}$2a$10$xLFtBIXGtYvAbRqM95JhcOaG23fHRpDoZIJrsF2cCff9xEHTTdK1u',
	'ADMIN',
	'Abundio Ejemplez',
	23,
	4.5,
	1,true,'2015-07-23'
);

-- Otro usuario de ejemplo con username = b y contraseña = aa  


INSERT INTO usuario(id,activo,nombre_cuenta,nombre,password,roles,apellidos,edad,tags,score,num_contrataciones,verificado,fecha_registro) VALUES (
	2, true, 'b','InfluencerB',
	'{bcrypt}$2a$10$xLFtBIXGtYvAbRqM95JhcOaG23fHRpDoZIJrsF2cCff9xEHTTdK1u',
	'INFLUENCER',
	'Berta Muestrez',
	3,
	'Fiesta',
	4,1,false,'2015-07-22'
);

INSERT INTO usuario(id,activo,nombre,nombre_cuenta,password,roles,apellidos,edad,tags,score,num_contrataciones,verificado,fecha_registro) VALUES (
	3, true, 'InfluencerC', 'c', 
	'{bcrypt}$2a$10$xLFtBIXGtYvAbRqM95JhcOaG23fHRpDoZIJrsF2cCff9xEHTTdK1u',
	'INFLUENCER',
	'Sanchez Garcia',
	22,
	'Videojuegos',
	4,1,true,'2015-07-21'
);

INSERT INTO usuario(id,activo,nombre,nombre_cuenta,password,roles,apellidos,edad,tags,score,num_contrataciones,verificado,fecha_registro) VALUES (
	4, true, 'EmpresaD', 'd',
	'{bcrypt}$2a$10$xLFtBIXGtYvAbRqM95JhcOaG23fHRpDoZIJrsF2cCff9xEHTTdK1u',
	'EMPRESA',
	'Martin Gimeno',
	22,
	'Bebidas,Espirituosas',
	3,1,true,'2015-07-20'
);

INSERT INTO propuesta(id,descripcion,nombre,sueldo,tags,empresa_id,activa, edad_min_publico, edad_max_publico, fecha_inicio, fecha_fin, fecha_subida, tipo)  VALUES (
	1,
	'Realizar publicidad en metro',
	'Metropubli',
	100,
	'Ciudad',
	4,true,1,5,'2017-07-20','2017-07-23','2017-07-23','PROPUESTA' 
);

INSERT INTO propuesta(id,descripcion,nombre,sueldo,tags,empresa_id,activa, edad_min_publico, edad_max_publico, fecha_inicio, fecha_fin, fecha_subida, tipo) VALUES (
	2,
	'Realizar publicidad en taxi',
	'Taxipubli',
	200,
	'Ciudad,Taxi,Transporte',
	4,true,1,5,'2019-07-20','2019-07-23','2019-07-23','PROPUESTA'
);
INSERT INTO propuesta(id,descripcion,nombre,sueldo,tags,empresa_id, activa, edad_min_publico, edad_max_publico, fecha_inicio, fecha_fin, fecha_subida, tipo) VALUES (
	3,
	'Publicitar festival',
	'Festipubli',
	500,
	'Música',
	4,
	true,1,5,'2018-07-20','2018-07-23','2018-07-23','PROPUESTA'
);


INSERT INTO candidatura(id,aceptada,estado,candidato_id,propuesta_id) VALUES (
	1,
	true,
	'EN_VALORACION',
	2,
	1
);

INSERT INTO candidatura(id,aceptada,estado,candidato_id,propuesta_id) VALUES (
	2,
	false,
	'NEGOCIANDO',
	2,
	3
);

INSERT INTO candidatura(id,aceptada,estado,candidato_id,propuesta_id) VALUES (
	3,
	true,
	'EN_VALORACION',
	3,
	3
);

INSERT INTO candidatura(id,aceptada,estado,candidato_id,propuesta_id) VALUES (
	4,
	false,
	'EN_ULTIMATUM',
	3,
	2
);

INSERT INTO evento(id, descripcion, tipo, fecha_enviado,fecha_recibido, leido, candidatura_id, emisor_id, receptor_id) VALUES(
	1, 'se ha registrado un usuario', 'ADMINISTRACION','2017-07-23','2017-07-23', false,
	NULL,
	2,
	1
);

INSERT INTO evento(id, descripcion, tipo, fecha_enviado,fecha_recibido,leido, candidatura_id, emisor_id, receptor_id) VALUES(
	2, 'se ha registrado otro usuario', 'ADMINISTRACION','2017-07-23','2017-07-23', false,
	NULL,
	2,
	1
);


INSERT INTO evento(id, descripcion, tipo, fecha_enviado,fecha_recibido,leido, candidatura_id, emisor_id, receptor_id) VALUES(
	3, 'se ha registrado un usuario', 'CHAT','2017-07-23','2017-07-23', false,
	1,
	2,
	4
);

INSERT INTO evento(id, descripcion, tipo, fecha_enviado,fecha_recibido, leido, candidatura_id, emisor_id, receptor_id) VALUES(
	4, 'se ha registrado otro usuario', 'CHAT','2017-07-23','2017-07-23', false,
	1,
	4,
	2
);

INSERT INTO evento(id, descripcion, tipo, fecha_enviado,fecha_recibido,leido, candidatura_id, emisor_id, receptor_id) VALUES(
	5, 'Lo siento, la oferta no me interesa. Saludos.', 'NOTIFICACION','2017-07-23','2017-07-23', false,
	NULL,
	2,
	1
);

INSERT INTO evento(id, descripcion, tipo, fecha_enviado,fecha_recibido,leido, candidatura_id, emisor_id, receptor_id) VALUES(
	6, 'Hola, estoy interesado en participar en la propuesta', 'NOTIFICACION','2017-07-23','2017-07-23', false,
	NULL,
	2,
	1
);

INSERT INTO evento(id, descripcion, tipo, fecha_enviado,fecha_recibido,leido, candidatura_id, emisor_id, receptor_id) VALUES(
	7, 'Hola, ¿como te encuentras?', 'NOTIFICACION','2017-07-23','2017-07-23', false,
	NULL,
	2,
	1
);

INSERT INTO valoracion(id, emisor_id, candidatura_id, valoracion, puntuacion) VALUES(
	1, 2, 1,'me ha timado', 3
);

INSERT INTO denuncia(id, denunciante_id, denunciado_id, titulo, descripcion, fecha, tramitada) VALUES(
	1, 2,3,'Prueba','Denuncia test', '2018-07-20', false
);

INSERT INTO perfilrrss(id, influencer_id, nombre, num_seguidores, rrss) VALUES (
	1, 2, 'CuentaTwitter', 10, 'Twitter'
);

INSERT INTO perfilrrss(id, influencer_id, nombre, num_seguidores, rrss) VALUES (
	2, 2, 'CuentaFacebook', 10, 'Facebook'
);
INSERT INTO perfilrrss(id, influencer_id, nombre, num_seguidores, rrss) VALUES (
	3, 2, 'CuentaInstagram', 10, 'Instagram'
);

