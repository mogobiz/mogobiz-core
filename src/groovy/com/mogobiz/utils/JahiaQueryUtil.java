package com.mogobiz.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JahiaQueryUtil {

	public static String transformeJahiaOrderBy(String query)
	{
		String resultat = query;
		if (query != null)
		{
			resultat = removePrefixe(resultat);
		}
		if (resultat == null) {
			resultat = "";
		}
		if (resultat.length() > 0) {
			resultat = " ORDER BY " + resultat;
		}
		return resultat;
	}

	public static String transformeJahiaQuery(String query)
	{
		String resultat = query;
		if (query != null)
		{
			resultat = removePrefixe(resultat);
			resultat = removeLanguage(resultat);
			resultat = transformeCast(resultat);
			resultat = clearEmptyOperand(resultat);
		}
		return resultat;
	}
	
	public static String removePrefixe(String query)
	{
		return query.replaceAll("ebiznt:", "");
	}
	
	public static String removeLanguage(String query)
	{
		String resultat = query;
		resultat = resultat.replaceAll("\\([a-zA-Z]+\\.\\[jcr:language\\] IS NOT NULL\\)", "");
		resultat = resultat.replaceAll("\\([a-zA-Z]+\\.\\[jcr:language\\] = '[a-zA-Z_]+'\\)", "");
		resultat = resultat.replaceAll("[a-zA-Z]+\\.\\[jcr:language\\] IS NOT NULL", "");
		resultat = resultat.replaceAll("[a-zA-Z]+\\.\\[jcr:language\\] = '[a-zA-Z_]+'", "");
		return resultat;
	}
		
	public static String transformeCast(String query)
	{
		String resultat = query;
		resultat = resultat.replaceAll("CAST\\('true' AS BOOLEAN\\)", "true");
		resultat = resultat.replaceAll("CAST\\('false' AS BOOLEAN\\)", "false");
		
		Matcher m = getMatcher(resultat, "^(.*)CAST\\('(.*)T(.*)' AS DATE\\)(.*)$");
		while (m.matches())
		{
			resultat = m.group(1) + "'" + m.group(2) + " " + m.group(3) + "'" + m.group(4);
			m = getMatcher(resultat, "^(.*)CAST\\('(.*)T(.*)' AS DATE\\)(.*)$");
		}
		return resultat;
	}
	
	public static String clearEmptyOperand(String query)
	{
		String resultat = query;
		// suppression des parenthèses vides ()
		resultat = resultat.replaceAll("\\(\\)", "");
		
		// suppression des NOT vides
		Matcher m = getMatcher(resultat, "^(.*)\\(NOT \\)(.*)$");
		while (m.matches())
		{
			resultat = m.group(1) + m.group(2);
			m = getMatcher(resultat, "^(.*)\\(NOT \\)(.*)$");
		}
		m = getMatcher(resultat, "^(.*)NOT $");
		while (m.matches())
		{
			resultat = m.group(1);
			m = getMatcher(resultat, "^(.*)NOT $");
		}		

		// suppression des AND vides
		m = getMatcher(resultat, "^(.*)\\((.*) AND \\)(.*)$");
		while (m.matches())
		{
			resultat = m.group(1) + m.group(2) + m.group(3);
			m = getMatcher(resultat, "^(.*)\\((.*) AND \\)(.*)$");
		}
		m = getMatcher(resultat, "^(.*)\\( AND (.*)\\)(.*)$");
		while (m.matches())
		{
			resultat = m.group(1) + m.group(2) + m.group(3);
			m = getMatcher(resultat, "^(.*)\\( AND (.*)\\)(.*)$");
		}
		m = getMatcher(resultat, "^(.*) AND $");
		while (m.matches())
		{
			resultat = m.group(1);
			m = getMatcher(resultat, "^(.*) AND $");
		}
		m = getMatcher(resultat, "^ AND (.*)$");
		while (m.matches())
		{
			resultat = m.group(1);
			m = getMatcher(resultat, "^ AND (.*)$");
		}
		
		// suppression des OR vides
		m = getMatcher(resultat, "^(.*)\\((.*) OR \\)(.*)$");
		while (m.matches())
		{
			resultat = m.group(1) + m.group(2) + m.group(3);
			m = getMatcher(resultat, "^(.*)\\((.*) OR \\)(.*)$");
		}		
		m = getMatcher(resultat, "^(.*)\\( OR (.*)\\)(.*)$");
		while (m.matches())
		{
			resultat = m.group(1) + m.group(2) + m.group(3);
			m = getMatcher(resultat, "^(.*)\\( OR (.*)\\)(.*)$");
		}		
		m = getMatcher(resultat, "^(.*) OR $");
		while (m.matches())
		{
			resultat = m.group(1);
			m = getMatcher(resultat, "^(.*) OR $");
		}		
		m = getMatcher(resultat, "^ OR (.*)$");
		while (m.matches())
		{
			resultat = m.group(1);
			m = getMatcher(resultat, "^ OR (.*)$");
		}
		
		if (resultat.equals(query))
		{
			// pas de changement, la requête est nettoyée
			return resultat;
		}
		else
		{
			// il y a eu des changements, on relance un nettoyage
			// pour nettoyer d'autres éléments apparus avec le nettoyage
			return clearEmptyOperand(resultat);
		}
	}
	
	private static Matcher getMatcher(String source, String expReg)
	{
		Pattern p = Pattern.compile(expReg);
		return p.matcher(source);
	}
}
