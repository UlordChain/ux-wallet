package one.ulord.upaas.uxtoken.wallet.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GlobalConfig {
	private static final Logger logger = LoggerFactory.getLogger(GlobalConfig.class);
	public static HashMap<String, String> mapConfigure = new HashMap<String, String>();

	public static String get(String key){
		synchronized (mapConfigure) {
			return mapConfigure.get(key);
		}
	}
	
	public static String get(String key, String defaultValue){
		synchronized (mapConfigure) {
			String ret = mapConfigure.get(key);
			return ret != null ? ret : defaultValue;
		}
	}
	public static void put(String key, String value){
		synchronized (mapConfigure) {
			mapConfigure.put(key, value);
		}
	}
	public static String dump(){
		StringBuilder sb = new StringBuilder();
		for (Entry<String, String> item : mapConfigure.entrySet()){
			sb.append(item.getKey() + ":" + item.getValue());
			sb.append("\n");
		}
		return sb.toString();
	}

	public static void loadConfigure(String configureFile) {
		try {
			InputStream in = Loader.getResourceAsStream(configureFile);
			BufferedReader br = new BufferedReader(new InputStreamReader(in));
			Pattern r = Pattern.compile("^[ \t]*([a-zA-Z]{1}[._a-zA-Z0-9]+)[ ]*=[ ]*(.*)");
			String line;
			while((line = br.readLine()) != null){
				Matcher m = r.matcher(line);
				if (m.find()){
					GlobalConfig.put(m.group(1), m.group(2));
				}
			}

			in.close();
		} catch (IOException e) {
			logger.warn("Configure file:config-test.properties read excetion.", e);
		}
	}

}
