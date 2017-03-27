package com.gson;

import com.google.gson.annotations.SerializedName;

public class Now {

	
	
	
	@SerializedName("tmp")
	public String remperataure;
	
	
	@SerializedName("cond")
	public More more;
	
	
	public class More{
		
		@SerializedName("txt")
		public String info;
	}
	
}
