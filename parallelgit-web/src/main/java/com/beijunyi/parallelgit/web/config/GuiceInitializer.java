package com.beijunyi.parallelgit.web.config;

import javax.servlet.annotation.WebFilter;

import com.google.inject.servlet.GuiceFilter;

@WebFilter(urlPatterns = "/*")
public class GuiceInitializer extends GuiceFilter {
}
