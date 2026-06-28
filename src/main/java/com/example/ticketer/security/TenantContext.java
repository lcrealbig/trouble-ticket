package com.example.ticketer.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Setter
@Getter
@Component
@RequestScope
public class TenantContext {

    private String tenantId;
    public void clear() {
        this.tenantId = null;
    }
}