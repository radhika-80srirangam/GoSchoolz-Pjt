package com.iskool.dao;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.hibernate.Criteria;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.LogicalExpression;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import com.iskool.dto.UserDto;
import com.iskool.entity.User;
import com.iskool.enumeration.Status;

@Repository
public class UserDAO implements IUserDAO {

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public void save(User obj) {
		Session session = sessionFactory.getCurrentSession();
		session.save(obj);
	}

	@Override
	public void update(User obj) {
		Session session = sessionFactory.getCurrentSession();
		session.update(obj);
	}

	@Override
	public void saveOrUpdate(User obj) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(obj);
	}

	@Override
	public List<User> getAll() {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(User.class);
		List<User> list = crit.list();
		return list;
	}

	@Override
	public User get(UUID id) {
		Session session = sessionFactory.getCurrentSession();
		User obj = session.get(User.class, id);
		return obj;
	}

	@Override
	public User getUserByUserName(String userName) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(User.class);
		crit.add(Restrictions.eq("userName", userName));
		List<User> userList = crit.list();
		if (null != userList && !userList.isEmpty()) {
			return userList.get(0);
		}
		return null;
	}

	@Override
	public List<User> getAll(UUID agentId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(User.class);
		crit.add(Restrictions.eq("agentObj.id", agentId));
		crit.add(Restrictions.eq("status", Status.ACTIVE));
		List<User> list = crit.list();
		return list;
	}

	@Override
	public void delete(UUID userId, UUID deletedBy) {
		Session session = sessionFactory.getCurrentSession();
		Query query = session.createQuery("UPDATE User set status='" + Status.INACTIVE
				+ "', deletedOn=now(), deletedBy='" + deletedBy + "' WHERE id = '" + userId + "'");
		query.executeUpdate();
	}

	@Override
	public List<UserDto> getActiveUsersByAgent(UUID agentId) {
		Session session = sessionFactory.getCurrentSession();
		String sqlQuery = "SELECT user_id,user_name,first_name,last_name FROM users where agent_id='" + agentId
				+ "' and status='ACTIVE' order by user_name asc";
		Query query = session.createSQLQuery(sqlQuery);
		List<Object[]> rows = query.list();
		List<UserDto> list = new ArrayList<UserDto>();
		UserDto obj = null;
		for (Object[] row : rows) {
			obj = new UserDto();
			obj.setId(UUID.fromString(row[0].toString()));
			obj.setUserName(row[1].toString());
			obj.setFirstName(row[2].toString());
			obj.setLastName(row[3].toString());
			list.add(obj);
		}
		return list;
	}

	@Override
	public User getUserByAgentId(UUID agentId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(User.class);
		crit.add(Restrictions.eq("agentObj.id", agentId));
		List<User> userList = crit.list();
		if (null != userList && !userList.isEmpty()) {
			return userList.get(0);
		}
		return null;
	}

	@Override
	public User getUserByUserNameOrEmail(String userNameOrEmail) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(User.class);
		LogicalExpression orExp = Restrictions.or(Restrictions.eq("userName", userNameOrEmail),
				Restrictions.eq("email", userNameOrEmail));
		crit.add(orExp);
		List<User> userList = crit.list();
		if (null != userList && !userList.isEmpty()) {
			return userList.get(0);
		}
		return null;
	}
}
