package com.vzap.trytons.shared.config;

import com.vzap.trytons.filter.AuthFilter;
import com.vzap.trytons.auth.resource.AuthResource;
import com.vzap.trytons.shared.resource.ProtectedResource;
import jakarta.ws.rs.ApplicationPath;
import jakarta.ws.rs.core.Application;

import java.util.Set;

@ApplicationPath("/api")
public class RestApplication extends Application {
}