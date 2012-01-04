package com.sensei.bql.parsers;

import java.util.Map;
import java.util.HashMap;
import junit.framework.TestCase;

import org.antlr.runtime.RecognitionException;
import org.junit.Test;

import org.json.JSONArray;
import org.json.JSONObject;
import org.json.JSONException;

public class TestBQL extends TestCase
{

  private BQLCompiler _compiler;
  private JsonComparator _comp = new JsonComparator(1);

  public TestBQL()
  {
    super();
    Map<String, String[]> facetInfoMap = new HashMap<String, String[]>();
    facetInfoMap.put("tags", new String[]{"multi", "string"});
    facetInfoMap.put("category", new String[]{"simple", "string"});
    facetInfoMap.put("price", new String[]{"range", "float"});
    facetInfoMap.put("mileage", new String[]{"range", "int"});
    facetInfoMap.put("color", new String[]{"simple", "string"});
    facetInfoMap.put("year", new String[]{"range", "int"});
    facetInfoMap.put("makemodel", new String[]{"path", "string"});
    facetInfoMap.put("city", new String[]{"path", "string"});
    _compiler = new BQLCompiler(facetInfoMap);
  }

  @Test
  public void testBasic1() throws Exception
  {
    System.out.println("testBasic1");
    System.out.println("==================================================");
    // No where clause
    JSONObject json = _compiler.compile(
      "select category " +
      "from cars "
      );
    assertTrue(json.length() == 0);
  }

  @Test
  public void testBasic2() throws Exception
  {
    System.out.println("testBasic2");
    System.out.println("==================================================");
    // No where clause, with a '*' in SELECT list
    JSONObject json = _compiler.compile(
      "select * " +
      "from cars "
      );
    assertTrue(json.length() == 0);
  }

  @Test
  public void testOrderBy() throws Exception
  {
    System.out.println("testOrderBy");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT category " +
      "FROM cars " +
      "ORDER BY color"
      );

