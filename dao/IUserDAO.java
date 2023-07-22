package com.iskool.dao;

import java.util.List;
import java.util.UUID;

import com.iskool.dto.UserDto;
import com.iskool.entity.User;

public interface IUserDAO {
	public void save(User obj);
	public void saveOrUpdate(User obj);
	public void update(User obj);
	public User get(UUID id);
	public List<User> getAll();
	public List<User> getAll(UUID agentId);
	public void delete(UUID userId, UUID deletedBy);
	public User getUserByUserName(String userName);
	public List<UserDto> getActiveUsersByAgent(UUID agentId);
	public User getUserByAgentId(UUID agentId);
	public User getUserByUserNameOrEmail(String userNameOrEmail);
	
}
