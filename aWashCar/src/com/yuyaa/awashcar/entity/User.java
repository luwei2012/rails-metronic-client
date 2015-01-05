package com.yuyaa.awashcar.entity;

import java.io.Serializable;

import org.json.JSONException;
import org.json.JSONObject;

public class User implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public String getAccount() {
		return account;
	}

	public void setAccount(String account) {
		this.account = account;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPhone() {
		return phone;
	}

	public void setPhone(String phone) {
		this.phone = phone;
	}

	public String getLicence_plate() {
		return licence_plate;
	}

	public void setLicence_plate(String licence_plate) {
		this.licence_plate = licence_plate;
	}

	public int getIntegral() {
		return integral;
	}

	public void setIntegral(int integral) {
		this.integral = integral;
	}

	public String getCar_type() {
		return car_type;
	}

	public void setCar_type(String car_type) {
		this.car_type = car_type;
	}

	public float getPercent() {
		return percent;
	}

	public void setPercent(float percent) {
		this.percent = percent;
	}

	public String getIntegral_tip() {
		return integral_tip;
	}

	public void setIntegral_tip(String integral_tip) {
		this.integral_tip = integral_tip;
	}

	public String account;
	public String password;
	public String email;
	public String name;
	public String phone;
	public String licence_plate;
	public int integral;
	public int sex;
	public String car_type;
	public float percent;
	public String integral_tip;
	public int last_comment_time;
	/**
	 * 最新一次的经纬度
	 */
	public double mCurrentLantitude;
	public double mCurrentLongitude;
	public String address;

	public User() {
		super();
		// TODO Auto-generated constructor stub
	}

	public void updateUser(JSONObject item) throws JSONException {
		name = item.optString("name");
		account = item.optString("account");
		licence_plate = item.optString("licence_plate");
		car_type = item.optString("car_type");
		sex = item.optInt("sex");
		phone = item.optString("phone");
		integral = item.optInt("integral");
		integral_tip = item.optString("integral_tip");
		percent = (float) item.optDouble("percent");
		email = item.optString("email");
	}

	public double getmCurrentLantitude() {
		return mCurrentLantitude;
	}

	public void setmCurrentLantitude(double mCurrentLantitude) {
		this.mCurrentLantitude = mCurrentLantitude;
	}

	public double getmCurrentLongitude() {
		return mCurrentLongitude;
	}

	public void setmCurrentLongitude(double mCurrentLongitude) {
		this.mCurrentLongitude = mCurrentLongitude;
	}

	public String getAddress() {
		return address;
	}

	public int getLast_comment_time() {
		return last_comment_time;
	}

	public void setLast_comment_time(int last_comment_time) {
		this.last_comment_time = last_comment_time;
	}

	public void setAddress(String address) {
		this.address = address;
	}
}
