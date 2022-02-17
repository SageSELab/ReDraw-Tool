/*******************************************************************************
 * Copyright (c) 2016, SEMERU
 * All rights reserved.
 *  
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *  
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *  
 * The views and conclusions contained in the software and documentation are those
 * of the authors and should not be interpreted as representing official policies,
 * either expressed or implied, of the FreeBSD Project.
 *******************************************************************************/
/**
 * TestPersistence.java
 * 
 * Created on Jun 19, 2014, 4:14:33 PM
 * 
 */
package edu.semeru.android.core.service;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;

import edu.semeru.android.core.dao.AppDao;
import edu.semeru.android.core.dao.exception.CRUDException;
import edu.semeru.android.core.entity.model.ActionType;
import edu.semeru.android.core.entity.model.App;
import edu.semeru.android.core.entity.model.Class;
import edu.semeru.android.core.entity.model.ComponentAction;
import edu.semeru.android.core.entity.model.GuiComponent;
import edu.semeru.android.core.entity.model.GuiComponentType;
import edu.semeru.android.core.entity.model.Transition;
import edu.semeru.android.core.entity.model.TransitionPK;
import edu.semeru.android.core.helpers.Constants;


/**
 * {Insert class description here}
 * 
 * @author Carlos Bernal
 * @since Jun 19, 2014
 */
public class TestPersistence {

    /**
     * @param args
     */
    public static void main(String[] args) {
	EntityManagerFactory emf = Persistence.createEntityManagerFactory(Constants.DB_SCHEMA);
	EntityManager em = emf.createEntityManager();

	AppDao aifd = new AppDao();
	App app = new App();

	app.setId(1l);
	app.setName("AngryBirds");
	// --
	Class c1 = new Class();
	c1.setId(1l);
	//c1.setApp(app);
	c1.setIsActivity(true);
	c1.setName("my.Class1");
	// --
	Class c2 = new Class();
	c2.setId(2l);
	//c2.setApp(app);
	c2.setIsActivity(true);
	c2.setName("my.Class2");
	// --
	// --
	Class c3 = new Class();
	c3.setId(3l);
	//c3.setApp(app);
	c3.setIsActivity(false);
	c3.setName("android.Button");
	// --
	ComponentAction comp = new ComponentAction();
	comp.setId(1l);
	ActionType action = new ActionType();
	action.setId(1l);
	action.setName("LongTouch");
	comp.setActionType(action);
	GuiComponent gui = new GuiComponent();
	gui.setId(1l);
	//gui.setNameVariable("myButton");
	//gui.setClazz(c3);
	GuiComponentType guiType = new GuiComponentType();
	guiType.setId(1l);
	gui.setGuiComponentType(guiType);
	comp.setGuiComponent(gui);
	// --
	Transition tr = new Transition();
	TransitionPK trPK = new TransitionPK();
	trPK.setClassIdSource(c1.getId());
	trPK.setClassIdTarget(c2.getId());
	trPK.setComponentActionId(comp.getId());
	tr.setId(trPK);
	// --
	//app.addClazz(c1);
	//app.addClazz(c2);
	//app.addClazz(c3);
	tr.setClassSource(c1);
	tr.setClassTarget(c2);
	tr.setComponentAction(comp);
	c1.addTransitionsSource(tr);
	c2.addTransitionsTarget(tr);
	// --
	em.getTransaction().begin();
	try {
	    aifd.save(app, em);
	} catch (CRUDException e) {
	    e.printStackTrace();
	}
	em.getTransaction().commit();

    }
}
