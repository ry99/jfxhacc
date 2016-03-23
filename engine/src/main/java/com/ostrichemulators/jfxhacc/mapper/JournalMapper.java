/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.ostrichemulators.jfxhacc.mapper;

import com.ostrichemulators.jfxhacc.model.Journal;
import javafx.collections.ObservableList;

/**
 *
 * @author ryan
 */
public interface JournalMapper extends DataMapper<Journal> {

	public Journal create( String name ) throws MapperException;

	public ObservableList<Journal> getObservable();
}
