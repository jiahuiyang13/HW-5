package edu.uwm.cs351;
import java.lang.reflect.Array;
import java.util.AbstractCollection;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.function.Consumer;
import java.util.Collection;
import edu.uwm.cs351.NewApptBook.Node;
import junit.framework.TestCase;

/**
 * A variant of the ApptBook ADT that follows the Collection model.
 * In particular, it has no sense of a current element.
 * All access to elements by the client must be through the iterator.
 * The {@link #add(Appointment)} method should add at the correct spot in sorted order in the collection.
 */



//Jiahui Yang


public class NewApptBook extends AbstractCollection<Appointment> implements Cloneable {
	private static Consumer<String> reporter = (s) -> { System.err.println("Invariant error: " + s); };
	//Initialize collection variables
	private Node head;
	private Node tail;
	private int manyItems;
	private int version;
	
	
	private boolean report(String error) {
		reporter.accept(error);
		return false;
	}
	private static boolean doReport = true; // change only in invariant tests
	
	

	private boolean wellFormed() {
		Node lag = null;
		int count = 0;
		//if the linked list is not empty and head's previous call is not null, there is a node before the head
		if (head != null && head.prev != null) {
			return report("Nodes before head");
		}
		//normal idiom loop
		for (Node p = head; p != null;lag = p, p=p.next) {
			//if p is not tail and p's next node's previous' is not p again, then the links aren't correct
			if (p.next != null && p.next.prev != p) {
				return report ("links do not match");
			}
			//if p's data is null, throws report because data cannot be null for a node
			if (p.data == null) {
				return report ("data of node is null");
			}
			
			//reports if linked list is in order comparing each node to the node after it
			if (p.next != null && p.next.data != null) {
				if (p.data.compareTo(p.next.data) > 0)
				{
					return report("list not in order");
				}
			}
			//increment count in loop to get size of linked list
			count++;
			
		}
		//if there is only one node and head and tail are not the same node then the tail is not in the right node
		if (count == 1 && head != tail) {
			return report("tail not in correct position");
		}
		//if head is null and tail isn't then there is no head in the linked list or linked list is empty but tail is a node
		if (head == null && tail != head) {
				return report ("head is null but not tail");
			}
		//if tail is not null and tail has nodes after is then there are nodes after tail
		if (tail!=null && tail.next!=null) {
			return report("Nodes after tail");
		}
		//if tail isn't equal to head or if it isn't the last node in the linked list then there is no tail
		if (tail != head && tail != lag) return report("there is no tail");
		//reports if manyItems not the number of nodes of list
		if (manyItems != count) return report("manyItems is not number of nodes in list");
		return true;
	}
	public static class Node{
		//Initialize node variables
		 Node next;
		 Appointment data;
		 Node prev;
		//Node  constructor that initializes data and node
		public Node(Appointment a) {
			 data = a;
		}
	}
	//default constructor to initialize fields
		public NewApptBook() {
			head = null;
			version = 0;
			tail = null;
			manyItems = 0;
			assert wellFormed(): "Invariant at end of constructor";
		}
		//specifying constructor to initialize fields
		public NewApptBook(int i) {
			head = null;
			version = 0;
			tail = null;
			manyItems = 0;
			assert wellFormed(): "Invariant at end of constructor";
		}
		//start an iterator after the first appointment that is equal or comes after the argument app
		//app can't be null
		public Iterator<Appointment> iterator(Appointment app) {
			assert wellFormed(): "Invariant broken in iterator";
			if (app == null) throw new NullPointerException();
			return new MyIterator(app);
		}
		
		
		
		@Override //required
		public Iterator<Appointment> iterator() {
			assert wellFormed(): "Invariant broken in iterator";
			return new MyIterator();
		}

		
		
