package com.sensei.search.query.filters;

import org.apache.lucene.search.Filter;
import org.json.JSONObject;

import com.sensei.search.query.FilterConstructor;
import com.sensei.search.query.SenseiTermFilter;

public class TermFilterConstructor extends FilterConstructor{

	@Override
	public Filter constructFilter(JSONObject json) throws Exception {
		boolean noOptimize = json.optBoolean("_noOptimize",false);
		
		String[] names = JSONObject.getNames(json);
		String termName = null;
		for (String name : names){
			if (!name.equals("_noOptimize")){
				termName = name;
				break;
			}
		}
		
		if (termName == null) throw new IllegalArgumentException("no term name specified: "+json);
		String val = json.optString(termName, null);
		if (val==null)  throw new IllegalArgumentException("no term value specified: "+json);
		return new SenseiTermFilter(termName, new String[]{val}, null, false, noOptimize);
	}
	
}
