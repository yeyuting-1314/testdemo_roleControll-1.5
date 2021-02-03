package testdemo.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.hierarchicalroles.RoleHierarchy;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.CredentialsExpiredException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.access.intercept.FilterSecurityInterceptor;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import testdemo.config.handler.AuthenticationAccessDeniedHandler;
import testdemo.config.handler.SimpleAuthenticationEntryPoint;
import testdemo.system.service.UserService;
import testdemo.system.service.impl.UserServiceImpl;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yeyuting
 * @create 2021/1/28
 */
//将自定义FilterInvocationSecurityMetadataSource和自定义AccessDecisionManager配置到Spring Security的配置类中
@Configuration
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {
    @Autowired
    UserServiceImpl userDetailsService ;
    @Autowired
    private AuthenticationFailureHandler customAuthenticationFailureHandler;
    @Autowired
    private AuthenticationSuccessHandler customAuthenticationSuccessHandler ;
    @Autowired
    AuthenticationAccessDeniedHandler authenticationAccessDeniedHandler;
    @Autowired
    SimpleAuthenticationEntryPoint simpleAuthenticationEntryPoint;
    /**
     * 配置角色继承关系
     *
     * @return
     */
    @Bean
    RoleHierarchy roleHierarchy(){
        RoleHierarchyImpl roleHierarchy = new RoleHierarchyImpl() ;
        String hierarchy = "ROLE_SUPERADMIN > ROLE_ADMIN > ROLE_USER" ;
        roleHierarchy.setHierarchy(hierarchy) ;
        return roleHierarchy ;
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService).passwordEncoder(new BCryptPasswordEncoder()) ;

    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        //引入数据库对资源访问操作
        http.authorizeRequests()
                .withObjectPostProcessor(new ObjectPostProcessor<FilterSecurityInterceptor>() {
                    @Override
                    public <O extends FilterSecurityInterceptor> O postProcess(O object) {
                        object.setSecurityMetadataSource(cfisms());
                        object.setAccessDecisionManager(cadm());
                        return object ;
                    }
                })
                .antMatchers("/userLogin").permitAll()
                // 所有访问该应用的http请求都要通过身份认证才可以访问
                .anyRequest().authenticated()
                .and().httpBasic()
                .and()
                .csrf().disable()
                // 指定登陆URL
                .formLogin()
               //页面默认访问页面重命名
                .loginProcessingUrl("/userLogin")
                //如果身份验证通过并且也具有相关权限时执行此操作
                .successHandler(customAuthenticationSuccessHandler)
                //如果身份验证失败则执行此代码逻辑，提示用户先进行登陆操作，用于 用户身份验证
                .failureHandler(customAuthenticationFailureHandler)
                .and()
                //用户访问通过但是不具备相关权限时执行此代码逻辑
                .exceptionHandling().accessDeniedHandler(authenticationAccessDeniedHandler)
                //用户未进行身份验证就访问系统页面时候会提示用户先进行登录操作，才能顺利进行后续操作，这是一个大入口
                .authenticationEntryPoint(simpleAuthenticationEntryPoint) ;
                //这里暂时通过session对应的cookie判断用户身份是否进行了验证  在后面完善中加入redis集成后再将瑟斯哦那禁用
                // 不需要session
                //.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
    }


    /**
     * 自定义的FilterInvocationSecurityMetadataSource
     * 将访问当前资源的URL与数据库中访问该资源的URL进行匹配
     *
     * @return
     */
    @Bean
    FilterInvocationSecurityMetadataSource cfisms() {
        return new FilterInvocationSecurityMetadataSource();
    }

    /**
     * 自定义的AccessDecisionManager
     * 判断登录用户是否具备访问当前URL所需要的角色
     *
     * @return
     */
    @Bean
    AccessDecisionManager cadm() {
        return new AccessDecisionManager();
    }


}
