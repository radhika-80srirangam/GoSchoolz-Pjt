package com.iskool.dao;

import java.util.List;
import java.util.UUID;

import com.iskool.entity.Role;
import com.iskool.enumeration.RoleType;

public interface IRoleDAO {
	public void save(Role obj);
	public void saveOrUpdate(Role obj);
	public void update(Role obj);
	public Role get(UUID id);
	public List<Role> getAll();
	public Role getByRoleType(RoleType roleType);
}
