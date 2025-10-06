 /* ELIMINAR DATOS DE LA BASE DE DATOS */
DO
$$
DECLARE
    tabla RECORD;
BEGIN
    -- Desactivar las restricciones de claves foráneas temporalmente
    EXECUTE 'SET session_replication_role = replica';

    -- Iterar sobre todas las tablas en el esquema público
    FOR tabla IN
        SELECT tablename
        FROM pg_tables
        WHERE schemaname = 'public'
    LOOP
        -- Borrar datos de cada tabla
        EXECUTE format('TRUNCATE TABLE %I CASCADE', tabla.tablename);
    END LOOP;

    -- Reactivar las restricciones de claves foráneas
    EXECUTE 'SET session_replication_role = DEFAULT';
END
$$;




/* INSERTAMOS DATOS EN LA TABLA USERS */
INSERT INTO Users (id, email, first_name, last_name, password, phone, address, postal_code, city, country, role, user_name, points)
VALUES (1, 'loremipsum@email.com', 'Lorem', 'Ipsum', '$2a$12$eFpzD2rN/HSFoBq6Fw1NYez/G6lEsIrypXvMASAt9PlQuMKjZlpVW', '0123456789', 'address', '12345', 'Seville', 'Spain', 'USER', 'Lorem ipsum', 0),
	   (2, 'jedu937@gmail.com', 'Eduardo', 'Bustamante', '$2a$12$XvsQmtpwvFIbeCKTAd0eYun8wBISBPRWaXfh.6vx4yxCbEHsLrE1u','0123456789', 'address', '12345', 'Seville', 'Spain', 'USER', 'jedu', 500 ),
	   (3, 'duki@email.com', 'Mauro', 'Lombardo', '$2a$12$eFpzD2rN/HSFoBq6Fw1NYez/G6lEsIrypXvMASAt9PlQuMKjZlpVW', '0123456789', 'address', '12345', 'Seville', 'Spain','ARTIST', 'Duki', 0 ),
	   (4,'mariabecerra@email.com', 'Maria', 'Becerra', '$2a$12$eFpzD2rN/HSFoBq6Fw1NYez/G6lEsIrypXvMASAt9PlQuMKjZlpVW', '0123456789', 'address', '12345', 'Seville', 'Spain','ARTIST', 'Maria Becerra', 0),
	   (5,'litkillah@email.com', 'Mauro', 'Román', '$2a$12$eFpzD2rN/HSFoBq6Fw1NYez/G6lEsIrypXvMASAt9PlQuMKjZlpVW', '0123456789', 'address', '12345', 'Seville', 'Spain', 'ARTIST', 'Lit Killah', 0),
	   (6,'trueno@email.com', 'Mateo', 'Palacios', '$2a$12$eFpzD2rN/HSFoBq6Fw1NYez/G6lEsIrypXvMASAt9PlQuMKjZlpVW', '0123456789', 'address', '12345', 'Seville', 'Spain', 'ARTIST', 'Trueno', 0),
	   (7, 'mod.artistheaven@gmail.com', 'admin', 'admin', '$2a$12$TVGYqsM1PlhGG62ovr5dUOV6IZSf1cc7pI.BeuORKWPNrLz7n6L.u','0123456789', 'address', '12345', 'Seville', 'Spain', 'ADMIN', 'admin', 0 ),
	   (8, 'dummyuser@email.com', 'dummy', 'user', '$2a$12$nwf98CDpUoptgVOSEuotBuDdaA1V46A/O6T3510PDDRwQUp1dbiI6','0123456789', 'address', '12345', 'Seville', 'Spain', 'USER', 'dummyUser', 0 );
	   
INSERT INTO shopping_cart (id, user_id) VALUES 
(1,1),
(2,2),
(3,3),
(4,4),
(5,5),
(6,6),
(7,7),
(8,8);

INSERT INTO Artist (id, artist_name, is_verificated, artist_url, main_view_photo, main_color) 
VALUES (3, 'Duki', 'true', 'https://www.youtube.com/@duki', '/mainArtist_media/duki.png', '#8d0100'),
       (4, 'Maria Becerra', 'true', 'https://www.youtube.com/@MariaBecerraMusic', '/mainArtist_media/MariaBecerra.jpg', '#FF99F5'),
       (5, 'Lit Killah', 'true', 'https://www.youtube.com/@LITkillah', '/mainArtist_media/Litkillah.png', '#4C0059'),
	   (6, 'Trueno', 'true', 'https://www.youtube.com/@TruenoOficial', '/mainArtist_media/trueno.png', '#F3B229');
	   
INSERT INTO Collection (id, created_at, is_promoted,name) VALUES 
(1, '2025-09-01', true, 'SUMMER-2025');
	   
