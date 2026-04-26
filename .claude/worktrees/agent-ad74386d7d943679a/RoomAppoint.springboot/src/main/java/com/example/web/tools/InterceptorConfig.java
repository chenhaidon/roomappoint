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

        // 兼容数据库里存的 "/<time>/<filename>" 形式的图片地址
        registry.addResourceHandler("/**")
                .addResourceLocations("classpath:/static/", uploadsLocation)
                .setCachePeriod(3600);

        // 同时也支持 "/uploads/**" 直链
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(uploadsLocation)
                .setCachePeriod(3600);
    }
}
