/*******************************************************************************
 * Copyright (c) 2008, 2010 VMware Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   VMware Inc. - initial contribution
 *******************************************************************************/
package net.flybyte.virgo.maven.helper;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MavenVersionNumberConverter {
	private static final Pattern VERSION_PATTERN = Pattern
			.compile("(\\d+)(\\.\\d+)?(\\.\\d+)?([\\.-]{1}.*)?");

	public static String convertToOsgi(String mavenVersionNumber) {
		VersionBuilder versionBuilder = new VersionBuilder();
		Matcher matcher = VERSION_PATTERN.matcher(mavenVersionNumber);
		matcher.find();
		versionBuilder.setMajor(matcher.group(1));
		versionBuilder.setMinor(matcher.group(2));
		versionBuilder.setMicro(matcher.group(3));
		versionBuilder.setQualifier(matcher.group(4));
		return versionBuilder.getVersionString();
	}

	private static class VersionBuilder {
		private volatile String major = "0";
		private volatile String minor = "0";
		private volatile String micro = "0";
		private volatile String qualifier = null;

		public void setMajor(String major) {
			if (major != null) {
				this.major = major;
			}
		}

		public void setMinor(String minor) {
			if (minor != null) {
				this.minor = trim(minor);
			}
		}

		public void setMicro(String micro) {
			if (micro != null) {
				this.micro = trim(micro);
			}
		}

		public void setQualifier(String qualifier) {
			if (qualifier != null) {
				this.qualifier = trim(qualifier).replaceAll("\\.", "_");
			}
		}

		public String getVersionString() {
			if (qualifier == null) {
				return String.format("%s.%s.%s", major, minor, micro);
			}
			return String.format("%s.%s.%s.%s", major, minor, micro, qualifier);
		}

		private String trim(String s) {
			return s.substring(1);
		}
	}
}
