package cs435.guiproto;

import org.junit.Test;

import cs435.guiproto.Constants.margin;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.junit.Assert;

public class RelativeTest {

	@Test
	/**
	 * Tests whether the positional attributes of the children are correct. Does not check for margins. Only checks horizontal
	 * alignment
	 */
	public void packBaseCaseTest(){
		RelativeLayout rel = new RelativeLayout();
		View a = new Component("a");
		a.setId(1);
		a.setValues(54, 64, 50, 100);
		Component b = new Component("b");
		b.setId(2);
		b.setValues(34, 254, 200, 50);
		Component c = new Component("c");
		c.setId(3);
		c.setValues(184, 326, 50, 50);
		rel.setWidth(304);
		rel.setHeight(437);
		rel.setX(8);
		rel.setY(8);
		rel.addToChildren(a);
		rel.addToChildren(b);
		rel.addToChildren(c);
		rel.pack();
		
		//these are what the children will be tested against
		HashMap<Constants.RelAttributes, String> alpha = new HashMap<Constants.RelAttributes, String>();
		alpha.put(Constants.RelAttributes.ALIGN_PARENT_TOP, "true");
		alpha.put(Constants.RelAttributes.ALIGN_START, "2");
		HashMap<Constants.RelAttributes, String> beta = new HashMap<Constants.RelAttributes, String>();
		beta.put(Constants.RelAttributes.ALIGN_END, "3");
		beta.put(Constants.RelAttributes.ABOVE, "3");
		HashMap<Constants.RelAttributes, String> delta = new HashMap<Constants.RelAttributes, String>();
		delta.put(Constants.RelAttributes.ALIGN_PARENT_BOTTOM, "true");
		delta.put(Constants.RelAttributes.ALIGN_PARENT_END, "true");
		
		//testing starts here
		System.out.println("Children: " + Arrays.toString(rel.getChildren().toArray()));
		System.out.println("alpha: " + alpha);
		System.out.println("should be equal to: " + rel.getNewList().get(0).getMap());
		assertTrue(alpha.equals(rel.getNewList().get(0).getMap()));
		assertEquals(beta, rel.getNewList().get(1).getMap());
		assertEquals(delta, rel.getNewList().get(2).getMap());
	}
	
	@Test
	public void baseCaseMarginsTest(){
		RelativeLayout rel = new RelativeLayout();
		View a = new Component("a");
		a.setId(1);
		a.setValues(54, 64, 50, 100);
		Component b = new Component("b");
		b.setId(2);
		b.setValues(34, 254, 200, 50);
		Component c = new Component("c");
		c.setId(3);
		c.setValues(184, 326, 50, 50);
		rel.setWidth(304);
		rel.setHeight(437);
		rel.setX(8);
		rel.setY(8);
		rel.addToChildren(a);
		rel.addToChildren(b);
		rel.addToChildren(c);
		rel.pack();
		
		HashMap<Constants.margin, Integer> alpha = new HashMap<Constants.margin, Integer>();
		alpha.put(Constants.margin.TOP, 56);
		alpha.put(Constants.margin.START, 20);
		HashMap<Constants.margin, Integer> beta = new HashMap<Constants.margin, Integer>();
		beta.put(margin.BOTTOM, 22);
		HashMap<Constants.margin, Integer> delta = new HashMap<Constants.margin, Integer>();
		delta.put(margin.BOTTOM, 69);
		delta.put(margin.END, 78);
		
		//testing begins here
		assertEquals(alpha, rel.getNewList().get(0).getMargins());
		assertEquals(beta, rel.getNewList().get(1).getMargins());
		assertEquals(delta, rel.getNewList().get(2).getMargins());
	}
	
