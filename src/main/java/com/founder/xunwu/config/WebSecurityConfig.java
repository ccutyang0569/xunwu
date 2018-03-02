package com.founder.xunwu.config;

import com.founder.xunwu.security.AuthProvider;
import com.founder.xunwu.security.LoginAuthFailHandler;
import com.founder.xunwu.security.LoginUrlEntryPoint;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * @program: xunwu
 * @description: SpringSecurity配置类
 * @author: YangMing
 * @create: 2018-01-30 19:51
 **/
@EnableWebSecurity
@EnableGlobalMethodSecurity
public class WebSecurityConfig extends WebSecurityConfigurerAdapter{

    @Override
    protected void configure(HttpSecurity http) throws Exception {

        // 资源访问权限
        http.authorizeRequests()
                .antMatchers("/admin/login").permitAll() // 管理员登录入口
                .antMatchers("/static/**").permitAll() // 静态资源
                .antMatchers("/user/login").permitAll() // 用户登录入口
                .antMatchers("/admin/**").hasRole("ADMIN")
                .antMatchers("/user/**").hasAnyRole("ADMIN", "USER")
                .antMatchers("/api/user/**").hasAnyRole("ADMIN",
                "USER")
                .and()
                .formLogin()
                .loginProcessingUrl("/login") // 配置角色登录处理入口
                .failureHandler(authFailHandler())
                .and()
                .logout()
                .logoutUrl("/logout")
                .logoutSuccessUrl("/logout/page")
                .deleteCookies("JSESSIONID")
                .invalidateHttpSession(true)
                .and()
                .exceptionHandling()
                .authenticationEntryPoint(urlEntryPoint())
                .accessDeniedPage("/403");

        http.csrf().disable();
        http.headers().frameOptions().sameOrigin();
    }
    /**
     *
     * 自定义认证策略
     */
    @Autowired
    public void configGlobal(AuthenticationManagerBuilder authenticationManagerBuilder) throws Exception {
        //authenticationManagerBuilder.inMemoryAuthentication().withUser("admin").password("admin").roles("ADMIN").and();
      authenticationManagerBuilder.authenticationProvider(authProvider()).eraseCredentials(true);

    }
    @Bean
    public AuthenticationProvider authProvider() {

          return new AuthProvider();
    }
    @Bean
    public LoginUrlEntryPoint urlEntryPoint(){

        return new LoginUrlEntryPoint("/user/login");
    }

//    @Override
//    public void configure(WebSecurity web) throws Exception {
//        //解决静态资源被拦截的问题
//        web.ignoring().antMatchers("/static/**").antMatchers("/css/**");
//    }
    @Bean
    public LoginAuthFailHandler authFailHandler(){
        return new LoginAuthFailHandler(urlEntryPoint());
    }
}