		@Override //implementation
		public boolean add(Appointment e) {
			assert wellFormed(): "Invariant at start of add";
			if (e == null) throw new NullPointerException();
			Node added = new Node(e);
			Node lag = null;
			Node temp = null;
			
			//follower idiom
			for (Node p = tail; p != null;lag = p, p=p.prev) {
				if (p.data.compareTo(added.data)<=0) {
					temp = p;
					break;
				}
			}
			//if list is empty
			if (manyItems == 0) {
				head = tail = added;
				tail.next = null;
				head.next = null;
			}
			
			//if list is adding at tail
			else if (lag == null) {
				tail.next = added;
				added.prev = tail;
				tail = added;
				added.next = null;
			}
			
			
			//if adding at head
			else if (lag.prev == null) {
				lag.prev = added;
				head = added;
				head.next = lag;
				added.prev = null;
				
			}
			//adding in middle
			else {
				temp.next = added;
				lag.prev = added;
				added.next = lag;
				added.prev = temp;
			}
			
			
			
			++manyItems;
			++version;
			
			
			assert wellFormed(): "Invariant at end of add";
			return true;
		}
		//takes the NewApptBook addend's nodes and adds them into the NewApptBook that is calling addAll
		//the nodes being added will be sorted accordingly and tail and head will be updated accordingly as well using the add method
		public boolean addAll(NewApptBook addend) {
			assert wellFormed(): "Invariant at start of addAll";
			if(addend.manyItems == 0) return false;
			//if addall's argument and caller are the same NewApptBook, then addend is cloned and added to itself
			if(addend == this) addend = addend.clone();
			for (Node p = addend.head;p!=null;p=p.next) {
				add(p.data);
			}
			assert wellFormed(): "Invariant at end of addAll";
			return true;
		}
		
		
		@Override //efficiency
		public void clear() {
			if (head != null) {
				head.next = null;
				head.prev = null;
				head = null;
				tail = head;
				version++;
			}
			else {
				head = tail;
			}
			manyItems = 0;
			
			
			
			assert wellFormed(): ("failed at end of clear");
		}
		@Override //required
		public int size() {
			assert wellFormed(): "failed at size";
			return manyItems;
			
		}
		@Override //efficiency
		public boolean contains(Object o) {
			boolean check = false;
			Node lag = null;
			if(!(o instanceof Appointment))return false;
			else {
			for (Node p = head;p!=null;lag = p, p= p.next) {
				if (p.data == o) {
					check = true;
				}
			}
			if (lag == null) {
				if (o == head)check = true;
			}
			
			assert wellFormed(): "failed at end of contains";
			return check;
			}
		}
		//makes a copy of the NewApptBook and generates a new NewApptBook with the nodes copied
		public NewApptBook clone( ) { 
			assert wellFormed() : "invariant failed at start of clone";
			NewApptBook answer;
			Node last = null;
			try
			{
				answer = (NewApptBook) super.clone( );
			}
			catch (CloneNotSupportedException e)
			{  // This exception should not occur. But if it does, it would probably
				// indicate a programming error that made super.clone unavailable.
				// The most common error would be forgetting the "Implements Cloneable"
				// clause at the start of this class.
				throw new RuntimeException
				("This class does not implement Cloneable");
			}
			for (Node p = head; p!=null; p = p.next) {
				Node newNode = new Node(p.data);
				if (p == head) answer.head = newNode;
				if (p == tail) answer.tail = newNode;
				//set up links
				if (last == null) {
					answer.head = answer.tail = newNode;
				}
				//set up links
				else {
					last.next = newNode;
					newNode.prev = last;
				}
				last = newNode;
			}
		
			assert wellFormed() : "invariant failed at end of clone";
			assert answer.wellFormed() : "invariant on answer failed at end of clone";
			return answer;
			
			
		}
	
	


	private class MyIterator implements Iterator<Appointment> 
	{
		//Initialize iterators variables
		private Node cursor;
		private boolean canRemove = false;
		private int colVersion = version;
		
