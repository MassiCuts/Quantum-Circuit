package utils.customCollections;

class ListNode <T> {
	T element;
	ListNode<T> next;
	
	ListNode (T element, ListNode<T> next) {
		this.element = element;
		this.next = next;
	}
}
