package app.web.config;

import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.i18n.CookieLocaleResolver;
import org.springframework.web.servlet.i18n.LocaleChangeInterceptor;
import org.springframework.web.servlet.i18n.SessionLocaleResolver;

import java.nio.charset.StandardCharsets;
import java.util.Locale;

/**
 * Configuración para la internacionalización y resolución de idiomas en la aplicación.
 * Esta clase implementa {@link WebMvcConfigurer} para personalizar el comportamiento del MVC.
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    /**
     * Configura el origen de los mensajes para la internacionalización (i18n).
     *
     * @return un {@link MessageSource} que carga los mensajes desde un archivo de recursos.
     */
    @Bean
    public MessageSource messageSource() {
        ReloadableResourceBundleMessageSource messageSource = new ReloadableResourceBundleMessageSource();
        messageSource.setBasename("classpath:i18n/messages");
        messageSource.setDefaultEncoding(StandardCharsets.ISO_8859_1.name());
        return messageSource;
    }

    /**
     * Configura el resolutor de locales, que determina el idioma basado en una cookie.
     *
     * @return un {@link LocaleResolver} que utiliza cookies para mantener el idioma seleccionado.
     */
    @Bean
    public LocaleResolver localeResolver() {
        CookieLocaleResolver localeResolver = new CookieLocaleResolver();
        localeResolver.setDefaultLocale(new Locale("es"));
        localeResolver.setCookieName("lang");
        localeResolver.setCookieMaxAge(3600);
        return localeResolver;
    }

    /**
     * Configura un interceptor para permitir el cambio de idioma mediante un parámetro en las solicitudes.
     *
     * @return un {@link LocaleChangeInterceptor} que detecta el parámetro de cambio de idioma.
     */
    @Bean
    public LocaleChangeInterceptor localeChangeInterceptor() {
        LocaleChangeInterceptor lci = new LocaleChangeInterceptor();
        lci.setParamName("lang");
        return lci;
    }

    /**
     * Registra el interceptor para manejar los cambios de idioma en las solicitudes entrantes.
     *
     * @param registry el {@link InterceptorRegistry} utilizado para registrar interceptores.
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(localeChangeInterceptor());
    }
}