		private boolean wellFormed() {
			//flag
			boolean check = false;
			if (!NewApptBook.this.wellFormed()) return false;
			if (version != colVersion) return true; //not my fault if invariant broken
			//normal idiom that goes through the linked list
			for (Node p = head; p !=null; p = p.next) {
				//if any node in the linked list is the cursor or if cursor is null then the flag turns true
				if (p==cursor || cursor == null) {
					check = true;
				}
			}
			//if linked list is not empty and the flag is false then reports that cursor is not in the linked list
			if (manyItems != 0 && check == false) {
				return report("cursor is not in linked list");
			}
			//if cursor is null and canRemove is true then there is no cursor currently in the linked list so remove method can't be called
			if (cursor == null && canRemove == true) {
				return report ("cursor not in list, can't remove");
			}
			//if linked list is empty and cursor is not null then report an invariant
			if (manyItems == 0 && cursor != null) {
				return report("there is no cursor in the list");
			}
			//if linked list is empty and canRemove is true then report that there are no nodes to remove
			if (manyItems == 0 && canRemove == true) {
				return report("there is no element to remove");
			}
			
			return true;
		}
		
		
		//default iterator constructor that initializes fields
		public MyIterator() {
			
			cursor = head;
			
			assert wellFormed(): "Invariant at end of constructor";
		}
		//specifying iterator constructor that initializes fields and sets cursor to the argument app
		//if app is not in the linked list then cursor is the node after the argument
		public MyIterator(Appointment app) {
				Node temp = head;
				for (Node p = head; p!=null; p = p.next) {
					if(p.data == app) {
						cursor = p;
						break;
					}			
					else if(p.data.compareTo(app)>0) {
						cursor = p;
						break;
					}
				}
			assert wellFormed(): "Invariant at end of constructor";
		}
				
		
		
		private void checkVersion() {
			if (colVersion != version) throw new ConcurrentModificationException("stale iterator");
		}
		@Override //required
		public boolean hasNext() {
			assert wellFormed(): "Invariant at start of hasNext";
			checkVersion();

			
			if(cursor == null) {
				return false;
			}
			
			else if (canRemove == true && cursor.next == null) {
				return false;
			}
			else if(canRemove == false && cursor == null) {
				return false;
			}
			
			else {
				return true;
			}
			
		}
		@Override //required
		public Appointment next() {
			assert wellFormed(): "Invariant at start of next";
			if (hasNext() == false) throw new NoSuchElementException();
			checkVersion();
			if (canRemove == true) {
				cursor = cursor.next;
				
			}
			canRemove = true;
			return cursor.data;
			
		}
		@Override //implementation
		public void remove() {
			assert wellFormed(): "invariant at start of remove";
			checkVersion();
			if(canRemove == false) throw new IllegalStateException();
			if(head == tail) {
				head = tail = null;
				
			}
			else if (head != null && cursor == head) {
				head = head.next;
				head.prev = null;
			}
			else if(tail != null && cursor == tail) {
				tail = tail.prev;
				tail.next = null;
			}
			
			else {
				cursor.next.prev = cursor.prev;
				cursor.prev.next = cursor.next;
			}
			cursor = cursor.next;
			manyItems--;
			colVersion=++version;
			canRemove = false;
			
			assert wellFormed(): "invariant at end of remove";
			
		}

	}
	
	public static class TestInvariantChecker extends TestCase {
		Time now = new Time();
		Appointment e1 = new Appointment(new Period(now,Duration.HOUR),"1: think");
		Appointment e2 = new Appointment(new Period(now,Duration.DAY),"2: current");
		Appointment e3 = new Appointment(new Period(now.add(Duration.HOUR),Duration.HOUR),"3: eat");
		Appointment e4 = new Appointment(new Period(now.add(Duration.HOUR.scale(2)),Duration.HOUR.scale(8)),"4: sleep");
		Appointment e5 = new Appointment(new Period(now.add(Duration.DAY),Duration.DAY),"5: tomorrow");

		private int reports = 0;
		