	@Test
	public void topBarTest(){
		RelativeLayout rel = new RelativeLayout();
		View a = new Component("a");
		a.setId(1);
		//this is the top bar
		a.setValues(20, 20, 100, 600);
		Component b = new Component("b");
		b.setId(2);
		b.setValues(40, 500, 50, 50);
		Component c = new Component("c");
		c.setId(3);
		c.setValues(340, 505, 50, 50);
		rel.setWidth(400);
		rel.setHeight(600);
		rel.setX(20);
		rel.setY(20);
		rel.addToChildren(a);
		rel.addToChildren(b);
		rel.addToChildren(c);
		rel.pack();
	}
	/**
	 * tests the checkNotCircular method
	 */
	@Test
	public void circularTest1(){
		RelativeLayout rel = new RelativeLayout();
		Component a = new Component("a");
		a.setAndroidId("1");
		Component b = new Component("b");
		b.setAndroidId("2");
		RelativeDecorator A = new RelativeDecorator(a);
		RelativeDecorator B = new RelativeDecorator(b);
		HashMap<Constants.RelAttributes, String> map1 = new HashMap<Constants.RelAttributes, String>();
		map1.put(Constants.RelAttributes.ALIGN_START, "2");
		HashMap<Constants.RelAttributes, String> map2 = new HashMap<Constants.RelAttributes, String>();
		map2.put(Constants.RelAttributes.ALIGN_START, "1");
		
		A.setMap(map1);
		B.setMap(map2);
		
		ArrayList<RelativeDecorator> lst = new ArrayList<RelativeDecorator>();
		lst.add(A);
		lst.add(B);
		rel.setNewList(lst);
		
//		boolean test = rel.checkNotCircular(A, B, false);
//		assertFalse(test);
	}
	
	/**
	 * tests circular dependencies involving more than two components
	 */
	@Test
	public void circularTest2(){
		RelativeLayout rel = new RelativeLayout();
		Component a = new Component("a");
		a.setAndroidId("1");
		Component b = new Component("b");
		b.setAndroidId("2");
		Component c = new Component("c");
		c.setAndroidId("3");
		RelativeDecorator A = new RelativeDecorator(a);
		RelativeDecorator B = new RelativeDecorator(b);
		RelativeDecorator C = new RelativeDecorator(c);
		HashMap<Constants.RelAttributes, String> map1 = new HashMap<Constants.RelAttributes, String>();
		map1.put(Constants.RelAttributes.ALIGN_START, "2");
		HashMap<Constants.RelAttributes, String> map2 = new HashMap<Constants.RelAttributes, String>();
		map2.put(Constants.RelAttributes.ALIGN_START, "3");
		HashMap<Constants.RelAttributes, String> map3 = new HashMap<Constants.RelAttributes, String>();
		map3.put(Constants.RelAttributes.ALIGN_START, "1");
		
		A.setMap(map1);
		B.setMap(map2);
		C.setMap(map3);
		
		ArrayList<RelativeDecorator> lst = new ArrayList<RelativeDecorator>();
		lst.add(A);
		lst.add(B);
		lst.add(C);
		rel.setNewList(lst);
		
//		boolean test = rel.checkNotCircular(A, B, false);
//		assertFalse(test);
	}
	
	/**
	 * good example - should return true
	 * here C depends on A which depends on B which depends on nobody, so it should pass.
	 */
	@Test
	public void circularTest3(){
		RelativeLayout rel = new RelativeLayout();
		Component a = new Component("a");
		a.setAndroidId("1");
		Component b = new Component("b");
		b.setAndroidId("2");
		Component c = new Component("c");
		c.setAndroidId("3");
		RelativeDecorator A = new RelativeDecorator(a);
		RelativeDecorator B = new RelativeDecorator(b);
		RelativeDecorator C = new RelativeDecorator(c);
		HashMap<Constants.RelAttributes, String> map1 = new HashMap<Constants.RelAttributes, String>();
		map1.put(Constants.RelAttributes.ALIGN_START, "2");
		HashMap<Constants.RelAttributes, String> map2 = new HashMap<Constants.RelAttributes, String>();
		map2.put(Constants.RelAttributes.ALIGN_PARENT_START, "3");
		HashMap<Constants.RelAttributes, String> map3 = new HashMap<Constants.RelAttributes, String>();
		map3.put(Constants.RelAttributes.ALIGN_START, "1");
		
		A.setMap(map1);
		B.setMap(map2);
		C.setMap(map3);
		
		ArrayList<RelativeDecorator> lst = new ArrayList<RelativeDecorator>();
		lst.add(A);
		lst.add(B);
		lst.add(C);
		rel.setNewList(lst);
		
//		boolean test = rel.checkNotCircular(A, B, false);
//		assertTrue(test);
	}
	
}
