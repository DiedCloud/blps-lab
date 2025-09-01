package com.example.blps.config.security;

import com.example.blps.dao.OwnedObject;
import com.example.blps.dao.repository.CommentRepository;
import com.example.blps.dao.repository.VideoInfoRepository;
import com.example.blps.dao.repository.model.Permission;
import com.example.blps.dao.repository.model.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.security.access.PermissionEvaluator;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.Map;
import java.util.NoSuchElementException;

@Component
public class PermissionEvaluatorImpl implements PermissionEvaluator {
    private final Map<String, JpaRepository<? extends OwnedObject, Long>> registeredEntityTypes;

    @Autowired
    public PermissionEvaluatorImpl(VideoInfoRepository videoRepo, CommentRepository commentRepo) {
        this.registeredEntityTypes = Map.ofEntries(
                Map.entry("VideoInfo", videoRepo),
                Map.entry("Comment", commentRepo)
        );
    }

    @Override
    public boolean hasPermission(Authentication auth, Object targetDomainObject, Object permission) {
        if (auth == null) return false;
        if (!(auth.getPrincipal() instanceof User principal)) return false;

        if (targetDomainObject instanceof OwnedObject) {
            User owner = ((OwnedObject) targetDomainObject).getOwner();
            if (owner.getId().equals(principal.getId()))
                return true;
        }

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
        if (auth == null) return false;
        if (!(auth.getPrincipal() instanceof User)) return false;
        if (!(targetId instanceof Long)) return false;
        if (targetType == null) return false;
        if (!(permission instanceof String || permission instanceof Permission)) return false;

        OwnedObject targetDomainObject = null;
        if (registeredEntityTypes.containsKey(targetType)){
            JpaRepository<? extends OwnedObject, Long> repo = registeredEntityTypes.get(targetType);
            targetDomainObject = repo.findById((Long) targetId)
                    .orElseThrow(() -> new NoSuchElementException("Not found"));
        }

        return hasPermission(auth, targetDomainObject, permission);
    }
}