		private void assertWellFormed(Object s, boolean expected) {
			reports = 0;
			Consumer<String> savedReporter = reporter;
			try {
				reporter = (String message) -> {
					++reports;
					if (message == null || message.trim().isEmpty()) {
						assertFalse("Uninformative report is not acceptable", true);
					}
					if (expected) {
						assertFalse("Reported error incorrectly: " + message, true);
					}
				};
				if (s instanceof NewApptBook) {
					assertEquals(expected, ((NewApptBook)s).wellFormed());
				} else {
					assertEquals(expected, ((NewApptBook.MyIterator)s).wellFormed());
				}
				if (!expected) {
					assertEquals("Expected exactly one invariant error to be reported", 1, reports);
				}
				reporter = null;
			} finally {
				reporter = savedReporter;
			}
		}
		
		protected Node newNode(Appointment a, Node p, Node n) {
			Node result = new Node(a);
			result.prev = p;
			result.next = n;
			result.data = a;
			return result;
		}
		
		protected Node newNode(Appointment a) {
			return newNode(a, null, null);
		}

		NewApptBook self;
		NewApptBook.MyIterator selfit;
		
		protected void setUp() {
			self = new NewApptBook();
			self.head = self.tail = null;
			self.manyItems = 0;
			self.version = 17;
			selfit = self.new MyIterator();
			selfit.canRemove = false;
			selfit.cursor = null;
			selfit.colVersion = 17;
		}

		public void testA0() {
			assertWellFormed(self, true);
		}
		
		public void testA1() {
			self.tail = new Node(e1);
			assertWellFormed(self, false);
		}
		
		public void testA2() {
			self.manyItems = -1;
			assertWellFormed(self, false);
			self.manyItems = 1;
			assertWellFormed(self, false);
		}
		
		public void testA3() {
			self.head = self.tail = newNode(null);
			assertWellFormed(self, false);
			self.manyItems = 1;
			assertWellFormed(self, false);
		}
		
		public void testB0() {
			self.head = self.tail = newNode(e2);
			assertWellFormed(self, false);
			self.manyItems = 1;
			assertWellFormed(self, true);
		}
		
		public void testB1() {
			self.head = newNode(e3);
			self.tail = newNode(e3);
			self.manyItems = 1;
			assertWellFormed(self, false);
			self.manyItems = 2;
			assertWellFormed(self, false);
		}
		
		public void testB2() {
			self.head = newNode(e3);
			self.tail = null;
			self.manyItems = 1;
			assertWellFormed(self, false);
			self.manyItems = 0;
			assertWellFormed(self, false);
		}
		
		public void testB3() {
			self.head = self.tail = newNode(e2);
			self.head.prev = newNode(e1,null,self.head);
			self.tail.next = newNode(e3,self.tail,null);
			self.manyItems = 0;
			assertWellFormed(self, false);
			self.manyItems = 1;
			assertWellFormed(self, false);
			self.manyItems = 2;
			assertWellFormed(self, false);
			self.manyItems = 3;
			assertWellFormed(self, false);			
		}
		
		public void testB4() {
			self.head = self.tail = newNode(e1);
			self.head.prev = self.head;
			self.tail.next = self.tail;
			self.manyItems = 0;
			assertWellFormed(self, false);
			self.manyItems = 1;
			assertWellFormed(self, false);
			self.manyItems = 2;
			assertWellFormed(self, false);
			self.manyItems = 3;
			assertWellFormed(self, false);
		}
		
		public void testC0() {
			self.head = newNode(e4);
			self.tail = newNode(e5);
			self.manyItems = 2;
			assertWellFormed(self, false);
			self.head.next = self.tail;
			assertWellFormed(self, false);
			self.tail.prev = self.head;
			assertWellFormed(self, true);
		}
		
		public void testC1() {
			self.head = newNode(e2);
			self.tail = newNode(e1);
			self.head.next = self.tail;
			self.tail.prev = self.head;
			self.manyItems = 2;
			assertWellFormed(self, false);
		}
		
