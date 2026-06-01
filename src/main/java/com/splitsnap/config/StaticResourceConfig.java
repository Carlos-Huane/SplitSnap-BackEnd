package com.splitsnap.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Expone la carpeta local {@code uploads/} (donde UserService guarda los avatares)
 * como recurso estatico bajo la ruta {@code /uploads/**}.
 *
 * <p>Sin este handler, las peticiones GET /uploads/avatars/xxx.png caen al filtro
 * de Spring Security: la etiqueta &lt;img&gt; del navegador no manda Authorization,
 * asi que la respuesta seria 403. SecurityConfig ya permite /uploads/** sin auth.</p>
 *
 * <p><b>Nota Railway:</b> el filesystem es efimero. Cada redeploy borra los archivos
 * subidos. Para produccion real conviene migrar a S3/Cloudinary/Supabase Storage.</p>
 */
@Configuration
public class StaticResourceConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadsPath = Paths.get("uploads").toAbsolutePath();
        String location = uploadsPath.toUri().toString();

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(location)
                .setCachePeriod(3600);
    }
}
