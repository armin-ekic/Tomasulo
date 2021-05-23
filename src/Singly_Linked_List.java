/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
//package comp_arch_proj_1;

/**
 *
 * @author Armin Ekic and Luke Halverson
 */
public class Singly_Linked_List <E> {
    private static class Node<E>{
        private E element;
        private Node<E> next;
        public Node(E e, Node<E> n){
            element = e;
            next = n;
        }
        //Gets the element and returns it
        public E getElement(){return element;}

        //Node that looks at the next list element
        public Node<E> getNext(){return next;}

        //Sets the next node
        public void setNext(Node<E> n) { next = n;}
    }

    //Initialize the linked list
    private Node<E> head = null;
    private Node<E> tail = null;
    private int size = 0;

    //Default constructor for the linked list
    public Singly_Linked_List(){}

    //Check the size of the list
    public int size(){return size;}

    //Check to see if the list is empty
    public boolean isEmpty(){return size == 0;}

    //Get the first element in the list
    public E first(){
        if (isEmpty()) return null;
        return head.getElement();
    }
    
    //Get the last element in the linked list
    public E last(){
        if (isEmpty()) return null;
        return tail.getElement();
    }

    //Add an element to the linked list
    public void addFirst(E e){
        head = new Node<>(e,head);
        if(size == 0)
            tail = head;
        size ++;
    }
    
    //Adds an element to the end of the linked list
    public void addLast(E e){
        Node<E> newest = new Node<>(e,null);
            if(isEmpty())
                head = newest;
            else
                tail.setNext(newest);
            tail = newest;
            size++;     
    }
    
    //Remove the first element of the linked list
    public E removeFirst( ) {
        if (isEmpty( )) return null;
        E answer = head.getElement( );
        head = head.getNext( );
        size --;
        if(size == 0)
            tail = null;
        return answer;
    }

    //Print the elements of the linked list
    public String toString(){
        if(size == 0);
        System.out.println("The Bag is empty.");
        String linked = "";
        Node<E> temp = head;
        for(int i=0; i<size; i++){
            linked+=temp.getElement().toString();
            temp = temp.getNext();
        }
        return getClass().getName()+ "@" + linked;
    }
}
