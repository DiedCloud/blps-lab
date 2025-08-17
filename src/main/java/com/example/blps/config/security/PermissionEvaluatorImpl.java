package com.example.blps.config.security;

import com.example.blps.dao.OwnedObject;
import com.example.blps.dao.repository.model.Permission;
import com.example.blps.dao.repository.model.User;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class PermissionEvaluatorImpl implements PermissionEvaluator {

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        if (auth == null) return false;

        String permName;
        if (permission instanceof String)
            permName = (String) permission;
        else if (permission instanceof Permission)
            permName = ((Permission) permission).getName();
        else
            return false;

        return auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(a -> a.equals(permName));
    }

    @Override
    public boolean hasPermission(Authentication auth, Serializable targetId, String targetType, Object permission) {
        if (auth == null ||
                !(auth.getPrincipal() instanceof User principal) ||
                !(targetId instanceof OwnedObject) ||
                !(permission instanceof String || permission instanceof Permission)
        ) {
            return false;
        }

        User owner = ((OwnedObject) targetId).getOwner();
        if (owner.getId().equals(principal.getId())) {
            return true;
        }

        return hasPermission(auth, null, permission);
    }
}
