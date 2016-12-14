/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper;

import com.ostrichemulators.jfxhacc.model.IDable;
import org.openrdf.model.URI;

/**
 *
 * @author ryan
 * @param <T>
 */
public interface MapperListener<T extends IDable> {

	public void added( T t );

	public void updated( T t );

	public void removed( URI uri );
}
