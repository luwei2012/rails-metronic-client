package com.yuyaa.awashcar.entity;

import java.io.Serializable;

public class Weather implements Serializable {

	@Override
	public String toString() {
		// TODO Auto-generated method stub

		return getStatus() + " " + getTemperature() + "℃";
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String status; // 保存天气情况，白天
	private String temperature; // 保存当天最高温度

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	public String getTemperature() {
		return temperature;
	}

	public void setTemperature(String temperature) {
		this.temperature = temperature;
	}

}