    JSONObject expected = new JSONObject("{\"sort\": [{\"color\": \"asc\"}]}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testOrderBy2() throws Exception
  {
    System.out.println("testOrderBy2");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT category " +
      "FROM cars " +
      "ORDER BY color, price DESC, year ASC"
      );
    JSONObject expected = new JSONObject("{\"sort\": [{\"color\": \"asc\"},{\"price\": \"desc\"},{\"year\": \"asc\"}]}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testLimit1() throws Exception
  {
    System.out.println("testLimit1");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT category " +
      "FROM cars " +
      "LIMIT 123"
      );
    JSONObject expected = new JSONObject("{\"from\": 0, \"size\": 123}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testLimit2() throws Exception
  {
    System.out.println("testLimit2");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT category " +
      "FROM cars " +
      "LIMIT 15, 30"
      );
    JSONObject expected = new JSONObject("{\"from\": 15, \"size\": 30}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testGroupBy1() throws Exception
  {
    System.out.println("testGroupBy1");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT category " +
      "FROM cars " +
      "GROUP BY color"
      );
    JSONObject expected = new JSONObject("{\"groupBy\": {\"columns\": [\"color\"]}}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testGroupBy2() throws Exception
  {
    System.out.println("testGroupBy2");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT category " +
      "FROM cars " +
      "GROUP BY color TOP 5"
      );
    JSONObject expected = new JSONObject("{\"groupBy\": {\"columns\": [\"color\"], \"top\":5}}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testEqualPredInteger() throws Exception
  {
    System.out.println("testEqualPredInteger");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT category " +
      "FROM cars " +
      "WHERE year = 1999"
      );
    JSONObject expected = new JSONObject("{\"selections\":[{\"range\":{\"year\":{\"to\":1999,\"include_lower\":true,\"include_upper\":true,\"from\":1999}}}]}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testEqualPredFloat() throws Exception
  {
    System.out.println("testEqualPredFloat");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT category " +
      "FROM cars " +
      "WHERE price = 1500.99"
      );
    JSONObject expected = new JSONObject("{\"selections\":[{\"range\":{\"price\":{\"to\":1500.99,\"include_lower\":true,\"include_upper\":true,\"from\":1500.99}}}]}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testEqualPredString() throws Exception
  {
    System.out.println("testEqualPredString");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT category " +
      "FROM cars " +
      "WHERE color = 'red'"
      );
    JSONObject expected = new JSONObject("{\"selections\": [{\"term\": {\"color\": {\"value\": \"red\"}}}]}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testInPred() throws Exception
  {
    System.out.println("testInPred");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT category " +
      "FROM cars " +
      "WHERE color IN ('red', 'blue', 'yellow')"
      );
    JSONObject expected = new JSONObject("{\"selections\":[{\"terms\":{\"color\":{\"values\":[\"red\",\"blue\",\"yellow\"],\"excludes\":[],\"operator\":\"or\"}}}]}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testNotInPred() throws Exception
  {
    System.out.println("testNotInPred");
    System.out.println("==================================================");


    JSONObject json = _compiler.compile(
      "SELECT category " +
      "FROM cars " +
      "WHERE color NOT IN ('red', 'blue', 'yellow') EXCEPT ('black', 'green')"
      );
    JSONObject expected = new JSONObject("{\"selections\":[{\"terms\":{\"color\":{\"excludes\":[\"red\",\"blue\",\"yellow\"],\"values\":[\"black\", \"green\"],\"operator\":\"or\"}}}]}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testContainsAll() throws Exception
  {
    System.out.println("testContainsAll");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT category " +
      "FROM cars " +
      "WHERE color CONTAINS ALL ('red', 'blue', 'yellow') EXCEPT ('black', 'green')"
      );
    JSONObject expected = new JSONObject("{\"selections\":[{\"terms\":{\"color\":{\"values\":[\"red\",\"blue\",\"yellow\"],\"excludes\":[\"black\", \"green\"],\"operator\":\"and\"}}}]}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testPathPred1() throws Exception
  {
    System.out.println("testPathPred1");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT * " +
      "FROM cars " +
      "WHERE city = 'china/hongkong' WITH ('strict':false, 'depth':1)"
      );
    JSONObject expected = new JSONObject("{\"selections\":[{\"path\":{\"city\":{\"value\":\"china/hongkong\",\"strict\":false,\"depth\":1}}}]}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testPathPred2() throws Exception
  {
    System.out.println("testPathPred2");
    System.out.println("==================================================");

    int result = 0;
    try
    {
      JSONObject json = _compiler.compile(
                                          "SELECT * " +
                                          "FROM cars " +
                                          "WHERE city = 'china/hongkong' WITH ('strict':false, 'ddd':1)"
                                          );
    }
    catch (RecognitionException err) 
    {
      result = 1;
    }
    assertEquals(result, 1);
  }

  @Test
  public void testNotEqualPred() throws Exception
  {
    System.out.println("testNotEqualPred");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT category " +
      "FROM cars " +
      "WHERE color <> 'red'"
      );
    JSONObject expected = new JSONObject("{\"selections\":[{\"terms\":{\"color\":{\"values\":[],\"excludes\":[\"red\"],\"operator\":\"or\"}}}]}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testQueryIs() throws Exception
  {
    System.out.println("testQueryIs");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT category " +
      "FROM cars " +
      "WHERE QUERY IS 'cool AND moon-roof'"
      );
    JSONObject expected = new JSONObject("{\"query\":{\"query_string\":{\"query\":\"cool AND moon-roof\"}}}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testQueryAndSelection1() throws Exception
  {
    System.out.println("testQueryAndSelection1");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT category " +
      "FROM cars " +
      "WHERE QUERY IS 'cool AND moon-roof' " +
      "AND color = 'red' " +
      "AND category = 'sedan'"
      );
    JSONObject expected = new JSONObject("{\"query\":{\"query_string\":{\"query\":\"cool AND moon-roof\"}},\"selections\":[{\"term\":{\"color\":{\"value\":\"red\"}}},{\"term\":{\"category\":{\"value\":\"sedan\"}}}]}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testQueryAndSelection2() throws Exception
  {
    System.out.println("testQueryAndSelection2");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT category " +
      "FROM cars " +
      "WHERE QUERY IS 'cool AND moon-roof' " +
      "AND age = 12 "
      );
    JSONObject expected = new JSONObject("{\"query\":{\"query_string\":{\"query\":\"cool AND moon-roof\"}},\"filter\":{\"term\":{\"age\":{\"value\":12}}}}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testBrowseBy1() throws Exception
  {
    System.out.println("testBrowseBy1");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT category " +
      "FROM cars " +
      "BROWSE BY color"
      );
    JSONObject expected = new JSONObject("{\"facets\":{\"color\":{\"max\":10,\"order\":\"hits\",\"expand\":false,\"minhit\":0}}}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testBrowseBy2() throws Exception
  {
    System.out.println("testBrowseBy2");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT category " +
      "FROM cars " +
      "BROWSE BY color, price(true, 1, 20, value), year"
      );
    JSONObject expected = new JSONObject("{\"facets\":{\"price\":{\"max\":20,\"order\":\"val\",\"expand\":true,\"minhit\":1},\"color\":{\"max\":10,\"order\":\"hits\",\"expand\":false,\"minhit\":0},\"year\":{\"max\":10,\"order\":\"hits\",\"expand\":false,\"minhit\":0}}}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testBetweenPred() throws Exception
  {
    System.out.println("testBetweenPred");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT category " +
      "FROM cars " +
      "WHERE year BETWEEN 2000 AND 2001"
      );
    JSONObject expected = new JSONObject("{\"selections\":[{\"range\":{\"year\":{\"to\":2001,\"include_lower\":true,\"include_upper\":true,\"from\":2000}}}]}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testFetchingStored1() throws Exception
  {
    System.out.println("testFetchingStored1");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT * " +
      "FROM cars " +
      "FETCHING STORED FALSE"
      );
    JSONObject expected = new JSONObject("{}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testFetchingStored2() throws Exception
  {
    System.out.println("testFetchingStored2");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT * " +
      "FROM cars " +
      "FETCHING STORED true"
      );
    JSONObject expected = new JSONObject("{\"fetchStored\":true}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testNotBetweenPred() throws Exception
  {
    System.out.println("testNotBetweenPred");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT category " +
      "FROM cars " +
      "WHERE year NOT BETWEEN 2000 AND 2002"
      );
    JSONObject expected = new JSONObject("{\"filter\":{\"or\":[{\"range\":{\"year\":{\"to\":2000,\"include_upper\":false}}},{\"range\":{\"year\":{\"include_lower\":false,\"from\":2002}}}]}}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testRangePred1() throws Exception
  {
    System.out.println("testRangePred1");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT year " +
      "FROM cars " +
      "WHERE year > 1999"
      );
    JSONObject expected = new JSONObject("{\"selections\":[{\"range\":{\"year\":{\"from\":1999,\"include_lower\":false}}}]}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testRangePred2() throws Exception
  {
    System.out.println("testRangePred2");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT year " +
      "FROM cars " +
      "WHERE year <= 2000"
      );
    JSONObject expected = new JSONObject("{\"selections\":[{\"range\":{\"year\":{\"to\":2000,\"include_upper\":true}}}]}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testRangePred3() throws Exception
  {
    System.out.println("testRangePred3");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT year " +
      "FROM cars " +
      "WHERE year > 1999 AND year <= 2003 AND year >= 1999"
      );
    JSONObject expected = new JSONObject("{\"selections\":[{\"range\":{\"year\":{\"to\":2003,\"include_lower\":true,\"include_upper\":false,\"from\":1999}}}]}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testRangePred4() throws Exception
  {
    System.out.println("testRangePred4");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT * " +
      "FROM cars " +
      "WHERE name > 'abc' AND name < 'xyz' AND name >= 'ddd'"
      );
    JSONObject expected = new JSONObject("{\"filter\":{\"range\":{\"name\":{\"to\":\"xyz\",\"include_lower\":true,\"include_upper\":false,\"from\":\"ddd\"}}}}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testRangePred5() throws Exception
  {
    System.out.println("testRangePred5");
    System.out.println("==================================================");

    int result = 0;
    try
    {
    JSONObject json = _compiler.compile(
      "SELECT * " +
      "FROM cars " +
      "WHERE year > 1999 AND year < 1995"
      );
    }
    catch (RecognitionException err) 
    {
      result = 1;
    }
    assertEquals(result, 1);
  }

  @Test
  public void testOrPred() throws Exception
  {
    System.out.println("testOrPred");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT color " +
      "FROM cars " +
      "WHERE color = 'red' OR year > 1995"
      );
    JSONObject expected = new JSONObject("{\"filter\":{\"or\":[{\"term\":{\"color\":{\"value\":\"red\"}}},{\"range\":{\"year\":{\"include_lower\":false,\"from\":1995}}}]}}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testAndPred() throws Exception
  {
    System.out.println("testAndPred");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT color " +
      "FROM cars " +
      "WHERE color = 'red' AND year > 1995"
      );
    JSONObject expected = new JSONObject("{\"selections\":[{\"term\":{\"color\":{\"value\":\"red\"}}},{\"range\":{\"year\":{\"include_lower\":false,\"from\":1995}}}]}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testAndOrPred() throws Exception
  {
    System.out.println("testAndOrPred");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT color " +
      "FROM cars " +
      "WHERE (color = 'red' OR color = 'blue') " +
      "   OR (color = 'black' AND tags CONTAINS ALL ('hybrid', 'moon-roof', 'leather'))"
      );
    JSONObject expected = new JSONObject("{\"filter\":{\"or\":[{\"or\":[{\"term\":{\"color\":{\"value\":\"red\"}}},{\"term\":{\"color\":{\"value\":\"blue\"}}}]},{\"and\":[{\"term\":{\"color\":{\"value\":\"black\"}}},{\"terms\":{\"tags\":{\"values\":[\"hybrid\",\"moon-roof\",\"leather\"],\"excludes\":[],\"operator\":\"and\"}}}]}]}}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testSelectionAndFilter() throws Exception
  {
    System.out.println("testSelectionAndFilter");
    System.out.println("==================================================");

    // Here "age" is not a facet, so we have to treat it as a filter
    JSONObject json = _compiler.compile(
      "SELECT color " +
      "FROM cars " +
      "WHERE color = 'red' AND age > 25"
      );
    JSONObject expected = new JSONObject("{\"selections\":[{\"term\":{\"color\":{\"value\":\"red\"}}}],\"filter\":{\"range\":{\"age\":{\"include_lower\":false,\"from\":25}}}}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testMultipleQueries() throws Exception
  {
    System.out.println("testMultipleQueries");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT color " +
      "FROM cars " +
      "WHERE (color = 'red' AND query is 'hybrid AND cool') " +
      "   OR (color = 'blue' AND query is 'moon-roof AND navigation')"
      );
    JSONObject expected = new JSONObject("{\"filter\":{\"or\":[{\"and\":[{\"term\":{\"color\":{\"value\":\"red\"}}},{\"query\":{\"query_string\":{\"query\":\"hybrid AND cool\"}}}]},{\"and\":[{\"term\":{\"color\":{\"value\":\"blue\"}}},{\"query\":{\"query_string\":{\"query\":\"moon-roof AND navigation\"}}}]}]}}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testMatchPred() throws Exception
  {
    System.out.println("testMatchPred");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT color " +
      "FROM cars " +
      "WHERE MATCH(f1, f2) AGAINST('text1 AND text2')"
      );
    JSONObject expected = new JSONObject("{\"query\":{\"query_string\":{\"query\":\"text1 AND text2\",\"fields\":[\"f1\",\"f2\"]}}}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testColumnType1() throws Exception
  {
    System.out.println("testColumnType1");
    System.out.println("==================================================");

    int result = 0;
    try 
    {
      JSONObject json = _compiler.compile(
        "SELECT * " +
        "FROM cars " +
        "WHERE color = 1"
      );
    }
    catch (RecognitionException err) 
    {
      result = 1;
    }
    assertEquals(result, 1);
  }

  @Test
  public void testColumnType2() throws Exception
  {
    System.out.println("testColumnType2");
    System.out.println("==================================================");

    int result = 0;
    try 
    {
      JSONObject json = _compiler.compile(
        "SELECT * " +
        "FROM cars " +
        "WHERE mileage = 111 " +
        "  OR (color IN ('red', 'blue') AND year > 'bbb')"
      );
    }
    catch (RecognitionException err) 
    {
      result = 1;
    }
    assertEquals(result, 1);
  }

  @Test
  public void testColumnType3() throws Exception
  {
    System.out.println("testColumnType3");
    System.out.println("==================================================");

    int result = 0;
    try 
    {
      JSONObject json = _compiler.compile(
        "SELECT * " +
        "FROM cars " +
        "WHERE color IN ('red', 123)"
      );
    }
    catch (RecognitionException err) 
    {
      result = 1;
    }
    assertEquals(result, 1);
  }

  @Test
  public void testColumnType4() throws Exception
  {
    System.out.println("testColumnType4");
    System.out.println("==================================================");

    int result = 0;
    try 
    {
      JSONObject json = _compiler.compile(
        "SELECT * " +
        "FROM cars " +
        "WHERE tags CONTAINS ALL ('cool', 123)"
      );
    }
    catch (RecognitionException err) 
    {
      result = 1;
    }
    assertEquals(result, 1);
  }

  @Test
  public void testGivenClause1() throws Exception
  {
    System.out.println("testGivenClause1");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT * " +
      "FROM cars " +
      "GIVEN FACET PARAM (My-Network, 'srcid', int, 8233570)"
      );
    JSONObject expected = new JSONObject("{\"facetInit\":{\"My-Network\":{\"srcid\":{\"values\":[8233570],\"type\":\"int\"}}}}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testGivenClause2() throws Exception
  {
    System.out.println("testGivenClause2");
    System.out.println("==================================================");

    JSONObject json = _compiler.compile(
      "SELECT * " +
      "FROM cars " +
      "GIVEN FACET PARAM (My-Network, 'srcid', int, 8233570), " +
      "                  (time, 'now', long, '999999'), " +
      "                  (member, 'last_name', string, 'Cui'), " + 
      "                  (member, 'age', int, 25)"
      );
    JSONObject expected = new JSONObject("{\"facetInit\":{\"member\":{\"age\":{\"values\":[25],\"type\":\"int\"},\"last_name\":{\"values\":[\"Cui\"],\"type\":\"string\"}},\"time\":{\"now\":{\"values\":[\"999999\"],\"type\":\"long\"}},\"My-Network\":{\"srcid\":{\"values\":[8233570],\"type\":\"int\"}}}}");
    assertTrue(_comp.isEquals(json, expected));
  }

  @Test
  public void testCorrectStatement() throws Exception
  {
    System.out.println("\n==================================================");
    System.out.println("testCorrectStatement");
    System.out.println("==================================================");
    //compile the statement

    JSONObject json = _compiler.compile(
      "SELECT color, year " +
      "FROM cars " +
      "WHERE QUERY IS \"hello\" " +
      "  AND color IN (\"red\", \"blue\") EXCEPT ('red') " +
      "  AND category = 'sedan' WITH ('aaa':'111', 'bbb':'222', 'ccc':'333') " +
      "  AND year NOT BETWEEN 1999 AND 2000"
      );
    
    // for (int i = 0; i < ast.getChildCount(); ++i)
    // {
    //   System.out.print(ast.getChild(i).getText() + " -- ");
    // }
    //check AST structure
    //assertEquals(BQLParser.SELECT, ast.getChild(0).getType());
    //assertEquals(BQLParser.STAR, ast.getChild(1).getType());
  }

}