		public void testC2() {
			self.head = newNode(e3);
			self.tail = newNode(e3);
			self.head.prev = self.head.next = self.tail;
			self.tail.prev = self.tail.next = self.head;
			self.manyItems = 2;
			assertWellFormed(self, false);			
			self.manyItems = 3;
			assertWellFormed(self, false);			
			self.manyItems = Integer.MAX_VALUE;
			assertWellFormed(self, false);			
		}
		
		public void testC3() {
			self.head = newNode(e3);
			self.tail = newNode(e3);
			self.manyItems = 2;
			self.head.next = self.tail;
			self.tail.prev = self.head;
			assertWellFormed(self, true);
			self.tail.next = newNode(e3,self.tail,null);
			assertWellFormed(self, false);
			self.tail.next = null;
			self.tail = null;
			assertWellFormed(self, false);
			self.tail = new Node(e3);
			self.tail.prev = self.head;
			assertWellFormed(self, false);
			self.tail.next = self.tail;
			self.tail.prev = self.tail;
			assertWellFormed(self, false);
		}
		
		public void testD0() {
			Node n1 = newNode(e1);
			Node n2 = newNode(e2);
			Node n3 = newNode(e3);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; n3.prev = n2;
			self.head = n1;
			self.tail = n3;
			self.manyItems = 3;
			assertWellFormed(self, true);
		}
		
		public void testD1() {
			Node n1 = newNode(e1);
			Node n2 = newNode(e2);
			Node n3 = newNode(e3);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; // n3.prev = n2;
			self.head = n1;
			self.tail = n3;
			self.manyItems = 3;
			assertWellFormed(self, false);
			
			n3.prev = n2;
			n2.prev = null;
			assertWellFormed(self, false);
			
			n2.prev = newNode(e1,null,n2);
			assertWellFormed(self, false);
			
			n2.prev = n1;
			n3.prev = newNode(e2,n1,n3);
			assertWellFormed(self, false);
		}
		
		public void testD2() {
			Node n1 = newNode(e4);
			Node n2 = newNode(e4);
			Node n3 = newNode(e4);
			Node n4 = newNode(e4);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; n3.prev = n2;
			n3.next = n4; n4.prev = n3;
			self.manyItems = 4;
			self.head = n1;
			self.tail = n4;
			assertWellFormed(self, true);
			
			self.tail = n3;
			assertWellFormed(self, false);
			self.manyItems = 3;
			assertWellFormed(self, false);
			
			self.tail = n4;
			self.head = n2;
			assertWellFormed(self, false);
		}
		
		public void testD3() {
			Node n1 = newNode(e4);
			Node n2 = newNode(e4);
			Node n3 = newNode(e4);
			Node n4 = newNode(e4);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; n3.prev = n2;
			n3.next = n4; n4.prev = n3;
			self.manyItems = 4;
			self.head = n1;
			self.tail = n4;
			assertWellFormed(self, true);
			
			n1.data = null;
			assertWellFormed(self, false);
			n1.data = e4;
			
			n2.data = null;
			assertWellFormed(self, false);
			n2.data = e4;
			
			n3.data = null;
			assertWellFormed(self, false);
			n3.data = e4;
			
			n4.data = null;
			assertWellFormed(self, false);
			n4.data = e4;
			
			assertWellFormed(self, true);
		}
		
