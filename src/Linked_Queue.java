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
public class Linked_Queue <E> implements Queue <E> {
    private Singly_Linked_List<E> list = new Singly_Linked_List<>(); //an empty list
    public Linked_Queue(){} //new queue relies on intially empty list
    @Override
    public int size(){return list.size();}
    @Override
    public boolean isEmpty(){return list.isEmpty();}
    @Override
    public void enqueue(E element){list.addLast(element);}
    @Override
    public E first(){return list.first();}
    @Override
    public E dequeue(){return list.removeFirst();}
}
