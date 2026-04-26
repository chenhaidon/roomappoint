package com.example.web.tools;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;

/**
 * WebMvc的配置
 */
@Configuration
public class InterceptorConfig implements WebMvcConfigurer {


    /**
     * 配置切面
     * @param registry
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {

        //获取当前上下文用户信息拦截器
        registry.addInterceptor(new CurrentUserInterceptor())
                .addPathPatterns("/**");
    }
    /**
     * 配置前后端跨域报错的问题
     * @param registry
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*");
    }

    /**
     * 资源的配置处理
     */
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadsDir = Path.of(System.getProperty("user.dir"), "uploads").toAbsolutePath().normalize();
        String uploadsLocation = uploadsDir.toUri().toString();
        if (!uploadsLocation.endsWith("/")) {
            uploadsLocation += "/";
        }

        // 标准上传访问路径：/uploads/{time}/{filename}
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadsLocation)
                .setCachePeriod(3600);

        // 兼容旧数据里的 "/{time}/{filename}" 形式地址
        registry.addResourceHandler("/*/**")
                .addResourceLocations(uploadsLocation)
                .setCachePeriod(3600);

        // 其他静态资源仍从 classpath:/static/ 提供
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/")
                .setCachePeriod(3600);
    }
}
