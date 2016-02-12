package com.beijunyi.parallelgit.web.security;

import javax.servlet.annotation.WebFilter;

import org.apache.shiro.web.servlet.ShiroFilter;

@WebFilter("/*")
public class AuthenticationFilter extends ShiroFilter {
}
