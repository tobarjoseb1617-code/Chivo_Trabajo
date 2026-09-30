package com.cherodevscode.chivo_trabajo.utils

/**
 * Configuración con 20 categorías profesionales oficiales en El Salvador
 * y sus respectivos sub-servicios específicos asociados para selección dinámica.
 */
object CategoriasConfig {

    // Mapa que relaciona cada categoría oficial con sus servicios específicos
    val mapaCategoriasServicios = mapOf(
        "Fontanería y Tuberías" to listOf(
            "Reparación de fugas",
            "Instalación de grifería",
            "Destape de tuberías",
            "Instalación de inodoros",
            "Mantenimiento cisterna"
        ),
        "Electricidad Residencial y Comercial" to listOf(
            "Cortos circuitos",
            "Instalación de lámparas",
            "Tableros eléctricos",
            "Tomas y apagadores",
            "Ventiladores de techo"
        ),
        "Albañilería y Construcción" to listOf(
            "Levantado de paredes",
            "Reparación de losas",
            "Cimentación y zapatas",
            "Enochechado y repello"
        ),
        "Pintura y Acabados" to listOf(
            "Pintura de interiores",
            "Pintura de fachadas",
            "Empaste de paredes",
            "Impermeabilización de muros"
        ),
        "Carpintería y Muebles" to listOf(
            "Fabricación de closets",
            "Reparación de puertas",
            "Muebles a medida",
            "Instalación de repisas"
        ),
        "Climatización y Aire Acondicionado" to listOf(
            "Mantenimiento de A/C",
            "Instalación de minisplit",
            "Carga de gas refrigerante",
            "Reparación de aires"
        ),
        "Cerrajería General" to listOf(
            "Apertura de puertas",
            "Cambio de chapas",
            "Instalación de cerrojos",
            "Duplicado de llaves"
        ),
        "Jardinería y Paisajismo" to listOf(
            "Poda de césped y árboles",
            "Diseño de jardines",
            "Sistema de riego",
            "Limpieza de terrenos"
        ),
        "Limpieza Profunda y Hogar" to listOf(
            "Limpieza post-construcción",
            "Lavado de muebles y alfombras",
            "Limpieza general profunda",
            "Sanitización de espacios"
        ),
        "Herrería y Soldadura" to listOf(
            "Fabricación de portones",
            "Reparación de barandales",
            "Estructuras metálicas",
            "Rejas de seguridad"
        ),
        "Impermeabilización de Techos" to listOf(
            "Aplicación de manto asfáltico",
            "Sellado de goteras",
            "Pintura impermeabilizante",
            "Limpieza de canales"
        ),
        "Reparación de Electrodomésticos" to listOf(
            "Reparación de lavadoras",
            "Refrigeración y neveras",
            "Estufas y hornos",
            "Microondas"
        ),
        "Tablaroca y Paneles de Yeso" to listOf(
            "Paredes de Tablaroca",
            "Cielos falsos",
            "Divisiones de oficina",
            "Nichos decorativos"
        ),
        "Instalación de Pisos y Azulejos" to listOf(
            "Colocación de cerámica",
            "Instalación de porcelanato",
            "Pisos laminados",
            "Zócalos"
        ),
        "Cámaras de Seguridad y Alarmas" to listOf(
            "Instalación de cámaras CCTV",
            "Configuración de DVR/NVR",
            "Sistemas de alarma",
            "Video porteros"
        ),
        "Fumigación y Control de Plagas" to listOf(
            "Control de roedores",
            "Fumigación de insectos",
            "Desinfección de virus",
            "Termitas y plagas de madera"
        ),
        "Energía Solar y Paneles" to listOf(
            "Instalación de paneles solares",
            "Mantenimiento de inversores",
            "Sistemas fotovoltaicos",
            "Baterías de respaldo"
        ),
        "Mudanzas y Transporte" to listOf(
            "Fletes locales",
            "Mudanzas de hogar",
            "Transporte de carga",
            "Embalaje de muebles"
        ),
        "Mantenimiento de Piscinas" to listOf(
            "Limpieza de filtros",
            "Control de cloro y pH",
            "Lavado de paredes de piscina",
            "Reparación de bombas"
        ),
        "Albañilería y Reformas" to listOf(
            "Remodelación de baños",
            "Ampliación de espacios",
            "Demolición de muros",
            "Acabados generales"
        )
    )

    // Lista plana con los nombres de las 20 categorías oficiales
    val listaCategorias = mapaCategoriasServicios.keys.toList()
}
