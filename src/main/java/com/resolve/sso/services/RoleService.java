package com.resolve.sso.services;

import java.util.List;

import org.elasticsearch.client.security.user.privileges.Role;
import org.springframework.stereotype.Service;

@Service
public interface RoleService {

	public List<Role> getRoles(String... roleNames);
}
