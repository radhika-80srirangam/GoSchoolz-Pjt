package com.iskool.dao;

import java.util.List;
import java.util.UUID;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.criterion.Restrictions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.iskool.entity.Otp;

@Repository
@Transactional
public class OtpDAO implements IOtpDAO {

	@Autowired
	private SessionFactory sessionFactory;

	@Override
	public void save(Otp obj) {
		Session session = sessionFactory.getCurrentSession();
		session.save(obj);
	}

	@Override
	public void update(Otp obj) {
		Session session = sessionFactory.getCurrentSession();
		session.update(obj);
	}

	@Override
	public void saveOrUpdate(Otp obj) {
		Session session = sessionFactory.getCurrentSession();
		session.saveOrUpdate(obj);
	}

	@Override
	public Otp get(UUID id) {
		Session session = sessionFactory.getCurrentSession();
		Otp obj = session.get(Otp.class, id);
		return obj;
	}

	@Override
	public Otp getByUserId(UUID userId) {
		Session session = sessionFactory.getCurrentSession();
		Criteria crit = session.createCriteria(Otp.class);
		crit.add(Restrictions.eq("userId", userId));
		List<Otp> list = crit.list();
		if (null != list && !list.isEmpty()) {
			return list.get(0);
		}
		return null;
	}

}