		public void testE0() {
			Node n1 = newNode(e1);
			Node n2 = newNode(e2);
			Node n3 = newNode(e3);
			Node n4 = newNode(e4);
			Node n5 = newNode(e5);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; n3.prev = n2;
			n3.next = n4; n4.prev = n3;
			n4.next = n5; n5.prev = n4;
			self.head = n1;
			self.tail = n5;
			self.manyItems = 5;
			assertWellFormed(self, true);
			
			n1.prev = newNode(null,null,n1);
			assertWellFormed(self, false);
			n1.prev = null;
			
			n2.prev = newNode(e1,null,n2);
			assertWellFormed(self, false);
			n2.prev = null;
			assertWellFormed(self, false);
			n2.prev = n1;
			
			n3.prev = newNode(e2,null,n3);
			assertWellFormed(self, false);
			n3.prev = null;
			assertWellFormed(self, false);
			n3.prev = n2;
			
			n4.prev = newNode(e3,null,n4);
			assertWellFormed(self, false);
			n4.prev = null;
			assertWellFormed(self, false);
			n4.prev = n3;
			
			n5.prev = newNode(e4,null,n5);
			assertWellFormed(self, false);
			n5.prev = null;
			assertWellFormed(self, false);
			n5.prev = n4;
			
			assertWellFormed(self, true);
		}
		
		public void testE1() {
			Node n1 = newNode(e5);
			Node n2 = newNode(e5);
			Node n3 = newNode(e5);
			Node n4 = newNode(e5);
			Node n5 = newNode(e5);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; n3.prev = n2;
			n3.next = n4; n4.prev = n3;
			n4.next = n5; n5.prev = n4;
			self.head = n1;
			self.tail = n5;
			self.manyItems = 5;
			assertWellFormed(self, true);
			
			n1.next = n1;
			assertWellFormed(self, false);
			n1.next = n2;
			
			n2.next = n1;
			assertWellFormed(self, false);
			n2.next = n2;
			assertWellFormed(self, false);
			n2.next = n3;
			
			n3.next = n1;
			assertWellFormed(self, false);
			n3.next = n2;
			assertWellFormed(self, false);
			n3.next = n3;
			assertWellFormed(self, false);
			n3.next = n4;
			
			n4.next = n1;
			assertWellFormed(self, false);
			n4.next = n2;
			assertWellFormed(self, false);
			n4.next = n3;
			assertWellFormed(self, false);
			n4.next = n4;
			assertWellFormed(self, false);
			n4.next = n5;
			
			n5.next = n1;
			assertWellFormed(self, false);
			n5.next = n2;
			assertWellFormed(self, false);
			n5.next = n3;
			assertWellFormed(self, false);
			n5.next = n4;
			assertWellFormed(self, false);
			n5.next = n5;
			assertWellFormed(self, false);
			n5.next = null;
			
			assertWellFormed(self, true);
		}
		
		public void testE2() {
			Node n1 = newNode(e5);
			Node n2 = newNode(e5);
			Node n3 = newNode(e5);
			Node n4 = newNode(e5);
			Node n5 = newNode(e5);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; n3.prev = n2;
			n3.next = n4; n4.prev = n3;
			n4.next = n5; n5.prev = n4;
			self.head = n1;
			self.tail = n5;
			self.manyItems = 5;
			assertWellFormed(self, true);

			n1.prev = n1;
			assertWellFormed(self, false);
			n1.prev = n2;
			assertWellFormed(self, false);
			n1.prev = n3;
			assertWellFormed(self, false);
			n1.prev = n4;
			assertWellFormed(self, false);
			n1.prev = n5;
			assertWellFormed(self, false);
			n1.prev = null;
			
			n2.prev = null;
			assertWellFormed(self, false);
			n2.prev = n2;
			assertWellFormed(self, false);
			n2.prev = n3;
			assertWellFormed(self, false);
			n2.prev = n4;
			assertWellFormed(self, false);
			n2.prev = n5;
			assertWellFormed(self, false);
			n2.prev = n1;
			
			n3.prev = null;
			assertWellFormed(self, false);
			n3.prev = n1;
			assertWellFormed(self, false);
			n3.prev = n3;
			assertWellFormed(self, false);
			n3.prev = n4;
			assertWellFormed(self, false);
			n3.prev = n5;
			assertWellFormed(self, false);
			n3.prev = n2;
			
			n4.prev = null;
			assertWellFormed(self, false);
			n4.prev = n1;
			assertWellFormed(self, false);
			n4.prev = n2;
			assertWellFormed(self, false);
			n4.prev = n4;
			assertWellFormed(self, false);
			n4.prev = n5;
			assertWellFormed(self, false);
			n4.prev = n3;
			
			n5.prev = null;
			assertWellFormed(self, false);
			n5.prev = n1;
			assertWellFormed(self, false);
			n5.prev = n2;
			assertWellFormed(self, false);
			n5.prev = n3;
			assertWellFormed(self, false);
			n5.prev = n5;
			assertWellFormed(self, false);
			n5.prev = n4;
			
			assertWellFormed(self, true);
		}
		
