package com.example.data.model

data class BindingType(
    val id: String,
    val name: String,
    val subtitle: String,
    val description: String,
    val basePrice: Double,
    val defaultPages: Int = 120,
    val durability: String,
    val openingAngle: String,
    val recommendedUses: List<String>,
    val defaultCoverMaterial: String,
    val defaultColorHex: Long = 0xFF5C3317,
    val spineType: SpineType = SpineType.ROUNDED,
    val supportsFoil: Boolean = true,
    val hasRibbon: Boolean = true,
    val hasCornerGuards: Boolean = true,
    val difficulty: String = "Intermedio"
)

enum class SpineType {
    FLAT,
    ROUNDED,
    EXPOSED_COPTIC,
    JAPANESE_STAB,
    SPIRAL_WIRE
}

object PredefinedBindingTypes {
    val list = listOf(
        BindingType(
            id = "tapa_dura",
            name = "Tapa Dura Clásica",
            subtitle = "Encuadernación Cartoné / Holandesa",
            description = "Estructura sólida con cartón prensado de 2.5mm forrado en tela de lino o papel marmoleado. Lomo redondeado tradicional con cabezadas tejidas a mano y cinta señaladora.",
            basePrice = 28.0,
            defaultPages = 160,
            durability = "Máxima (décadas)",
            openingAngle = "180° plano",
            recommendedUses = listOf("Libros de autor", "Tesis", "Álbumes familiares", "Ediciones de lujo"),
            defaultCoverMaterial = "Tela de Lino Natural",
            defaultColorHex = 0xFF2D5A43, // Emerald green
            spineType = SpineType.ROUNDED,
            supportsFoil = true,
            hasRibbon = true,
            hasCornerGuards = true,
            difficulty = "Alta"
        ),
        BindingType(
            id = "cuero_artesanal",
            name = "Cuero Artesanal Antiguo",
            subtitle = "Encuadernación de Lujo en Piel",
            description = "Encuadernado completo en cuero vacuno curtido vegetal. Nervios en relieve en el lomo, golpe seco o dorado con pan de oro, guardas marmoleadas al agua y esquineros de bronce.",
            basePrice = 45.0,
            defaultPages = 200,
            durability = "Vitalicia / Museo",
            openingAngle = "180° plano",
            recommendedUses = listOf("Grimorios", "Biblias", "Libros de firmas", "Proyectos de coleccionista"),
            defaultCoverMaterial = "Cuero Vacuno Envejecido",
            defaultColorHex = 0xFF4A2A18, // Rich leather brown
            spineType = SpineType.ROUNDED,
            supportsFoil = true,
            hasRibbon = true,
            hasCornerGuards = true,
            difficulty = "Maestro"
        ),
        BindingType(
            id = "copta",
            name = "Costura Copta Expuesta",
            subtitle = "Costura ancestral a la vista",
            description = "Libro sin lomo rígido donde la costura trenzada con hilo de lino encerado queda bellamente visible. Permite apertura de 360° totalmente plana.",
            basePrice = 24.0,
            defaultPages = 120,
            durability = "Muy Alta",
            openingAngle = "360° total",
            recommendedUses = listOf("Cuadernos de boceto", "Acuarela", "Bullet journals", "Artbooks"),
            defaultCoverMaterial = "Madera o Cartón Prensado Forrado",
            defaultColorHex = 0xFFB85D38, // Terracotta
            spineType = SpineType.EXPOSED_COPTIC,
            supportsFoil = false,
            hasRibbon = false,
            hasCornerGuards = true,
            difficulty = "Media"
        ),
        BindingType(
            id = "japonesa",
            name = "Costura Japonesa (Watoji)",
            subtitle = "Encuadernación Oriental Noble",
            description = "Técnica tradicional de cuatro orificios o estilo Asanoha (hoja de cáñamo). Hojas dobladas en fuelle unidas lateralmente con cordón de seda o lino.",
            basePrice = 22.0,
            defaultPages = 80,
            durability = "Media - Alta",
            openingAngle = "Lateral 120°",
            recommendedUses = listOf("Poemarios", "Menús gourmet", "Portafolios", "Caligrafía y grabado"),
            defaultCoverMaterial = "Papel Japonés Washi / Tela Seda",
            defaultColorHex = 0xFF6D213C, // Burgundy / Wine
            spineType = SpineType.JAPANESE_STAB,
            supportsFoil = true,
            hasRibbon = false,
            hasCornerGuards = true,
            difficulty = "Media"
        ),
        BindingType(
            id = "rustica_cosida",
            name = "Rústica Cosida (Softcover)",
            subtitle = "Cubierta flexible de alto gramaje",
            description = "Pliegos cosidos con hilo vegetal y lomo cubierto con cartulina texturizada con solapas. Ligera, flexible y muy resistente al deshojado.",
            basePrice = 16.0,
            defaultPages = 100,
            durability = "Media",
            openingAngle = "160°",
            recommendedUses = listOf("Manuales", "Ediciones cortas", "Zines de autor", "Novelas"),
            defaultCoverMaterial = "Cartulina Kraft 300g",
            defaultColorHex = 0xFF7A6B5D, // Kraft Grey/Brown
            spineType = SpineType.FLAT,
            supportsFoil = true,
            hasRibbon = false,
            hasCornerGuards = false,
            difficulty = "Básica"
        ),
        BindingType(
            id = "wire_o",
            name = "Wire-O Doble Anillo Metálico",
            subtitle = "Espiral doble artesanal",
            description = "Tapas duras protectoras con perforación cuadrada y alambre doble bronce/negro. Ideal para pasar páginas rápidamente.",
            basePrice = 14.0,
            defaultPages = 100,
            durability = "Media",
            openingAngle = "360° continuo",
            recommendedUses = listOf("Agendas anuales", "Recetarios de cocina", "Catálogos comerciales"),
            defaultCoverMaterial = "Tapa Dura Laminada Mate",
            defaultColorHex = 0xFF354458, // Dark slate blue
            spineType = SpineType.SPIRAL_WIRE,
            supportsFoil = true,
            hasRibbon = true,
            hasCornerGuards = true,
            difficulty = "Básica"
        )
    )
}