INSERT INTO Product (id, available, description, name, price, on_Promotion, discount, created_date, section, reference, composition, shipping_details, collection_id)
VALUES (1, true, 'Lleva el flow de Duki a cada esquina con esta camiseta urbana. Diseño cómodo, corte street wear y actitud que no pasa desapercibida. Perfecta para fans y amantes del trap argentino.',
	   'Duki Flow Tee', 50, true, 20, '2025-06-20 15:31:00', 'TSHIRT', 12345, '# COMPOSICIÓN

Nuestra camiseta de Duki está diseñada para quienes viven el ritmo urbano y el trap argentino. Hecha con materiales suaves y duraderos, combina comodidad y estilo street wear. Priorizamos fibras responsables para reducir el impacto ambiental y hacer que cada prenda dure más.

# MATERIALES

- 100% Algodón orgánico

# CUIDADOS

Cuidar de tus prendas es cuidar del medioambiente.

Lava a baja temperatura y con programas suaves para conservar mejor la prenda y reducir el consumo energético.

## Guía de cuidados

![No usar lejía](/icons/lejia.png) No usar lejía / blanqueador.

![Planchar máximo 110°C](/icons/iron.png) Planchar a un máximo de 110°C.

![No usar secadora](/icons/dryer-no.png) No usar secadora.

![Lavar máx 30ºC](/icons/wash.png) Lavar a máquina a un máximo de 30ºC. Centrifugado corto.

# ORIGEN

Contamos con requisitos de trazabilidad para conocer la cadena de suministro de nuestras producciones.  
Solicitamos a nuestros proveedores que nos informen sobre todas las instalaciones involucradas en los procesos de producción, desde la fibra hasta la prenda final.  
Esto incluye tanto las fábricas propias como externas, así como los intermediarios involucrados en cada proceso.

**Hecho en España**
', '# ENVÍO Y DEVOLUCIONES

## ENVÍO A DOMICILIO
- **Tiempo estimado de entrega**: 3-5 días laborables.
- **Coste de envío**: Actualmente, el envío es **gratuito** debido a la apertura de nuestra tienda online.

## CAMBIOS Y DEVOLUCIONES
- Dispones de **30 días** desde la fecha de recepción de tu pedido para realizar cambios o devoluciones.

## PROCESO DE DEVOLUCIÓN

Para realizar una devolución, sigue uno de los siguientes pasos dependiendo de si estás autenticado o no:

1. **Si no estás autenticado**: Haz clic en la lupa (arriba de la página) y escribe tu número de pedido. Podrás ver el estado de tu pedido, y si ha sido entregado, podrás solicitar la devolución.
   
2. **Si estás autenticado**: Ingresa a tu perfil (icono arriba a la derecha), ve a "MY SPACE" y luego a "MY ORDER". Allí podrás ver tus pedidos y solicitar devoluciones si ya han sido entregados.

3. **Empaque**: Asegúrate de que el artículo esté en su estado original, sin usar, y con las etiquetas intactas.

4. **Devolución**: Envíanos el artículo según la opción seleccionada para la devolución.

Para más detalles, puedes escribirnos a **mod.artistheaven@gmail.com**.

## NOTAS IMPORTANTES
- El coste de los envíos de devolución corre por cuenta del usuario.
- En caso de cambios, el usuario debe seguir el mismo proceso de devolución y enviar el artículo de acuerdo con las opciones disponibles.', null),
	   (2, false, 'Camiseta unisex de corte clásico, fabricada con algodón peinado de alta calidad para un tacto suave y duradero.',
	   'Camiseta 002', 25, false,0, '2025-06-20 15:29:00', 'TSHIRT', 12346, '.', '.',null),
	   (3, true, 'Completa tu look urbano con la gorra oficial de Nicki Nicole. Diseño cómodo, ajustable y con actitud que destaca en cualquier esquina. Perfecta para fans y amantes del street style.',
	   'Nicki on Top', 40, false,0, '2025-06-20 15:28:00', 'ACCESSORIES', 12347, '# COMPOSICIÓN

Nuestra gorra de Nicki Nicole combina estilo urbano con materiales resistentes y ligeros, pensados para uso diario. Además, priorizamos fibras responsables para reducir el impacto ambiental y asegurar durabilidad.

# MATERIALES

- 100% Algodón orgánico
- Visera reforzada con poliéster reciclado

# CUIDADOS

Cuidar de tus prendas es cuidar del medioambiente.

Lava a mano o con programas suaves y evita temperaturas altas para conservar mejor la prenda.

## Guía de cuidados

![No usar lejía](/icons/lejia.png) No usar lejía / blanqueador.

![Planchar máximo 110°C](/icons/iron.png) No planchar la gorra.

![No usar secadora](/icons/dryer-no.png) No usar secadora.

![Lavar máx 30ºC](/icons/wash.png) Lavar a mano o con programa suave a máximo 30ºC.

# ORIGEN

Contamos con requisitos de trazabilidad para conocer la cadena de suministro de nuestras producciones.  
Solicitamos a nuestros proveedores que nos informen sobre todas las instalaciones involucradas en los procesos de producción, desde la fibra hasta el producto final.  
Esto incluye tanto las fábricas propias como externas, así como los intermediarios involucrados en cada proceso.

**Hecho en España**
', '# ENVÍO Y DEVOLUCIONES

## ENVÍO A DOMICILIO
- **Tiempo estimado de entrega**: 3-5 días laborables.
- **Coste de envío**: Actualmente, el envío es **gratuito** debido a la apertura de nuestra tienda online.

## CAMBIOS Y DEVOLUCIONES
- Dispones de **30 días** desde la fecha de recepción de tu pedido para realizar cambios o devoluciones.

## PROCESO DE DEVOLUCIÓN

Para realizar una devolución, sigue uno de los siguientes pasos dependiendo de si estás autenticado o no:

1. **Si no estás autenticado**: Haz clic en la lupa (arriba de la página) y escribe tu número de pedido. Podrás ver el estado de tu pedido, y si ha sido entregado, podrás solicitar la devolución.
   
2. **Si estás autenticado**: Ingresa a tu perfil (icono arriba a la derecha), ve a "MY SPACE" y luego a "MY ORDER". Allí podrás ver tus pedidos y solicitar devoluciones si ya han sido entregados.

3. **Empaque**: Asegúrate de que el artículo esté en su estado original, sin usar, y con las etiquetas intactas.

4. **Devolución**: Envíanos el artículo según la opción seleccionada para la devolución.

Para más detalles, puedes escribirnos a **mod.artistheaven@gmail.com**.

## NOTAS IMPORTANTES
- El coste de los envíos de devolución corre por cuenta del usuario.
- En caso de cambios, el usuario debe seguir el mismo proceso de devolución y enviar el artículo de acuerdo con las opciones disponibles.',1),
	   (4, true, 'Lleva la vibra urbana de Emilia a cada paso con estos pantalones cómodos y con actitud. Corte street wear, estilo auténtico y materiales que te acompañan todo el día. Perfectos para fans y amantes del flow urbano.',
	   'Emilia Street Pants', 40, false,0, '2025-06-20 15:27:00', 'PANTS', 12348, '# COMPOSICIÓN

Nuestros pantalones de Emilia combinan estilo urbano con comodidad total. Hechos con materiales resistentes y suaves, están diseñados para moverse contigo en cada momento del día. Además, usamos fibras responsables para reducir el impacto ambiental y garantizar durabilidad.

# MATERIALES

- 70% Algodón orgánico
- 30% Poliéster reciclado

# CUIDADOS

Cuidar de tus prendas es cuidar del medioambiente.

Lava a baja temperatura y con programas suaves para conservar mejor la prenda y reducir el consumo energético.

## Guía de cuidados

![No usar lejía](/icons/lejia.png) No usar lejía / blanqueador.

![Planchar máximo 110°C](/icons/iron.png) Planchar a un máximo de 110°C.

![No usar secadora](/icons/dryer-no.png) No usar secadora.

![Lavar máx 30ºC](/icons/wash.png) Lavar a máquina a un máximo de 30ºC. Centrifugado corto.

# ORIGEN

Contamos con requisitos de trazabilidad para conocer la cadena de suministro de nuestras producciones.  
Solicitamos a nuestros proveedores que nos informen sobre todas las instalaciones involucradas en los procesos de producción, desde la fibra hasta la prenda final.  
Esto incluye tanto las fábricas propias como externas, así como los intermediarios involucrados en cada proceso.

**Hecho en España**
', '# ENVÍO Y DEVOLUCIONES

## ENVÍO A DOMICILIO
- **Tiempo estimado de entrega**: 3-5 días laborables.
- **Coste de envío**: Actualmente, el envío es **gratuito** debido a la apertura de nuestra tienda online.

## CAMBIOS Y DEVOLUCIONES
- Dispones de **30 días** desde la fecha de recepción de tu pedido para realizar cambios o devoluciones.

## PROCESO DE DEVOLUCIÓN

Para realizar una devolución, sigue uno de los siguientes pasos dependiendo de si estás autenticado o no:

1. **Si no estás autenticado**: Haz clic en la lupa (arriba de la página) y escribe tu número de pedido. Podrás ver el estado de tu pedido, y si ha sido entregado, podrás solicitar la devolución.
   
2. **Si estás autenticado**: Ingresa a tu perfil (icono arriba a la derecha), ve a "MY SPACE" y luego a "MY ORDER". Allí podrás ver tus pedidos y solicitar devoluciones si ya han sido entregados.

3. **Empaque**: Asegúrate de que el artículo esté en su estado original, sin usar, y con las etiquetas intactas.

4. **Devolución**: Envíanos el artículo según la opción seleccionada para la devolución.

Para más detalles, puedes escribirnos a **mod.artistheaven@gmail.com**.

## NOTAS IMPORTANTES
- El coste de los envíos de devolución corre por cuenta del usuario.
- En caso de cambios, el usuario debe seguir el mismo proceso de devolución y enviar el artículo de acuerdo con las opciones disponibles.', null),
	   (5, true, 'Diseño inspirado en la fuerza lírica y presencia de Trueno; una camiseta que late al ritmo del hip hop y viste la calle con autoridad.',
	   'BajoFuego', 50, false,0, '2025-06-20 15:26:00', 'TSHIRT', 12349, '# COMPOSICIÓN

Camiseta urbana confeccionada con materiales de alta calidad, suaves al tacto y resistentes al uso diario. El algodón proviene de cultivos responsables que reducen el consumo de agua y minimizan el impacto ambiental. El poliéster reciclado aporta durabilidad y ayuda a dar una segunda vida a materiales ya existentes.

# MATERIALES

- 70% Algodón orgánico
- 30% Poliéster reciclado

# CUIDADOS

Cuidar de tus prendas es cuidar del medioambiente.

Lava a baja temperatura y con programas suaves para conservar mejor la prenda y reducir el consumo energético.

## Guía de cuidados

![No usar lejía](/icons/lejia.png) No usar lejía / blanqueador.  

![Planchar máximo 110°C](/icons/iron.png) Planchar a un máximo de 110°C.  

![No usar secadora](/icons/dryer-no.png) No usar secadora.  

![Lavar máx 30ºC](/icons/wash.png) Lavar a máquina a un máximo de 30ºC. Centrifugado corto.  

# ORIGEN

Contamos con requisitos de trazabilidad para conocer la cadena de suministro de nuestras producciones.  
Solicitamos a nuestros proveedores que nos informen sobre todas las instalaciones involucradas en los procesos de producción, desde el hilo (o la fibra) hasta la prenda final para cada pedido.  
Esto incluye tanto las fábricas propias como externas, así como los intermediarios involucrados en cada proceso.

**Hecho en España**
', '# ENVÍO Y DEVOLUCIONES

## ENVÍO A DOMICILIO
- **Tiempo estimado de entrega**: 3-5 días laborables.
- **Coste de envío**: Actualmente, el envío es **gratuito** debido a la apertura de nuestra tienda online.

## CAMBIOS Y DEVOLUCIONES
- Dispones de **30 días** desde la fecha de recepción de tu pedido para realizar cambios o devoluciones.

## Proceso de Devolución

Para realizar una devolución, sigue uno de los siguientes pasos dependiendo de si estás autenticado o no:

1. **Si no estás autenticado**: Haz clic en la lupa (arriba de la página) y escribe tu número de pedido. Podrás ver el estado de tu pedido, y si ha sido entregado, podrás solicitar la devolución.
   
2. **Si estás autenticado**: Ingresa a tu perfil (icono arriba a la derecha), ve a "MY SPACE" y luego a "MY ORDER". Allí podrás ver tus pedidos y solicitar devoluciones si ya han sido entregados.

3. **Empaque**: Asegúrate de que el artículo esté en su estado original, sin usar, y con las etiquetas intactas.

4. **Devolución**: Envíanos el artículo según la opción seleccionada para la devolución.

Para más detalles, puedes escribirnos a **mod.artistheaven@gmail.com**.

## NOTAS IMPORTANTES
- El coste de los envíos de devolución corre por cuenta del usuario.
- En caso de cambios, el usuario debe seguir el mismo proceso de devolución y enviar el artículo de acuerdo con las opciones disponibles.

', null),
	   (6, true, 'Sube el volumen de tu estilo con la sudadera oficial de LitKillah. Diseño cómodo, corte urbano y actitud que no pasa desapercibida. Perfecta para destacar en cada esquina o escenario.',
	   'LitKillah Hoodie', 60, false,0, '2025-06-20 15:25:00', 'HOODIES', 12340, '# COMPOSICIÓN

Nuestra sudadera de LitKillah combina estilo urbano y confort total. Con materiales suaves y resistentes, está diseñada para acompañarte en cada aventura callejera. Además, buscamos minimizar el impacto ambiental usando fibras responsables y procesos sostenibles.

# MATERIALES

- 80% Algodón orgánico
- 20% Poliéster reciclado

# CUIDADOS

Cuidar de tus prendas es cuidar del medioambiente.

Lava a baja temperatura y con programas suaves para conservar mejor la prenda y reducir el consumo energético.

## Guía de cuidados

![No usar lejía](/icons/lejia.png) No usar lejía / blanqueador.

![Planchar máximo 110°C](/icons/iron.png) Planchar a un máximo de 110°C.

![No usar secadora](/icons/dryer-no.png) No usar secadora.

![Lavar máx 30ºC](/icons/wash.png) Lavar a máquina a un máximo de 30ºC. Centrifugado corto.

# ORIGEN

Contamos con requisitos de trazabilidad para conocer la cadena de suministro de nuestras producciones.  
Solicitamos a nuestros proveedores que nos informen sobre todas las instalaciones involucradas en los procesos de producción, desde la fibra hasta la prenda final.  
Esto incluye tanto las fábricas propias como externas, así como los intermediarios involucrados en cada proceso.

**Hecho en España**', '# ENVÍO Y DEVOLUCIONES

## ENVÍO A DOMICILIO
- **Tiempo estimado de entrega**: 3-5 días laborables.
- **Coste de envío**: Actualmente, el envío es **gratuito** debido a la apertura de nuestra tienda online.

## CAMBIOS Y DEVOLUCIONES
- Dispones de **30 días** desde la fecha de recepción de tu pedido para realizar cambios o devoluciones.

## PROCESO DE DEVOLUCIÓN

Para realizar una devolución, sigue uno de los siguientes pasos dependiendo de si estás autenticado o no:

1. **Si no estás autenticado**: Haz clic en la lupa (arriba de la página) y escribe tu número de pedido. Podrás ver el estado de tu pedido, y si ha sido entregado, podrás solicitar la devolución.
   
2. **Si estás autenticado**: Ingresa a tu perfil (icono arriba a la derecha), ve a "MY SPACE" y luego a "MY ORDER". Allí podrás ver tus pedidos y solicitar devoluciones si ya han sido entregados.

3. **Empaque**: Asegúrate de que el artículo esté en su estado original, sin usar, y con las etiquetas intactas.

4. **Devolución**: Envíanos el artículo según la opción seleccionada para la devolución.

Para más detalles, puedes escribirnos a **mod.artistheaven@gmail.com**.

## NOTAS IMPORTANTES
- El coste de los envíos de devolución corre por cuenta del usuario.
- En caso de cambios, el usuario debe seguir el mismo proceso de devolución y enviar el artículo de acuerdo con las opciones disponibles.', null),
	   (7, true, 'Lleva la energía y el flow de Nicki Nicole a cada calle y escenario. Camiseta cómoda, con corte urbano y diseño que destaca tu estilo auténtico. Perfecta para fans y amantes del street style.',
	   'Nicki Nicole Street Tee', 40, false,0, '2025-06-20 15:24:00', 'TSHIRT', 12341, '# COMPOSICIÓN

Nuestra camiseta de Nicki Nicole está hecha para quienes viven el ritmo urbano. Con materiales suaves y duraderos, combina comodidad con estilo street wear. Además, priorizamos fibras responsables para reducir el impacto ambiental y hacer que cada prenda dure más.

# MATERIALES

- 100% Algodón orgánico

# CUIDADOS

Cuidar de tus prendas es cuidar del medioambiente.

Lava a baja temperatura y con programas suaves para conservar mejor la prenda y reducir el consumo energético.

## Guía de cuidados

![No usar lejía](/icons/lejia.png) No usar lejía / blanqueador.

![Planchar máximo 110°C](/icons/iron.png) Planchar a un máximo de 110°C.

![No usar secadora](/icons/dryer-no.png) No usar secadora.

![Lavar máx 30ºC](/icons/wash.png) Lavar a máquina a un máximo de 30ºC. Centrifugado corto.

# ORIGEN

Contamos con requisitos de trazabilidad para conocer la cadena de suministro de nuestras producciones.  
Solicitamos a nuestros proveedores que nos informen sobre todas las instalaciones involucradas en los procesos de producción, desde la fibra hasta la prenda final.  
Esto incluye tanto las fábricas propias como externas, así como los intermediarios involucrados en cada proceso.

**Hecho en España**', '# ENVÍO Y DEVOLUCIONES

## ENVÍO A DOMICILIO
- **Tiempo estimado de entrega**: 3-5 días laborables.
- **Coste de envío**: Actualmente, el envío es **gratuito** debido a la apertura de nuestra tienda online.

## CAMBIOS Y DEVOLUCIONES
- Dispones de **30 días** desde la fecha de recepción de tu pedido para realizar cambios o devoluciones.

## PROCESO DE DEVOLUCIÓN

Para realizar una devolución, sigue uno de los siguientes pasos dependiendo de si estás autenticado o no:

1. **Si no estás autenticado**: Haz clic en la lupa (arriba de la página) y escribe tu número de pedido. Podrás ver el estado de tu pedido, y si ha sido entregado, podrás solicitar la devolución.
   
2. **Si estás autenticado**: Ingresa a tu perfil (icono arriba a la derecha), ve a "MY SPACE" y luego a "MY ORDER". Allí podrás ver tus pedidos y solicitar devoluciones si ya han sido entregados.

3. **Empaque**: Asegúrate de que el artículo esté en su estado original, sin usar, y con las etiquetas intactas.

4. **Devolución**: Envíanos el artículo según la opción seleccionada para la devolución.

Para más detalles, puedes escribirnos a **mod.artistheaven@gmail.com**.

## NOTAS IMPORTANTES
- El coste de los envíos de devolución corre por cuenta del usuario.
- En caso de cambios, el usuario debe seguir el mismo proceso de devolución y enviar el artículo de acuerdo con las opciones disponibles.',null),
	   (8, true, 'Súbete al ritmo del barrio con el bucket hat oficial de Trueno. Diseño urbano, vibra auténtica y actitud que no se apaga. Perfecto para destacar en cualquier esquina o escenario.',
	   'Trueno Storm Hat', 25, false,0, '2025-06-20 15:23:00', 'ACCESSORIES', 12342, '# COMPOSICIÓN

Nuestro bucket hat de Trueno combina estilo urbano con materiales de alta calidad, pensados para ofrecer durabilidad y confort. La elección de estos materiales busca minimizar el impacto ambiental, priorizando fibras responsables y procesos sostenibles siempre que es posible.

# MATERIALES

- 100% Algodón orgánico

# CUIDADOS

Cuidar de tus prendas es cuidar del medioambiente.

Lava a baja temperatura y con programas suaves para conservar mejor la prenda y reducir el consumo energético.

## Guía de cuidados

![No usar lejía](/icons/lejia.png) No usar lejía / blanqueador.

![Planchar máximo 110°C](/icons/iron.png) Planchar a un máximo de 110°C.

![No usar secadora](/icons/dryer-no.png) No usar secadora.

![Lavar máx 30ºC](/icons/wash.png) Lavar a máquina a un máximo de 30ºC. Centrifugado corto.

# ORIGEN

Contamos con requisitos de trazabilidad para conocer la cadena de suministro de nuestras producciones.  
Solicitamos a nuestros proveedores que nos informen sobre todas las instalaciones involucradas en los procesos de producción, desde la fibra hasta el producto final.  
Esto incluye tanto las fábricas propias como externas, así como los intermediarios involucrados en cada proceso.

**Hecho en España**
', '# ENVÍO Y DEVOLUCIONES

## ENVÍO A DOMICILIO
- **Tiempo estimado de entrega**: 3-5 días laborables.
- **Coste de envío**: Actualmente, el envío es **gratuito** debido a la apertura de nuestra tienda online.

## CAMBIOS Y DEVOLUCIONES
- Dispones de **30 días** desde la fecha de recepción de tu pedido para realizar cambios o devoluciones.

## Proceso de Devolución

Para realizar una devolución, sigue uno de los siguientes pasos dependiendo de si estás autenticado o no:

1. **Si no estás autenticado**: Haz clic en la lupa (arriba de la página) y escribe tu número de pedido. Podrás ver el estado de tu pedido, y si ha sido entregado, podrás solicitar la devolución.
   
2. **Si estás autenticado**: Ingresa a tu perfil (icono arriba a la derecha), ve a "MY SPACE" y luego a "MY ORDER". Allí podrás ver tus pedidos y solicitar devoluciones si ya han sido entregados.

3. **Empaque**: Asegúrate de que el artículo esté en su estado original, sin usar, y con las etiquetas intactas.

4. **Devolución**: Envíanos el artículo según la opción seleccionada para la devolución.

Para más detalles, puedes escribirnos a **mod.artistheaven@gmail.com**.

## NOTAS IMPORTANTES
- El coste de los envíos de devolución corre por cuenta del usuario.
- En caso de cambios, el usuario debe seguir el mismo proceso de devolución y enviar el artículo de acuerdo con las opciones disponibles.

', null),
	   (9, true, 'Gorra oficial de Duki, con sus logos icónicos y estilo urbano. Un must para llevar el trap argentino en cada outfit.',
	   'Crown of Trap', 30, false,0, '2025-06-20 15:23:00', 'ACCESSORIES', 12343, '# COMPOSICIÓN

Gorra urbana inspirada en el estilo de Duki, diseñada para un uso cómodo y duradero. Fabricada con materiales de alta calidad que aseguran resistencia y confort, cuidando el planeta en cada paso del proceso.

# MATERIALES

- 60% Algodón orgánico
- 40% Poliéster reciclado

# CUIDADOS

Cuidar de tus prendas es cuidar del medioambiente.  

Lava a baja temperatura y con programas suaves para conservar mejor la prenda y reducir el consumo energético.

## Guía de cuidados

![No usar lejía](/icons/lejia.png) No usar lejía / blanqueador.  

![Planchar máximo 110°C](/icons/iron.png) Planchar a un máximo de 110°C.  

![No usar secadora](/icons/dryer-no.png) No usar secadora.  

![Lavar máx 30ºC](/icons/wash.png) Lavar a máquina a un máximo de 30ºC. Centrifugado corto.

# ORIGEN

Contamos con requisitos de trazabilidad para conocer la cadena de suministro de nuestras producciones.  
Solicitamos a nuestros proveedores que nos informen sobre todas las instalaciones involucradas en los procesos de producción, desde el hilo hasta la prenda final para cada pedido.  
Esto incluye tanto las fábricas propias como externas, así como los intermediarios involucrados en cada proceso.

**Hecho en España**
','# ENVÍO Y DEVOLUCIONES

## ENVÍO A DOMICILIO
- **Tiempo estimado de entrega**: 3-5 días laborables.
- **Coste de envío**: Actualmente, el envío es **gratuito** debido a la apertura de nuestra tienda online.

## CAMBIOS Y DEVOLUCIONES
- Dispones de **30 días** desde la fecha de recepción de tu pedido para realizar cambios o devoluciones.

## PROCESO DE DEVOLUCIÓN

Para realizar una devolución, sigue uno de los siguientes pasos dependiendo de si estás autenticado o no:

1. **Si no estás autenticado**: Haz clic en la lupa (arriba de la página) y escribe tu número de pedido. Podrás ver el estado de tu pedido, y si ha sido entregado, podrás solicitar la devolución.
   
2. **Si estás autenticado**: Ingresa a tu perfil (icono arriba a la derecha), ve a "MY SPACE" y luego a "MY ORDER". Allí podrás ver tus pedidos y solicitar devoluciones si ya han sido entregados.

3. **Empaque**: Asegúrate de que el artículo esté en su estado original, sin usar, y con las etiquetas intactas.

4. **Devolución**: Envíanos el artículo según la opción seleccionada para la devolución.

Para más detalles, puedes escribirnos a **mod.artistheaven@gmail.com**.

## NOTAS IMPORTANTES
- El coste de los envíos de devolución corre por cuenta del usuario.
- En caso de cambios, el usuario debe seguir el mismo proceso de devolución y enviar el artículo de acuerdo con las opciones disponibles.

',1);

INSERT INTO product_color (id, product_id, color_name, hex_code, available_units, model_reference)
VALUES 
(1, 1, 'Blanco', '#FFFFFF', null, '/product_media/DukiTshirt.glb'),
(2, 1, 'Rojo', '#A60F0F', null, '/product_media/CamisetaDukiRed.glb'),
(3, 2, 'Blanco', '#FFFFFF', null, ''), 
(4, 3, 'Beige', '#f9e9c7', 100, ''),  
(5, 4, 'Blanco', '#FFFFFF', null, ''),
(6, 5, 'Blanco', '#FFFFFF', null, '/product_media/BajoFuego.glb'),
(7, 5, 'Verde', '#005463', null, '/product_media/CamisetaBlueBajoFuego.glb'),
(8, 6, 'Negro', '#000000', null, '/product_media/LitKillahHoodie.glb'),
(9, 7, 'Blanco', '#FFFFFF', null, '/product_media/NickiNicoleStreetTee.glb'),
(10, 8, 'Blanco', '#FFFFFF', 100, ''),  
(11, 9, 'Negro', '#000000', 100, '');   

-- 2. Insertar imágenes asociadas a esos colores
INSERT INTO product_color_images (product_color_id, images)
VALUES 
    (1, '/product_media/camiseta1.png'),
    (1, '/product_media/Camiseta1-back.png'),
    (2, '/product_media/camiseta1Roja.png'),
    (2, '/product_media/Camiseta1Roja-back.png'),
    (3, '/product_media/camiseta1.png'),
    (3, '/product_media/Camiseta1-back.png'),
    (4, '/product_media/GorraNicki-1.png'),
    (4, '/product_media/GorraNicki-2.png'),
    (5, '/product_media/frontJeanEmilia-1.png'),
    (5, '/product_media/backJeanEmilia-1.png'),
    (6, '/product_media/CamisetaTrueno-1.png'),
    (6, '/product_media/CamisetaTrueno-2.png'),
    (7, '/product_media/CamisetaTrueno-1Green.png'),
    (7, '/product_media/CamisetaTrueno-2Green.png'),
    (8, '/product_media/SudaderaLitKillah-1.png'),
    (8, '/product_media/SudaderaLitKillah2.png'),
    (9, '/product_media/CamisetaNicki-1.png'),
    (9, '/product_media/CamisetaNicki-2.png'),
    (10, '/product_media/TruenoBucketHat-1.png'),
    (10, '/product_media/TruenoBucketHat-2.png'),
    (11, '/product_media/Gorra-1.png'),
    (11, '/product_media/Gorra-1-2.png');

INSERT INTO category (id, name)
VALUES (1, 'SUMMER'),
	   (2, 'UNISEX'),
	   (3, 'URBANO'),
	   (4, 'BÁSICOS'),
	   (5, 'HOMBRE'),
       (6, 'MUJER'),
	   (7, 'DUKI'),
	   (8, 'TRUENO'),
	   (9, 'LITKILLAH'),
	   (10, 'NICKINICOLE'),
	   (11, 'MARIABECERRA'),
	   (12,'EMILIA');
	   
INSERT INTO product_color_sizes (product_color_id, size_label, units)
VALUES
(1, 'XS', 50),
(1, 'S', 50),
(1, 'M', 50),
(1, 'L', 50),
(1, 'XL', 50),
(1, 'XXL', 50),
(2, 'XS', 50),
(2, 'S', 50),
(2, 'M', 50),
(2, 'L', 50),
(2, 'XL', 50),
(2, 'XXL', 50),
(4, 'XS', 50),
(4, 'S', 50),
(4, 'M', 50),
(4, 'L', 50),
(4, 'XL', 50),
(4, 'XXL', 50),
(5, 'XS', 50),
(5, 'S', 50),
(5, 'M', 50),
(5, 'L', 50),
(5, 'XL', 50),
(5, 'XXL', 50),
(6, 'XS', 50),
(6, 'S', 50),
(6, 'M', 50),
(6, 'L', 50),
(6, 'XL', 50),
(6, 'XXL', 50),
(7, 'XS', 50),
(7, 'S', 50),
(7, 'M', 50),
(7, 'L', 50),
(7, 'XL', 50),
(7, 'XXL', 50),
(8, 'XS', 50),
(8, 'S', 50),
(8, 'M', 50),
(8, 'L', 50),
(8, 'XL', 50),
(8, 'XXL', 50),
(9, 'XS', 50),
(9, 'S', 50),
(9, 'M', 50),
(9, 'L', 50),
(9, 'XL', 50),
(9, 'XXL', 50);
	   
INSERT INTO Product_category(product_id, category_id)
VALUES (1,1),
       (1,7),
	   (1,2),
	   (1,3),
	   (3,2),
	   (3,10),
	   (4,6),
	   (4,12),
	   (5,2),
	   (5,8),
	   (5,3),
	   (6,5),
	   (6,9),
	   (7,6),
	   (7,10),
	   (8,5),
	   (8,8),
	   (9,5),
	   (9,7);
	   
INSERT INTO RATING(id, comment, score, user_id, created_At, product_id)
VALUES 
(3, 'Excelente calidad, superó mis expectativas.', 5, 2, '2025-08-15', 3),
(4, 'El producto llegó dañado, no lo recomiendo.', 1, 8, '2025-08-17', 7),
(5, 'Buena relación calidad-precio.', 4, 1, '2025-08-18', 5),
(6, 'No está mal, pero podría ser mejor.', 3, 2, '2025-08-20', 1),
(7, 'Entrega rápida y producto en buen estado.', 5, 8, '2025-08-22', 9),
(8, 'No era lo que esperaba.', 2, 1, '2025-08-24', 4),
(9, 'Perfecto para lo que necesitaba.', 5, 2, '2025-08-25', 2),
(10, 'Funciona bien, aunque el empaque venía abierto.', 3, 8, '2025-08-26', 6),
(11, 'Demasiado caro para lo que ofrece.', 2, 1, '2025-08-27', 8),
(12, 'Lo uso todos los días, muy satisfecho.', 5, 2, '2025-08-28', 3);
	   
-- INSERT INTO verification (id, date, video_url, artist_id, status)
-- VALUES (1, '2025-01-14 00:00:00', '/verification_media/mariaBecerra_verification.mp4', 4, 'PENDING'),
-- 	   (2, '2025-01-14 00:00:00', '/verification_media/litKillah_verification.mp4', 5, 'PENDING');
	   
INSERT INTO event (
    id, date, description, image, location, more_info, name, artist_id, latitude, longitude
)
VALUES
(
    1,
    '2025-12-07 21:00:00',
    'Vive una noche inolvidable con Duki presentando su gira "Desde el Cartel". Un show cargado de energía, con sus mayores éxitos y una puesta en escena única.',
    '/event_media/DukiCartel.jpg',
    'Movistar Arena, Humboldt 450, CABA, Argentina',
    'https://linktr.ee/duki',
    'Duki en Concierto - Desde el Cartel',
    3,
    -34.593301,
    -58.437725
),
(
    2,
    '2025-12-13 20:30:00',
    'Duki se presenta en el Boombastic Festival con un espectáculo que combina rap, trap y una producción audiovisual de primer nivel.',
    '/event_media/DukiBoombastic.jpg',
    'IFEMA Madrid, Av. del Partenón 5, Madrid, España',
    'https://linktr.ee/duki',
    'Duki en Boombastic Festival',
    3,
    40.467236,
    -3.616949
),
(
    3,
    '2025-12-12 22:00:00',
    'No te pierdas el show más esperado del año: Duki en el American Airlines Arena con todos sus hits y sorpresas especiales.',
    '/event_media/DukiAmeri.jpg',
    'Kaseya Center (ex American Airlines Arena), 601 Biscayne Blvd, Miami, USA',
    'https://linktr.ee/duki',
    'Duki Tour USA',
    3,
    25.781401,
    -80.187112
),
(
    4,
    '2025-10-25 21:30:00',
    'Duki hace historia con su presentación en el Estadio Santiago Bernabéu. Una noche única que quedará grabada en la memoria de miles de fans.',
    '/event_media/DukiBernabeu.jpg',
    'Estadio Santiago Bernabéu, Av. de Concha Espina 1, Madrid, España',
    'https://linktr.ee/duki',
    'Duki en el Bernabéu',
    3,
    40.453108,
    -3.688808
),
(
    5,
    '2025-11-21 20:00:00',
    'Duki continúa su Ameri World Tour en el Movistar Arena de Bogotá, con un espectáculo de trap y producción audiovisual poderosa.',
    '/event_media/DukiBogota.png',
    'Movistar Arena, Bogotá, Colombia',
    'https://www.duki.com',
    'Duki en Bogotá',
    3,
    4.6482837,
    -74.2478934
),

(
    6,
    '2025-11-12 21:00:00',
    'María Becerra presenta un show 360° en el Estadio River Plate, rompiendo récords como primera mujer argentina con cuatro shows ahí.',
    '/event_media/MariaBecerraRiver12.png',
    'Estadio River Plate, Buenos Aires, Argentina',
    'https://www.allaccess.com.ar',
    'María Becerra – River 360° (12 Nov)',
    4,
    -34.5450,
    -58.4497
),

(
    7,
    '2025-11-15 21:00:00',
    'Segunda noche del show 360° de María Becerra en River, una experiencia inmersiva con sus alter egos Shanina, JoJo, Maite y Gladys.',
    '/event_media/MariaBecerraRiver13.png',
    'Estadio River Plate, Buenos Aires, Argentina',
    'https://www.allaccess.com.ar',
    'María Becerra – River 360° (15 Nov)',
    4,
    -34.5450,
    -58.4497
),

(
    8,
    '2025-11-27 21:00:00',
    'Trueno encabeza el Holika Festival 2025 en Calahorra bajo el lema "The rise of the empire".',
    '/event_media/TruenoHolika.png',
    'Calahorra, La Rioja, España',
    'https://www.holikafestival.com',
    'Trueno en Holika Festival',
    6,
    42.3164,
    -2.4824
),

(9, '2025-11-04 21:00:00',
 'María Becerra en concierto en el WiZink Center, parte de su gira mundial.',
 '/event_media/MariaMadrid.png',
 'WiZink Center, Madrid, España',
 'https://www.livenation.es',
 'María Becerra en Madrid', 4, 40.4320, -3.6110),

-- 2: Aranda de Duero Septiembre
(10, '2025-11-19 22:00:00',
 'Actuación gratuita en las Fiestas Patronales Virgen de las Viñas en Aranda de Duero.',
 '/event_media/MariaAranda.png',
 'Recinto Ferial, Aranda de Duero, España',
 'https://los40.com',
 'María Becerra en Aranda de Duero', 4, 41.6623, -3.6889),


(11, '2025-11-26 23:00:00',
 'Participación en LAVA LIVE Lanzarote Music Festival, día final (26 de noviembre).',
 '/event_media/MariaLanzarote.png',
 'Parcela de Agramar, Lanzarote, España',
 'https://lavalivefestival.com',
 'María Becerra en LAVA LIVE Lanzarote', 4, 28.9650, -13.5500),


(12, '2025-11-12 21:00:00',
 'Actuación en Coca‑Cola Music Experience, gran festival en Madrid.',
 '/event_media/MariaCocacola.png',
 'Madrid, España',
 'https://cocacolamusice.com',
 'María Becerra en Coca‑Cola Music Experience', 4, 40.4168, -3.7038),
 
 (13, '2025-11-14 20:00:00',
 'Lit Killah en concierto en el Centro de Convenciones Teleférico, Quito.',
 '/event_media/LitQuito.png',
 'Centro de Convenciones Teleférico, Quito, Ecuador',
 'https://shazam.com',
 'Lit Killah en Quito', 5, -0.1807, -78.4678),


(14, '2025-11-20 21:00:00',
 'Actuación en el Festival Zuera Sound, Zaragoza.',
 '/event_media/LitZaragoza.png',
 'Zuera Sound, Zaragoza, España',
 'https://galaxymusicpromo.com',
 'Lit Killah en Zaragoza', 5, 41.6561, -0.8773),


(15, '2025-11-18 22:00:00',
 'Lit Killah presente en el Boombastic Festival, La Morgal (Llanera).',
 '/event_media/LitBoombastic.png',
 'La Morgal, Llanera, Asturias, España',
 'https://los40.com',
 'Lit Killah en Boombastic Festival', 5, 43.4179, -5.6936),


(16, '2025-11-26 22:30:00',
 'Cierre de gira en el festival Boombastic – Costa del Sol.',
 '/event_media/LitCostaSol.png',
 'Costa del Sol, Málaga, España',
 'https://galaxymusicpromo.com',
 'Lit Killah en Costa del Sol', 5, 36.7202, -4.4203),
 
(17, '2025-10-15 21:00:00',
 'Trueno en concierto en Madrid, primera gira tras colaboración con María Becerra.',
 '/event_media/TruenoMadrid.png',
 'Madrid, España',
 'https://los40.com',
 'Trueno en Madrid – Marzo', 6, 40.4168, -3.7038),

(18, '2025-05-10 20:00:00',
 'Trueno en Festival Hip Hop Argentina, show destacado en Buenos Aires.',
 '/event_media/TruenoBA.png',
 'Buenos Aires, Argentina',
 'https://examplefestival.com',
 'Trueno en Buenos Aires', 6, -34.6037, -58.3816),

(19, '2025-11-05 20:30:00',
 'Concierto urbano de Trueno en Bogotá, parte de gira latinoamericana.',
 '/event_media/TruenoBogota.png',
 'Bogotá, Colombia',
 'https://examplefest.com',
 'Trueno en Bogotá', 6, 4.7110, -74.0721);

INSERT INTO returns (id, reason, return_date) VALUES 
(1,'La camiseta no es de mi talla', '2025-10-01');
		
INSERT INTO orders (
    id,
    identifier,
    total_price,
    status,
    address_line1,
    address_line2,
    postal_code,
    city,
    country,
    user_id,
    created_date,
    email,
    phone,
    payment_intent,
    last_update_date_time,
    return_request_id
) VALUES
(1, 100001, 55.00, 'PAID', '123 Main Street', 'Apt 4B', '12345', 'Metropolis', 'ESP', 1, '2024-01-15', 'customer1@example.com', '+1234567890', 'pi_abc123', '2025-01-15', NULL),
(2, 100002, 80.00, 'IN_PREPARATION', '456 Elm Avenue', NULL, '67890', 'Gotham', 'USA', 2, '2024-02-03', 'customer2@example.com', '+1987654321', 'pi_def456', '2025-02-03', NULL),
(3, 100003, 120.00, 'RETURN_REQUEST', '789 Oak Blvd', 'Suite 12', '54321', 'Star City', 'USA', 3, '2023-02-18', 'customer3@example.com', '+1122334455', 'pi_ghi789', '2025-02-18', 1),
(4, 100004, 35.00, 'PAID', '123 Main Street', 'Apt 4B', '12345', 'Metropolis', 'USA', 8, '2025-03-05', 'dummyuser@email.com', '+1234567890', 'pi_wyz987', '2025-03-05', NULL),
(5, 100005, 60.00, 'DELIVERED', '123 Main Street', 'Apt 4B', '12345', 'Metropolis', 'ESP', 1, '2025-03-20', 'customer1@example.com', '+1234567890', 'pi_jkl111', '2025-03-21', NULL),
(6, 100006, 78.00, 'PAID', '456 Elm Avenue', NULL, '67890', 'Gotham', 'USA', 2, '2025-04-02', 'customer2@example.com', '+1987654321', 'pi_mno222', '2025-04-02', NULL),
(7, 100007, 95.00, 'IN_PREPARATION', '789 Oak Blvd', 'Suite 12', '54321', 'Star City', 'USA', 3, '2025-04-15', 'customer3@example.com', '+1122334455', 'pi_pqr333', '2025-04-15', NULL),
(8, 100008, 40.00, 'DELIVERED', '123 Main Street', 'Apt 4B', '12345', 'Metropolis', 'FRA', 8, '2025-05-01', 'dummyuser@email.com', '+1234567890', 'pi_stu444', '2025-05-02', NULL),
(9, 100009, 115.00, 'PAID', '789 Oak Blvd', 'Suite 12', '54321', 'Star City', 'FRA', 3, '2025-05-18', 'customer3@example.com', '+1122334455', 'pi_vwx555', '2025-05-19', NULL),
(10, 100010, 82.00, 'DELIVERED', '456 Elm Avenue', NULL, '67890', 'Gotham', 'USA', 2, '2025-06-05', 'customer2@example.com', '+1987654321', 'pi_yza666', '2025-06-06', NULL),
(11, 100011, 58.00, 'PAID', '123 Main Street', 'Apt 4B', '12345', 'Metropolis', 'FRA', 1, '2025-06-22', 'customer1@example.com', '+1234567890', 'pi_bcd777', '2025-06-23', NULL),
(12, 100012, 110.00, 'PAID', '789 Oak Blvd', 'Suite 12', '54321', 'Star City', 'USA', 3, '2025-07-10', 'customer3@example.com', '+1122334455', 'pi_efg888', '2025-07-15', NULL),
(13, 100013, 76.00, 'IN_PREPARATION', '456 Elm Avenue', NULL, '67890', 'Gotham', 'FRA', 2, '2025-07-27', 'customer2@example.com', '+1987654321', 'pi_hij999', '2025-07-27', NULL),
(14, 100014, 50.00, 'PAID', '123 Main Street', 'Apt 4B', '12345', 'Metropolis', 'USA', 8, '2025-08-05', 'dummyuser@email.com', '+1234567890', 'pi_klm000', '2025-08-05', NULL),
(15, 100015, 125.00, 'DELIVERED', '789 Oak Blvd', 'Suite 12', '54321', 'Star City', 'FRA', 3, '2025-08-22', 'customer3@example.com', '+1122334455', 'pi_nop111', '2025-08-23', NULL),
(16, 100016, 33.00, 'PAID', '456 Elm Avenue', NULL, '67890', 'Gotham', 'USA', 2, '2025-09-01', 'customer2@example.com', '+1987654321', 'pi_qrs222', '2025-09-01', NULL),
(17, 100017, 79.00, 'IN_PREPARATION', '123 Main Street', 'Apt 4B', '12345', 'Metropolis', 'USA', 1, '2025-09-16', 'customer1@example.com', '+1234567890', 'pi_tuv333', '2025-09-16', NULL),
(18, 100018, 105.00, 'IN_PREPARATION', '789 Oak Blvd', 'Suite 12', '54321', 'Star City', 'USA', 3, '2025-10-02', 'customer3@example.com', '+1122334455', 'pi_wxy444', '2025-10-02', NULL),
(19, 100019, 88.00, 'PAID', '456 Elm Avenue', NULL, '67890', 'Gotham', 'USA', 2, '2025-10-18', 'customer2@example.com', '+1987654321', 'pi_zab555', '2025-10-18', NULL),
(20, 100020, 47.00, 'DELIVERED', '123 Main Street', 'Apt 4B', '12345', 'Metropolis', 'ESP', 8, '2025-10-30', 'dummyuser@email.com', '+1234567890', 'pi_cde666', '2025-10-30', NULL);



INSERT INTO order_items (
    id,
    name,
    color,
    price,
    product_id,
    quantity,
    size,
    order_id
) VALUES
-- Pedido 1 (55.00)
(1, 'Duki Flow Tee', 'Blanco', 55.00, 1, 1, 'M', 1),
(2, 'Duki Flow Tee', 'Rojo', 40.00, 1, 2, 'L', 2),
(3, 'Duki Flow Tee', 'Negro', 60.00, 1, 2, 'XL', 3),
(4, 'Duki Flow Tee', 'Blanco', 35.00, 1, 1, 'M', 4),
(5, 'Duki Flow Tee', 'Rojo', 30.00, 1, 2, 'S', 5),
(6, 'Duki Flow Tee', 'Negro', 39.00, 1, 2, 'L', 6),
(7, 'Duki Flow Tee', 'Blanco', 47.50, 1, 2, 'XL', 7),
(8, 'Duki Flow Tee', 'Rojo', 40.00, 1, 1, 'M', 8),
(9, 'Duki Flow Tee', 'Negro', 57.50, 1, 2, 'L', 9),
(10, 'Duki Flow Tee', 'Blanco', 41.00, 1, 2, 'M', 10),
(11, 'Duki Flow Tee', 'Rojo', 29.00, 1, 2, 'S', 11),
(12, 'Duki Flow Tee', 'Negro', 55.00, 1, 2, 'L', 12),
(13, 'Duki Flow Tee', 'Blanco', 38.00, 1, 2, 'M', 13),
(14, 'Duki Flow Tee', 'Rojo', 50.00, 1, 1, 'XL', 14),
(15, 'Duki Flow Tee', 'Negro', 62.50, 1, 2, 'L', 15),
(16, 'Duki Flow Tee', 'Blanco', 33.00, 1, 1, 'M', 16),
(17, 'Duki Flow Tee', 'Rojo', 39.50, 1, 2, 'S', 17),
(18, 'Duki Flow Tee', 'Negro', 35.00, 1, 3, 'L', 18),
(19, 'Duki Flow Tee', 'Blanco', 44.00, 1, 2, 'XL', 19),
(20, 'Duki Flow Tee', 'Rojo', 47.00, 1, 1, 'M', 20);



INSERT INTO orders_items (
    order_id,
    items_id
) VALUES
(1, 1),
(2, 2),
(3, 3),
(4, 4),
(5, 5),
(6, 6),
(7, 7),
(8, 8),
(9, 9),
(10, 10),
(11, 11),
(12, 12),
(13, 13),
(14, 14),
(15, 15),
(16, 16),
(17, 17),
(18, 18),
(19, 19),
(20, 20);

INSERT INTO Collection_Products (collection_id, products_id) VALUES
(1,3),
(1,9);

INSERT INTO User_product(id, created_at, name, num_votes, status, user_id) VALUES (1,'2025-10-06 20:26:49.736', 'MiFirstProduct', 0, 1, 2);
INSERT INTO User_product_Images(user_product_id, images) VALUES
(1,'/userProduct_media/Camiseta1-modelo.png'),
(1,'/userProduct_media/Camiseta1-modelo2.png');

SELECT setval('product_id_seq', (SELECT MAX(id) FROM product));
SELECT setval('users_id_seq', (SELECT MAX(id) FROM users));
SELECT setval('category_id_seq', (SELECT MAX(id) FROM category));
SELECT setval('rating_id_seq', (SELECT MAX(id) FROM rating));
SELECT setval('verification_id_seq', (SELECT MAX(id) FROM verification));
SELECT setval('event_id_seq', (SELECT MAX(id) FROM event));
SELECT setval('orders_id_seq', (SELECT MAX(id) FROM orders));
SELECT setval('order_items_id_seq', (SELECT MAX(id) FROM order_items));
SELECT setval('shopping_cart_id_seq', (SELECT MAX(id) FROM shopping_cart));
SELECT setval('returns_id_seq', (SELECT MAX(id) FROM returns));
SELECT setval('collection_id_seq', (SELECT MAX(id) FROM collection));
SELECT setval('user_product_id_seq', (SELECT MAX(id) FROM User_product));









