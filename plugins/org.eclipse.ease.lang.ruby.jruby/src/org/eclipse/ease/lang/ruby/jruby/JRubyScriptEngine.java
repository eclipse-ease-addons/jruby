/*******************************************************************************
 * Copyright (c) 2013 Christian Pontesegger and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Christian Pontesegger - initial API and implementation
 *******************************************************************************/
package org.eclipse.ease.lang.ruby.jruby;

import java.net.URL;
import java.util.Map;

import org.eclipse.ease.AbstractScriptEngine;
import org.eclipse.ease.Script;
import org.jruby.embed.LocalVariableBehavior;
import org.jruby.embed.ScriptingContainer;

public class JRubyScriptEngine extends AbstractScriptEngine {

	private ScriptingContainer fEngine;

	public JRubyScriptEngine() {
		super("JRuby");
	}

	@Override
	public Object getExecutedFile() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void terminateCurrent() {
	}

	@Override
	protected boolean setupEngine() {
		fEngine = new ScriptingContainer(LocalVariableBehavior.PERSISTENT);

		fEngine.setOutput(getOutputStream());
		fEngine.setError(getErrorStream());
		fEngine.setInput(getInputStream());

		return true;
	}

	@Override
	protected boolean teardownEngine() {
		return true;
	}

	@Override
	protected Object execute(final Script script, final Object reference, final String fileName, final boolean uiThread) throws Exception {
		return fEngine.runScriptlet(script.getCodeStream(), fileName);
	}

	@Override
	public void setVariable(final String name, final Object content) {
		fEngine.put(name.startsWith("$") ? name : "$" + name, content);
	}

	@Override
	public Object getVariable(final String name) {
		return fEngine.get(name.startsWith("$") ? name : "$" + name);
	}

	@Override
	public boolean hasVariable(final String name) {
		throw new RuntimeException("Oeration not supported");
		// return mEngine.getVarMap().containsKey(name);
	}

	@Override
	public String getSaveVariableName(final String name) {
		throw new RuntimeException("Oeration not supported");
	}

	@Override
	public Object removeVariable(final String name) {
		throw new RuntimeException("Oeration not supported");
	}

	@Override
	public Map<String, Object> getVariables() {
		throw new RuntimeException("Oeration not supported");
	}

	@Override
	public void registerJar(final URL url) {
		throw new RuntimeException("Oeration not supported");
	}
}
