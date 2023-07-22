package com.iskool.dao;

import java.util.List;
import java.util.UUID;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.iskool.entity.Role;
import com.iskool.enumeration.RoleType;


@Repository
public class RoleDAO implements IRoleDAO {
	
	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public void save(Role obj) {
		Session session = sessionFactory.getCurrentSession();
		session.save(obj);
	}

	@Override
	public void update(Role obj) {
		Session session = sessionFactory.getCurrentSession();
		session.update(obj);
	}
	
	@Override
	public void saveOrUpdate(Role obj) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(obj);
	}

	@Override
	public List<Role> getAll() {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(Role.class);
		crit.add(Restrictions.ne("roleName", "ROLE_SUPER_ADMIN"));
		List<Role> list = crit.list();
		return list;
	}
	
	@Override
	public Role get(UUID id) {
		Session session = sessionFactory.getCurrentSession();
		Role obj = session.get(Role.class, id);
		return obj;
	}

	@Override
	public Role getByRoleType(RoleType roleType) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(Role.class);
		crit.add(Restrictions.ne("roleName", roleType.toString()));
		List<Role> list = crit.list();
		if(list != null) {
			return list.get(0);
		}
		return null;
	}
	  

}
