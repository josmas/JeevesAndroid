/* **************************************************
 Copyright (c) 2012, University of Cambridge
 Neal Lathia, neal.lathia@cl.cam.ac.uk
 Kiran Rachuri, kiran.rachuri@cl.cam.ac.uk

This library was developed as part of the EPSRC Ubhave (Ubiquitous and
Social Computing for Positive Behaviour Change) Project. For more
information, please visit http://www.emotionsense.org

Permission to use, copy, modify, and/or distribute this software for any
purpose with or without fee is hereby granted, provided that the above
copyright notice and this permission notice appear in all copies.

THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY
SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF OR
IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 ************************************************** */

package com.ubhave.triggermanager.config;

import java.util.HashMap;

public class TriggerConfig
{	
	/*
	 * Config Keys
	 */
	public static final String IGNORE_USER_PREFERENCES = "ignoreCap";
	public static final String MAX_DAILY_NOTIFICATION_CAP = "limitDailyCap";
	public static final String TRIGGER_ENABLED = "isEnabled";

	//Start and end dates of particular triggers
	public final static String FROM_DATE = "dateFrom";
	public final static String TO_DATE = "dateTo";


	// Time Boundaries
	public static final String LIMIT_BEFORE_HOUR = "limitBeforeHour";
	public static final String LIMIT_AFTER_HOUR = "limitAfterHour";
	public static final String INTERVAL_TRIGGER_TIME = "intervalTriggerTime";
	public static final String FIXED_RANDOM = "fixedRandom";
	//public static final String INTERVAL_WINDOW = "intervalWindowLength";
//	public static final String GRANULARITY = "granularity";

	// Sensor Based Triggers
	public final static String SENSOR_TYPE = "selectedSensor";
	public final static String INTERESTING_VALUE = "result";
	public final static String NOTIFICATION_PROBABILITY = "notificationProb";
	public final static String POST_SENSE_WAIT_INTERVAL_MILLIS = "postSenseWait";
	

	//BUTTON NAME
	public static final String BUTTON_NAME = "selectedButton";
	private final HashMap<String, Object> parameters;

	//SURVEY NAME
	public static final String SURVEY_NAME = "selectedSurvey";
	public static final String SURVEY_RESULT = "result";
	public static final String SURVEY_NUMBER = "numTimes";

	public TriggerConfig()
	{
		parameters = new HashMap<String, Object>();
	}
	
	public void addParameter(final String key, final Object value)
	{
		parameters.put(key, value);
	}

	public HashMap<String,Object> getParams(){
		return parameters;
	}
	public Object getParameter(final String key)
	{
		if (parameters.containsKey(key))
		{
			return parameters.get(key);
		}
		else
		{
			return defaultValue(key);
		}
	}
	
	private Object defaultValue(final String key)
	{
		if (key.equals(LIMIT_BEFORE_HOUR))
		{
			return TriggerManagerConstants.DEFAULT_DO_NOT_DISTURB_BEFORE_MINUTES;
		}
		else if (key.equals(LIMIT_AFTER_HOUR))
		{
			return TriggerManagerConstants.DEFAULT_DO_NOT_DISTURB_AFTER_MINUTES;
		}
		else if (key.equals(INTERVAL_TRIGGER_TIME))
		{
			return TriggerManagerConstants.DEFAULT_MIN_TRIGGER_INTERVAL_MINUTES;
		}
		else if (key.equals(MAX_DAILY_NOTIFICATION_CAP))
		{
			return TriggerManagerConstants.DEFAULT_DAILY_NOTIFICATION_CAP;
		}
		else if (key.equals(TRIGGER_ENABLED))
		{
			return TriggerManagerConstants.DEFAULT_TRIGGER_ENABLED;
		}
//		else if (key.equals(LIMIT_BEFORE_HOUR))
//		{
//			return TriggerManagerConstants.DEFAULT_DO_NOT_DISTURB_BEFORE_MINUTES;
//		}
//		else if (key.equals(LIMIT_AFTER_HOUR))
//		{
//			return TriggerManagerConstants.DEFAULT_DO_NOT_DISTURB_AFTER_MINUTES;
//		}
//		else if (key.equals(INTERVAL_TRIGGER_TIME))
//		{
//			return TriggerManagerConstants.DEFAULT_MIN_TRIGGER_INTERVAL_MINUTES;
//		}
		else if (key.equals(IGNORE_USER_PREFERENCES))
		{
			return TriggerManagerConstants.DEFAULT_IS_SYSTEM_TRIGGER;
		}
		else
		{
			System.err.println("Key not found: "+key);
			return null;
		}
	}
	
	public boolean containsKey(String key)
	{
		return parameters.containsKey(key);
	}
	
	public boolean isSystemTrigger()
	{
		return (Boolean) getParameter(TriggerConfig.IGNORE_USER_PREFERENCES);
	}

	public int getValueInMinutes(String key)
	{
		return Integer.parseInt(getParameter(key).toString());
	}
}
