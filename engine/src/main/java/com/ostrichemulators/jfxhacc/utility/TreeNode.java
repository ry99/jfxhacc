/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.utility;

import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;

/**
 *
 * @author ryan
 */
public class TreeNode<T> {

	private T node;
	private final List<TreeNode<T>> children = new ArrayList<>();

	public TreeNode( T n ) {
		node = n;
	}

	public TreeNode() {
		this( null );
	}

	public List<TreeNode<T>> getChildNodes() {
		return new ArrayList<>( children );
	}

	public List<T> getChildren() {
		List<T> childs = new ArrayList<>();
		for ( TreeNode<T> t : children ) {
			childs.add( t.node );
		}

		return childs;
	}

	public void setChildren( Collection<T> c ) {
		children.clear();
		for ( T t : c ) {
			children.add( new TreeNode<>( t ) );
		}
	}

	public TreeNode<T> addChild( T e ) {
		TreeNode<T> n = new TreeNode<>( e );
		children.add( n );
		return n;
	}

	public void addChild( TreeNode<T> t ) {
		children.add( t );
	}

	public TreeNode<T> removeChild( T e ) {
		int idx = -1;
		for ( TreeNode<T> child : children ) {
			idx++;

			if ( null == child.node ) {
				if ( null == e ) {
					break;
				}
			}
			if ( child.node.equals( e ) ) {
				break;
			}
		}

		return ( idx < 0 ? null : children.remove( idx ) );
	}

	public T getNode() {
		return node;
	}

	public void setNode( T t ) {
		node = t;
	}

	public TreeNode<T> findChild( T t ) {
		Deque<TreeNode<T>> todo = new ArrayDeque<>();
		todo.add( this );
		while ( !todo.isEmpty() ) {
			TreeNode<T> possible = todo.poll();
			if ( null == possible.node ) {
				// we have a null node, and we're looking for a null node
				if ( null == t ) {
					return possible;
				}
			}
			else if ( possible.node.equals( t ) ) {
				return possible;
			}

			todo.addAll( possible.getChildNodes() );
		}

		// not found!
		return null;
	}

	public static <T> void dump( TreeNode<T> t, PrintWriter writer, int depth ) {
		StringBuilder sb = new StringBuilder();
		for ( int i = 0; i < depth; i++ ) {
			sb.append( "  " );
		}

		sb.append( null == t.node ? "<null>" : t.node.toString() );
		writer.println( sb );

		for ( TreeNode<T> child : t.getChildNodes() ) {
			dump( child, writer, depth + 1 );
		}

		writer.flush();
	}
}
