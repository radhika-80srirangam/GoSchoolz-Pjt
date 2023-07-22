package com.iskool.dao;

import java.util.UUID;

import com.iskool.entity.Otp;

public interface IOtpDAO {
	public void save(Otp obj);
	public void saveOrUpdate(Otp obj);
	public void update(Otp obj);
	public Otp get(UUID id);
	public Otp getByUserId(UUID userId);
	
}