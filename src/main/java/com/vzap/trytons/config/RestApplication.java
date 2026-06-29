package com.vzap.trytons.config;

import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.resource.AuthResource;
import com.vzap.trytons.resource.ProtectedResource;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.Set;

@ApplicationPath("/api")
public class RestApplication extends Application {
}