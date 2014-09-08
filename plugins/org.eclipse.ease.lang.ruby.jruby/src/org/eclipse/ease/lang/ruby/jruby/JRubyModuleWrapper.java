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

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.eclipse.ease.Activator;
import org.eclipse.ease.Logger;
import org.eclipse.ease.modules.AbstractModuleWrapper;
import org.eclipse.ease.modules.IEnvironment;
import org.eclipse.ease.modules.IScriptFunctionModifier;

public class JRubyModuleWrapper extends AbstractModuleWrapper {

	public static List<String> RESERVED_KEYWORDS = new ArrayList<String>();

	static {
		// TODO set keywords
		// RESERVED_KEYWORDS.add("abstract");
	}

	@Override
	public String getSaveVariableName(final String variableName) {
		return getSaveName(variableName);
	}

	@Override
	public String createFunctionWrapper(final IEnvironment environment, final String moduleVariable, final Method method) {

		StringBuilder rubyScriptCode = new StringBuilder();

		// parse parameters
		List<Parameter> parameters = parseParameters(method);

		// build parameter string
		StringBuilder parameterList = new StringBuilder();
		for (Parameter parameter : parameters)
			parameterList.append(", ").append(parameter.getName());

		if (parameterList.length() > 2)
			parameterList.delete(0, 2);

		StringBuilder body = new StringBuilder();
		// insert parameter checks
		body.append(verifyParameters(parameters));

		// insert hooked pre execution code
		body.append(getPreExecutionCode(environment, method));

		// insert method call
		body.append("\t").append(IScriptFunctionModifier.RESULT_NAME).append(" = ").append(moduleVariable).append(".").append(method.getName()).append("(");
		body.append(parameterList);
		body.append(");\n");

		// insert hooked post execution code
		body.append(getPostExecutionCode(environment, method));

		// insert return statement
		body.append("\treturn ").append(IScriptFunctionModifier.RESULT_NAME).append(";\n");

		// build function declarations
		for (String name : getMethodNames(method)) {
			if (!isValidMethodName(name)) {
				Logger.logError("The method name \"" + name + "\" from the module \"" + moduleVariable + "\" can not be wrapped because it's name is reserved",
						Activator.PLUGIN_ID);

			} else if (!name.isEmpty()) {
				rubyScriptCode.append("def ").append(name).append("(").append(parameterList).append(")\n");
				rubyScriptCode.append(body);
				rubyScriptCode.append("end\n");
			}
		}

		return rubyScriptCode.toString();
	}

	private StringBuilder verifyParameters(final List<Parameter> parameters) {
		StringBuilder data = new StringBuilder();

		// FIXME currently not supported

		return data;
	}

	@Override
	public String classInstantiation(final Class<?> clazz, final String[] parameters) {
		StringBuilder code = new StringBuilder();
		code.append(clazz.getName());
		code.append(".new(");

		if (parameters != null) {
			for (String parameter : parameters) {
				code.append('"');
				code.append(parameter);
				code.append('"');
				code.append(", ");
			}
			if (parameters.length > 0)
				code.replace(code.length() - 2, code.length(), "");
		}

		code.append(")");

		return code.toString();
	}

	private static boolean isValidMethodName(final String methodName) {
		return isSaveName(methodName) && !RESERVED_KEYWORDS.contains(methodName);
	}

	private static boolean isSaveName(final String identifier) {
		return Pattern.matches("[a-zA-Z_$][a-zA-Z0-9_$]*", identifier);
	}

	public static String getSaveName(final String identifier) {
		// check if name is already valid
		if (isSaveName(identifier))
			return identifier;

		// not valid, convert string to valid format
		final StringBuilder buffer = new StringBuilder(identifier.replaceAll("[^a-zA-Z0-9]", "_"));

		// remove '_' at the beginning
		while ((buffer.length() > 0) && (buffer.charAt(0) == '_'))
			buffer.deleteCharAt(0);

		// remove trailing '_'
		while ((buffer.length() > 0) && (buffer.charAt(buffer.length() - 1) == '_'))
			buffer.deleteCharAt(buffer.length() - 1);

		// check for valid first character
		if (buffer.length() > 0) {
			final char start = buffer.charAt(0);
			if ((start < 65) || ((start > 90) && (start < 97)) || (start > 122))
				buffer.insert(0, '_');
		} else
			// buffer is empty
			buffer.insert(0, '_');

		return buffer.toString();
	}

	@Override
	public String createStaticFieldWrapper(final IEnvironment environment, final Field field) {
		StringBuilder rubyCode = new StringBuilder();
		rubyCode.append(getSaveVariableName(field.getName())).append(" = ");
		rubyCode.append(field.getDeclaringClass().getName()).append(".").append(field.getName());

		return rubyCode.toString();
	}

	@Override
	protected String getNullString() {
		return "nil";
	}
}