		public void testE3() {
			Node n1 = newNode(e5);
			Node n2 = newNode(e5);
			Node n3 = newNode(e5);
			Node n4 = newNode(e5);
			Node n5 = newNode(e5);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; n3.prev = n2;
			n3.next = n4; n4.prev = n3;
			n4.next = n5; n5.prev = n4;

			Node m1 = newNode(e5);
			Node m2 = newNode(e5);
			Node m3 = newNode(e5);
			Node m4 = newNode(e5);
			Node m5 = newNode(e5);
			m1.next = m2; m2.prev = m1;
			m2.next = m3; m3.prev = m2;
			m3.next = m4; m4.prev = m3;
			m4.next = m5; m5.prev = m4;
			
			self.manyItems = 5;
			self.head = n1;
			self.tail = m5;
			assertWellFormed(self, false);

			m2.prev = n1;
			assertWellFormed(self, false);
			m3.prev = n2;
			assertWellFormed(self, false);
			m4.prev = n3;
			assertWellFormed(self, false);
			m5.prev = n4;
			assertWellFormed(self, false);
			
			n4.next = m5;
			assertWellFormed(self, true);			
		}

		public void testE4() {
			Node n1 = newNode(e5);
			Node n2 = newNode(e5);
			Node n3 = newNode(e5);
			Node n4 = newNode(e5);
			Node n5 = newNode(e5);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; n3.prev = n2;
			n3.next = n4; n4.prev = n3;
			n4.next = n5; n5.prev = n4;
			self.head = n1;
			self.tail = n5;
			self.manyItems = 5;
			
			assertWellFormed(self, true);
			
			n3.next = n1;
			n1.prev = n3;
			assertWellFormed(self, false);
			
			Node m1 = newNode(e5);
			Node m2 = newNode(e5);
			Node m3 = newNode(e5);
			Node m4 = newNode(e5);
			Node m5 = newNode(e5);
			m1.next = m2; m2.prev = m1;
			m2.next = m3; m3.prev = m2;
			m3.next = m4; m4.prev = m3;
			m4.next = m5; m5.prev = m4;
			
			n3.next = n4;
			n1.prev = null;
			assertWellFormed(self, true);
			
			n5.next = n1;
			n1.prev = n5;
			self.tail = m5;
			assertWellFormed(self, false);
		}
		
		public void testI0() {
			selfit.canRemove = false;
			assertWellFormed(selfit, true);
		}
		
		public void testI1() {
			selfit.cursor = newNode(e1);
			assertWellFormed(selfit, false);
			selfit.colVersion = 16;
			assertWellFormed(selfit, true);
			self.head = selfit.cursor;
			assertWellFormed(selfit, false);
			selfit.cursor = null;
			assertWellFormed(selfit, false);
			selfit.colVersion = 17;
			assertWellFormed(selfit, false);
		}
		
		public void testI2() {
			selfit.canRemove = true;
			assertWellFormed(selfit, false);
			selfit.cursor = newNode(e2);
			assertWellFormed(selfit, false);
			selfit.colVersion = 0;
			assertWellFormed(selfit, true);
		}
		
