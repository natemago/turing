package org.def.turing;


public class Tape extends DBLLinkedList{

	private String defValue;
	public Tape(boolean infinite, String defaultSymbol) {
		super(infinite);
		this.defValue = defaultSymbol;
	}
	
	
	public void addToTape(Object symbol){
		addRight(new DBLNode(symbol));
	}
	
	public Object getDefaultValue(){
		return defValue;
	}
}

class DBLLinkedList{
	private int size = 0;
	private DBLNode first;
	private DBLNode current;
	private DBLNode last;
	private int curr = 0;
	private boolean infinite = false;
	
	
	
	public DBLLinkedList(boolean infinite) {
		super();
		this.infinite = infinite;
	}

	public void add(DBLNode node, boolean toLeft){
		if(size == 0){
			first = last = current = node;
			size = 1;
			curr = 0;
			node.id = curr;
		}else{
			if(toLeft){
				DBLNode f = first;
				f.left = node;
				this.first = node;
				first.right = f;
				first.left = null;
				node.id = f.id-1;
			}else{
				DBLNode l = last;
				l.right = node;
				this.last = node;
				last.right = null;
				last.left = l;
				node.id = l.id+1;
			}
			size++;
		}
	}
	
	public void addLeft(DBLNode node){this.add(node, true);}
	public void addRight(DBLNode node){this.add(node, false);}
	public void moveLeft() throws Exception{
		if(current != null){
			if(current.left != null){
				current = current.left;
				curr--;
			}else if(infinite){
				DBLNode n = new DBLNode(getDefaultValue());
				addLeft(n);
				current = n;
				curr--;
			}else{
				throw new Exception("LIST_UNDER");
			}
		}else if(size == 0 && infinite){
			addLeft(new DBLNode(getDefaultValue()));
			current = first;
			//moveLeft();
		}
	}
	public void moveRight() throws Exception{
		if(current != null){
			if(current.right != null){
				current = current.right;
				curr++;
			}else if(infinite){
				DBLNode n = new DBLNode(getDefaultValue());
				addRight(n);
				current = n;
				curr++;
			}else{
				throw new Exception("LIST_OVER");
			}
		}else if(size == 0 && infinite){
			addRight(new DBLNode(getDefaultValue()));
			current = last;
			//moveRight();
		}
	}
	public DBLNode getCurrent(){return current;}
	public Object getCurrentValue() throws Exception{
		if(current != null) return current.value;
		else if(infinite) {
			if(first != null) moveLeft();
			else moveRight();
			return getCurrentValue();
		}
		throw new Exception("EMPTY");
	}
	public int getCurrentPosition(){return curr;}
	public int getSize(){return size;}
	
	public String toString(){
		DBLNode f = first;
		StringBuffer b = new StringBuffer();
		while(f!=null){
			if(curr != f.id)b.append("[" + f.value + "]");
			else b.append(">" + f.value + "<");
			f=f.right;
		}
		return b.toString();
	}
	
	public Object getDefaultValue(){
		return DBLNode.DBLNODE_DEFAULT_VALUE;
	}
}
class DBLNode{
	static Object DBLNODE_DEFAULT_VALUE = null;
	public DBLNode(Object v) {
		this.value = v;
	}
	
	Object value;
	int id;
	DBLNode left;
	DBLNode right;
}