		public void testI3() {
			self.head = self.tail = newNode(e3);
			self.manyItems = 1;
			assertWellFormed(self, true);
			
			selfit.canRemove = false;
			assertWellFormed(selfit, true);
			selfit.cursor = self.head;
			assertWellFormed(selfit, true);
			selfit.canRemove = true;
			assertWellFormed(selfit, true);
			selfit.cursor = null;
			assertWellFormed(selfit, false);
		}
		
		public void testI4() {
			self.head = newNode(e1);
			self.tail = newNode(e2);
			self.head.next = self.tail;
			self.tail.prev = self.head;
			self.manyItems = 2;
			assertWellFormed(self, true);
			
			selfit.canRemove = false;
			selfit.cursor = null;
			assertWellFormed(selfit, true);
			selfit.cursor = self.head;
			assertWellFormed(selfit, true);
			selfit.cursor = self.tail;
			assertWellFormed(selfit, true);
			
			selfit.canRemove = true;
			assertWellFormed(selfit, true);
			selfit.cursor = self.head;
			assertWellFormed(selfit, true);
			selfit.cursor = null;
			assertWellFormed(selfit, false);
		}
		
		public void testI5() {
			Node n1 = newNode(e1);
			Node n2 = newNode(e2);
			Node n3 = newNode(e3);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; n3.prev = n2;
			self.head = n1;
			self.tail = n3;
			self.manyItems = 3;
			assertWellFormed(self, true);
			
			selfit.canRemove = false;
			
			selfit.cursor = newNode(e1,null,n2);
			assertWellFormed(selfit, false);
			selfit.cursor = newNode(e2,n1,n3);
			assertWellFormed(selfit, false);
			selfit.cursor = newNode(e3,n2,null);
			assertWellFormed(selfit, false);
			selfit.cursor = null;
			
			assertWellFormed(selfit, true);
		}
		
		public void testI6() {
			Node n1 = newNode(e4);
			Node n2 = newNode(e4);
			Node n3 = newNode(e4);
			Node n4 = newNode(e4);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; n3.prev = n2;
			n3.next = n4; n4.prev = n3;
			self.manyItems = 4;
			self.head = n1;
			self.tail = n4;
			assertWellFormed(self, true);

			selfit.canRemove = true;
			
			selfit.cursor = newNode(e1,null,n2);
			assertWellFormed(selfit, false);
			selfit.cursor = newNode(e2,n1,n3);
			assertWellFormed(selfit, false);
			selfit.cursor = newNode(e3,n2,n4);
			assertWellFormed(selfit, false);
			selfit.cursor = newNode(e4,n3,null);
			assertWellFormed(selfit, false);
			selfit.cursor = null;
			assertWellFormed(selfit, false);
		}
		
		public void testI7() {
			Node n1 = newNode(e1);
			Node n2 = newNode(e2);
			Node n3 = newNode(e3);
			Node n4 = newNode(e4);
			Node n5 = newNode(e5);
			n1.next = n2; n2.prev = n1;
			n2.next = n3; n3.prev = n2;
			n3.next = n4; n4.prev = n3;
			n4.next = n5; n5.prev = n4;
			self.head = n1;
			self.tail = n5;
			self.manyItems = 5;
			assertWellFormed(self, true);

			selfit.colVersion = 14;
			selfit.canRemove = true;
			
			selfit.cursor = newNode(e1,null,n2);
			assertWellFormed(selfit, true);
			selfit.cursor = newNode(e2,n1,n3);
			assertWellFormed(selfit, true);
			selfit.cursor = newNode(e3,n2,n4);
			assertWellFormed(selfit, true);
			selfit.cursor = newNode(e4,n3,n4);
			assertWellFormed(selfit, true);
			selfit.cursor = newNode(e5,n4,null);
			assertWellFormed(selfit, true);
			
			selfit.colVersion = self.version;
			assertWellFormed(selfit, false);
			selfit.cursor = null;
			assertWellFormed(selfit, false);
			selfit.canRemove = false;
			assertWellFormed(selfit, true);
		}
	}

	


}
