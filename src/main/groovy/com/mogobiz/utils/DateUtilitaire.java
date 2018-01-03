/*
 * Copyright (C) 2015 Mogobiz SARL. All rights reserved.
 */

package com.mogobiz.utils;

import java.text.DateFormatSymbols;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Cette classe offre des methodes de comparaison de dates qui s'appuie sur les
 * champs de la classe Calendar
 * 
 * @version $Id$
 */
public final class DateUtilitaire
{

    /**
     * Pattern par defaut pour la comparaison. Il permet la comparaison des
     * jours, mois et annees
     */
    public static final String PATTERN_PAR_DEFAUT = "yyyyMMdd";

    /**
     * Pattern pour la comparaison. Il permet la comparaison des
     * milliseconde, seconde, minutes, heures, jours, mois et annees
     */
    public static final String PATTERN_DATE_COMPLET = "yyyyMMddHHmmssSSS";

    /**
     * Constructeur par defaut
     */
    private DateUtilitaire()
    {

    }

    /**
     * Renvoie la date et l'heure actuelle
     * @return : date et heure actuelle
     */
    public static Calendar now()
    {
        return Calendar.getInstance(Locale.FRANCE);
    }

    /**
     * Renvoie la date et l'heure d'hier a la meme heure
     * @return : hier a la meme heure
     */
    public static Calendar yesterday()
    {
        Calendar c = now();
        c.add(Calendar.DAY_OF_MONTH, -1);
        return c;
    }
    
    /**
     * Renvoie la date et l'heure de demain a la meme heure
     * @return : demain a la meme heure
     */
    public static Calendar tomorrow()
    {
        Calendar c = now();
        c.add(Calendar.DAY_OF_MONTH, 1);
        return c;
    }    
    
    /**
     * Renvoie la date correspondant aux informations donnee
     * @param jour : jour du moins
     * @param mois : de 1 a 12
     * @param annee : annee de la date
     * @return : date correspond au jour, mois et annee donnees
     */
    public static Calendar get(int jour, int mois, int annee)
    {
        Calendar c = now();
        c.set(Calendar.YEAR, annee);
        c.set(Calendar.MONTH, mois - 1);
        c.set(Calendar.DAY_OF_MONTH, jour);
        return c;
    }
    
    /**
     * Permet de copier un calendrier
     * @param source : calendrier source
     * @return : copie du calendrier source
     */
    public static Calendar copy(Calendar source)
    {
        Calendar c = now();
        c.setTimeInMillis(source.getTimeInMillis());
        return c;
    }
    
    /**
     * Permet de copier une date
     * @param source : date source
     * @return : copie de la date source
     */
    public static Date copy(Date source)
    {
        Calendar c = now();
        c.setTimeInMillis(source.getTime());
        return c.getTime();
    }
    
    /**
     * Renvoie la date correspondant a la date UTC
     * @param dateUTC : format xxxxxxxxxxx.yyy
     * @return : date correspond au parametre donne
     */
    public static Calendar calendarFromUTC(String dateUTC)
    {
        long valeurDate = (long)(Double.valueOf(dateUTC) * 1000);
        Calendar d = DateUtilitaire.now();
        d.setTimeInMillis(valeurDate);
        return d;
    }
    
    /**
     * Permet de parser une chaine et de renvoyer une date
     * 
     * @param valeur
     *            - valeur
     * @param pattern
     *            - pattern
     * @param symbols
     *            - symbols
     * @return Date
     * @throws java.text.ParseException
     *             -
     */
    public static synchronized Date parseToDate(String valeur, String pattern,
        DateFormatSymbols symbols) throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setDateFormatSymbols(symbols);
        return format.parse(valeur);
    }

    /**
     * Permet de parser une chaine et de renvoyer une date
     *
     * @param valeur
     *            - valeur
     * @param pattern
     *            - pattern
     * @return Date
     * @throws java.text.ParseException
     *             -
     */
    public static synchronized Date parseToDate(String valeur, String pattern)
        throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.parse(valeur);
    }

    /**
     * Permet de parser une chaine et de renvoyer un calendar
     *
     * @param valeur
     *            - valeur
     * @param pattern
     *            - pattern
     * @param symbols
     *            - symbols
     * @return Date
     * @throws java.text.ParseException
     *             -
     */
    public static synchronized Calendar parseToCalendar(String valeur, String pattern,
        DateFormatSymbols symbols) throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setDateFormatSymbols(symbols);
        return dateToCalendar(format.parse(valeur));
    }

    /**
     * Permet de parser une chaine et de renvoyer un calendar.<br/>
     * La chaine doit avoir le format yyyy-MM-ddTHH:mm:ss.SSSZ ou
     * Z = le time zone avec les heures et les minutes separe par : (exemple +01:00)
     *
     * @param valeur
     *            - valeur
     * @param pattern
     *            - pattern
     * @return Calendar
     * @throws java.text.ParseException
     *             -
     */
    public static synchronized Calendar parseToCalendarISO(String valeur)
        throws ParseException
    {
    	if (valeur != null)
    	{
    		if (valeur.charAt(valeur.length() - 3) != ':')
    		{
    			throw new ParseException("Impossible de parser la chaine " + valeur + ". Le format attendu est yyyy-MM-ddTHH:mm:ss.SSSZ", valeur.length() - 3);
    		}

    		String newValeur = valeur.substring(0, valeur.length() - 3) + valeur.substring(valeur.length() - 2);
    		return parseToCalendar(newValeur, "yyyy-MM-dd'T'HH:mm:ss.SSSZ");
    	}
    	else
    	{
    		return null;
    	}
    }

    /**
     * Permet de parser une chaine et de renvoyer un calendar
     *
     * @param valeur
     *            - valeur
     * @param pattern
     *            - pattern
     * @return Calendar
     * @throws java.text.ParseException
     *             -
     */
    public static synchronized Calendar parseToCalendar(String valeur, String pattern)
        throws ParseException
    {
    	if (valeur != null)
    	{
    		SimpleDateFormat format = new SimpleDateFormat(pattern);
    		return dateToCalendar(format.parse(valeur));
    	}
    	else
    	{
    		return null;
    	}
    }

    /**
     * Permet de parser une chaine et de renvoyer un calendar
     *
     * @param valeur
     *            - valeur
     * @param pattern
     *            - pattern
     * @param locale
     *            - locale a utiliser pour le parsage de la date
     * @return Calendar
     * @throws java.text.ParseException
     *             -
     */
    public static synchronized Calendar parseToCalendar(String valeur, String pattern, Locale locale)
        throws ParseException
    {
        SimpleDateFormat format = new SimpleDateFormat(pattern, locale);
        return dateToCalendar(format.parse(valeur));
    }

    /**
     * Permet de formater une date en utilisant des symbols et de facon synchone
     *
     * @param date
     *            - date
     * @param pattern
     *            - pattern
     * @param symbols
     *            - symbols
     * @return date formatee
     */
    public static synchronized String format(Date date, String pattern, DateFormatSymbols symbols)
    {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        format.setDateFormatSymbols(symbols);
        return format.format(date);
    }

    /**
     * Permet de formater une date de facon synchone
     *
     * @param date
     *            - date
     * @param pattern
     *            - pattern
     * @return date formatee
     */
    public static synchronized String format(Date date, String pattern)
    {
        SimpleDateFormat format = new SimpleDateFormat(pattern);
        return format.format(date);
    }

    /**
     * Permet de formater un calendrier en utilisant des symbols et de facon
     * synchone
     *
     * @param date
     *            - date
     * @param pattern
     *            - pattern
     * @param symbols
     *            - symbols
     * @return calendrier formate
     */
    public static synchronized String format(Calendar date, String pattern,
        DateFormatSymbols symbols)
    {
        if (date != null)
        {
            return format(date.getTime(), pattern, symbols);
        }
        else
        {
            return "";
        }
    }

    /**
     * Permet de formater un calendrier de facon synchone
     *
     * @param date
     *            - calendrier
     * @param pattern
     *            - modele
     * @return calendrier formate
     */
    public static synchronized String format(Calendar date, String pattern)
    {
        if (date != null)
        {
            return format(date.getTime(), pattern);
        }
        else
        {
            return "";
        }
    }

    /**
     * Permet de convertir une Date en un Calendar
     *
     * @param date
     *            : date a convertir
     * @return : calendrier issu de la date
     */
    public static Calendar dateToCalendar(Date date)
    {
        if (date == null)
        {
            return null;
        }
        else
        {
            Calendar c = now();
            c.setTime(date);
            return c;
        }
    }

    /**
     * Permet de convertir un Calendar en une Date
     *
     * @param calendrier
     *            : calendrier a convertir
     * @return : date issu du calendrier
     */
    public static Date calendarToDate(Calendar calendrier)
    {
        if (calendrier == null)
        {
            return null;
        }
        else
        {
            return calendrier.getTime();
        }
    }

    /**
     * Cette methode permet recuperer la plus ancienne date entre deux en
     * comparant uniquement les jours, mois et annees.
     *
     * @param date1
     *            Premiere date a comparer
     * @param date2
     *            Deuxiere date a comparer
     * @return La plus ancienne date entre les 2
     */
    public static Calendar min(Calendar date1, Calendar date2)
    {
        if (compareDate(date1, date2) <= 0)
        {
            return date1;
        }
        else
        {
            return date2;
        }
    }

    /**
     * Cette methode permet recuperer la plus recente date entre deux en
     * comparant uniquement les jours, mois et annees.
     *
     * @param date1
     *            Premiere date a comparer
     * @param date2
     *            Deuxiere date a comparer
     * @return La plus recente date entre les 2
     */
    public static Calendar max(Calendar date1, Calendar date2)
    {
        if (compareDate(date1, date2) > 0)
        {
            return date1;
        }
        else
        {
            return date2;
        }
    }

    /**
     * Permet de determiner si <code>date</code> est strictement plus ancienne
     * que la date du jour en comparant uniquement les jours, mois et annees.
     *
     * @param date
     *            date a comparer
     * @return true si <code>date</code> est strictement plus ancienne que la
     *         date du jour
     */
    public static boolean isBefore(Date date)
    {
        return compareDate(date, now().getTime()) < 0;
    }

    /**
     * Permet de determiner si <code>date1</code> est strictement plus ancienne
     * que <code>date2</code> en comparant uniquement les jours, mois et annees.
     *
     * @param date1
     *            Premiere date a comparer
     * @param date2
     *            Deuxieme date a comparer
     * @return true si <code>date1</code> est strictement plus ancienne que
     *         <code>date2</code>
     */
    public static boolean isBefore(Date date1, Date date2)
    {
        return compareDate(date1, date2) < 0;
    }

    /**
     * Permet de determiner si <code>date1</code> est strictement plus ancienne
     * que <code>date2</code> en comparant avec le pattern
     *
     * @param date1
     *            Premiere date a comparer
     * @param date2
     *            Deuxieme date a comparer
     * @param pattern
     *            pattern qui permet de transformer la date en un nombre pour la
     *            comparaison. Le pattern permet de specifier les champs a
     *            prendre en compte
     * @return true si <code>date1</code> est strictement plus ancienne que
     *         <code>date2</code>
     */
    public static boolean isBefore(Date date1, Date date2, String pattern)
    {
        return compareDate(date1, date2, pattern) < 0;
    }

    /**
     * Permet de determiner si <code>date</code> est strictement plus ancienne
     * que la date du jour en comparant uniquement les jours, mois et annees.
     *
     * @param date
     *            date a comparer
     * @return true si <code>date</code> est strictement plus ancienne que la
     *         date du jour
     */
    public static boolean isBefore(Calendar date)
    {
        return compareDate(date, now()) < 0;
    }

    /**
     * Permet de determiner si <code>date1</code> est strictement plus ancienne
     * que <code>date2</code> en comparant uniquement les jours, mois et annees.
     *
     * @param date1
     *            Premiere date a comparer
     * @param date2
     *            Deuxieme date a comparer
     * @return true si <code>date1</code> est strictement plus ancienne que
     *         <code>date2</code>
     */
    public static boolean isBefore(Calendar date1, Calendar date2)
    {
        return compareDate(date1, date2) < 0;
    }

    /**
     * Permet de determiner si <code>date1</code> est strictement plus ancienne
     * que <code>date2</code> en comparant avec le pattern
     *
     * @param date1
     *            Premiere date a comparer
     * @param date2
     *            Deuxieme date a comparer
     * @param pattern
     *            pattern qui permet de transformer la date en un nombre pour la
     *            comparaison. Le pattern permet de specifier les champs a
     *            prendre en compte
     * @return true si <code>date1</code> est strictement plus ancienne que
     *         <code>date2</code>
     */
    public static boolean isBefore(Calendar date1, Calendar date2, String pattern)
    {
        return compareDate(date1, date2, pattern) < 0;
    }

    /**
     * Permet de determiner si <code>date</code> est plus ancienne ou egale a
     * aujourdhui en comparant uniquement les jours, mois et annees.
     *
     * @param date
     *            Premiere date a comparer
     * @return true si <code>date</code> est plus ancienne ou egale a aujourdhui
     */
    public static boolean isBeforeOrEqual(Calendar date)
    {
        return isBeforeOrEqual(date, now());
    }

    /**
     * Permet de determiner si <code>date1</code> est plus ancienne ou egale a
     * <code>date2</code> en comparant uniquement les jours, mois et annees.
     *
     * @param date1
     *            Premiere date a comparer
     * @param date2
     *            Deuxieme date a comparer
     * @return true si <code>date1</code> est plus ancienne ou egale a
     *         <code>date2</code>
     */
    public static boolean isBeforeOrEqual(Calendar date1, Calendar date2)
    {
        return isBefore(date1, date2) || isEqual(date1, date2);
    }

    /**
     * Permet de determiner si <code>date1</code> est plus ancienne ou egale a
     * <code>date2</code> en comparant uniquement les jours, mois et annees.
     *
     * @param date1
     *            Premiere date a comparer
     * @param date2
     *            Deuxieme date a comparer
     * @param pattern
     *            pattern qui permet de transformer la date en un nombre pour la
     *            comparaison. Le pattern permet de specifier les champs a
     *            prendre en compte
     * @return true si <code>date1</code> est plus ancienne ou egale a
     *         <code>date2</code>
     */
    public static boolean isBeforeOrEqual(Calendar date1, Calendar date2, String pattern)
    {
        return isBefore(date1, date2, pattern) || isEqual(date1, date2, pattern);
    }

    /**
     * Permet de determiner si <code>date</code> est plus ancienne ou egale a
     * aujourdhui en comparant uniquement les jours, mois et annees.
     *
     * @param date
     *            Premiere date a comparer
     * @return true si <code>date</code> est plus ancienne ou egale a aujourdhui
     */
    public static boolean isBeforeOrEqual(Date date)
    {
        return isBeforeOrEqual(date, now().getTime());
    }

    /**
     * Permet de determiner si <code>date1</code> est plus ancienne ou egale a
     * <code>date2</code> en comparant uniquement les jours, mois et annees.
     *
     * @param date1
     *            Premiere date a comparer
     * @param date2
     *            Deuxieme date a comparer
     * @return true si <code>date1</code> est plus ancienne ou egale a
     *         <code>date2</code>
     */
    public static boolean isBeforeOrEqual(Date date1, Date date2)
    {
        return isBefore(date1, date2) || isEqual(date1, date2);
    }

    /**
     * Permet de determiner si <code>date1</code> est plus ancienne ou egale a
     * <code>date2</code> en comparant uniquement les jours, mois et annees.
     *
     * @param date1
     *            Premiere date a comparer
     * @param date2
     *            Deuxieme date a comparer
     * @param pattern
     *            pattern qui permet de transformer la date en un nombre pour la
     *            comparaison. Le pattern permet de specifier les champs a
     *            prendre en compte
     * @return true si <code>date1</code> est plus ancienne ou egale a
     *         <code>date2</code>
     */
    public static boolean isBeforeOrEqual(Date date1, Date date2, String pattern)
    {
        return isBefore(date1, date2, pattern) || isEqual(date1, date2, pattern);
    }

    /**
     * Permet de determiner si <code>date</code> est egale a aujourdhui en
     * comparant uniquement les jours, mois et annees.
     *
     * @param date
     *            date a comparer
     * @return true si <code>date</code> est egale a aujourdhui
     */
    public static boolean isEqual(Date date)
    {
        return compareDate(date, now().getTime()) == 0;
    }

    /**
     * Permet de determiner si <code>date1</code> est egale a <code>date2</code>
     * en comparant uniquement les jours, mois et annees.
     *
     * @param date1
     *            Premiere date a comparer
     * @param date2
     *            Deuxieme date a comparer
     * @return true si <code>date1</code> est egale a <code>date2</code>
     */
    public static boolean isEqual(Date date1, Date date2)
    {
        return compareDate(date1, date2) == 0;
    }

    /**
     * Permet de determiner si <code>date1</code> est egale a <code>date2</code>
     * en comparant uniquement les jours, mois et annees.
     *
     * @param date1
     *            Premiere date a comparer
     * @param date2
     *            Deuxieme date a comparer
     * @param pattern
     *            pattern qui permet de transformer la date en un nombre pour la
     *            comparaison. Le patterne permet de specifier les champs a
     *            prendre en compte
     * @return true si <code>date1</code> est egale a <code>date2</code>
     */
    public static boolean isEqual(Date date1, Date date2, String pattern)
    {
        return compareDate(date1, date2, pattern) == 0;
    }

    /**
     * Permet de determiner si <code>date</code> est egale a aujourdhui en
     * comparant uniquement les jours, mois et annees.
     *
     * @param date
     *            date a comparer
     * @return true si <code>date</code> est egale a aujourdhui
     */
    public static boolean isEqual(Calendar date)
    {
        return isEqual(date, now());
    }

    /**
     * Permet de determiner si <code>date1</code> est egale a <code>date2</code>
     * en comparant uniquement les jours, mois et annees.
     *
     * @param date1
     *            Premiere date a comparer
     * @param date2
     *            Deuxieme date a comparer
     * @return true si <code>date1</code> est egale a <code>date2</code>
     */
    public static boolean isEqual(Calendar date1, Calendar date2)
    {
        return compareDate(date1, date2) == 0;
    }

    /**
     * Permet de determiner si <code>date1</code> est egale a <code>date2</code>
     * en comparant uniquement les jours, mois et annees.
     *
     * @param date1
     *            Premiere date a comparer
     * @param date2
     *            Deuxieme date a comparer
     * @param pattern
     *            pattern qui permet de transformer la date en un nombre pour la
     *            comparaison. Le patterne permet de specifier les champs a
     *            prendre en compte
     * @return true si <code>date1</code> est egale a <code>date2</code>
     */
    public static boolean isEqual(Calendar date1, Calendar date2, String pattern)
    {
        return compareDate(date1, date2, pattern) == 0;
    }

    /**
     * Permet de determiner si <code>date</code> est strictement plus recente
     * que la date du jour en comparant uniquement les jours, mois et annees.
     *
     * @param date
     *            date a comparer
     * @return true si <code>date</code> est strictement plus recente que la
     *         date du jour
     */
    public static boolean isAfter(Calendar date)
    {
        return isAfter(date, now());
    }

    /**
     * Permet de determiner si <code>date1</code> est strictement plus recente
     * que <code>date2</code> en comparant uniquement les jours, mois et annees.
     *
     * @param date1
     *            Premiere date a comparer
     * @param date2
     *            Deuxieme date a comparer
     * @return true si <code>date1</code> est strictement plus recente que
     *         <code>date2</code>
     */
    public static boolean isAfter(Calendar date1, Calendar date2)
    {
        return compareDate(date1, date2) > 0;
    }

    /**
     * Permet de determiner si <code>date1</code> est strictement plus recente
     * que <code>date2</code> en comparant avec le pattern
     *
     * @param date1
     *            Premiere date a comparer
     * @param date2
     *            Deuxieme date a comparer
     * @param pattern
     *            pattern qui permet de transformer la date en un nombre pour la
     *            comparaison. Le pattern permet de specifier les champs a
     *            prendre en compte
     * @return true si <code>date1</code> est strictement plus recente que
     *         <code>date2</code>
     */
    public static boolean isAfter(Calendar date1, Calendar date2, String pattern)
    {
        return compareDate(date1, date2, pattern) > 0;
    }

    /**
     * Permet de determiner si <code>date</code> est strictement plus recente
     * que la date du jour en comparant uniquement les jours, mois et annees.
     *
     * @param date
     *            date a comparer
     * @return true si <code>date</code> est strictement plus recente que la
     *         date du jour
     */
    public static boolean isAfter(Date date)
    {
        return isAfter(date, now().getTime());
    }

    /**
     * Permet de determiner si <code>date1</code> est strictement plus recente
     * que <code>date2</code> en comparant uniquement les jours, mois et annees.
     *
     * @param date1
     *            Premiere date a comparer
     * @param date2
     *            Deuxieme date a comparer
     * @return true si <code>date1</code> est strictement plus recente que
     *         <code>date2</code>
     */
    public static boolean isAfter(Date date1, Date date2)
    {
        return compareDate(date1, date2) > 0;
    }

    /**
     * Permet de determiner si <code>date1</code> est strictement plus recente
     * que <code>date2</code> en comparant avec le pattern
     *
     * @param date1
     *            Premiere date a comparer
     * @param date2
     *            Deuxieme date a comparer
     * @param pattern
     *            pattern qui permet de transformer la date en un nombre pour la
     *            comparaison. Le pattern permet de specifier les champs a
     *            prendre en compte
     * @return true si <code>date1</code> est strictement plus recente que
     *         <code>date2</code>
     */
    public static boolean isAfter(Date date1, Date date2, String pattern)
    {
        return compareDate(date1, date2, pattern) > 0;
    }

    /**
     * Permet de determiner si <code>date</code> est plus recente ou egale a
     * ajourdhui en comparant uniquement les jours, mois et annees.
     *
     * @param date
     *            date a comparer
     * @return true si <code>date</code> est plus recente ou egale a ajourdhui
     */
    public static boolean isAfterOrEqual(Calendar date)
    {
        return isAfterOrEqual(date, now());
    }

    /**
     * Permet de determiner si <code>date1</code> est plus recente ou egale a
     * <code>date2</code> en comparant uniquement les jours, mois et annees.
     *
     * @param date1
     *            Premiere date a comparer
     * @param date2
     *            Deuxieme date a comparer
     * @return true si <code>date1</code> est plus recente ou egale a
     *         <code>date2</code>
     */
    public static boolean isAfterOrEqual(Calendar date1, Calendar date2)
    {
        return isAfter(date1, date2) || isEqual(date1, date2);
    }

    /**
     * Permet de determiner si <code>date1</code> est plus recente ou egale a
     * <code>date2</code> en comparant avec le pattern.
     *
     * @param date1
     *            Premiere date a comparer
     * @param date2
     *            Deuxieme date a comparer
     * @param pattern
     *            - modele
     * @return true si <code>date1</code> est plus recente ou egale a
     *         <code>date2</code>
     */
    public static boolean isAfterOrEqual(Calendar date1, Calendar date2, String pattern)
    {
        return isAfter(date1, date2, pattern) || isEqual(date1, date2, pattern);
    }

    /**
     * Permet de determiner si <code>date</code> est plus recente ou egale a
     * ajourdhui en comparant uniquement les jours, mois et annees.
     *
     * @param date
     *            date a comparer
     * @return true si <code>date</code> est plus recente ou egale a ajourdhui
     */
    public static boolean isAfterOrEqual(Date date)
    {
        return isAfterOrEqual(date, now().getTime());
    }

    /**
     * Permet de determiner si <code>date1</code> est plus recente ou egale a
     * <code>date2</code> en comparant uniquement les jours, mois et annees.
     *
     * @param date1
     *            Premiere date a comparer
     * @param date2
     *            Deuxieme date a comparer
     * @return true si <code>date1</code> est plus recente ou egale a
     *         <code>date2</code>
     */
    public static boolean isAfterOrEqual(Date date1, Date date2)
    {
        return isAfter(date1, date2) || isEqual(date1, date2);
    }

    /**
     * Permet de determiner si <code>date1</code> est plus recente ou egale a
     * <code>date2</code> en comparant avec le pattern.
     *
     * @param date1
     *            Premiere date a comparer
     * @param date2
     *            Deuxieme date a comparer
     * @param pattern
     *            - modele
     * @return true si <code>date1</code> est plus recente ou egale a
     *         <code>date2</code>
     */
    public static boolean isAfterOrEqual(Date date1, Date date2, String pattern)
    {
        return isAfter(date1, date2, pattern) || isEqual(date1, date2, pattern);
    }

    /**
     * Cette methode permet de comparer 2 dates en utilisant uniquement les
     * champs donnes. Elle renvoie : - 0 si les 2 dates sont nulles ou egales -
     * MinValue si date1 == null et date2 != null - MaxValue si date2 == null et
     * date1 != null - < 0 si date1 est avant date2 - > 0 si date1 est apres
     * date2
     *
     * @param date1
     *            - date1
     * @param date2
     *            - date2
     * @param pattern
     *            - pattern
     * @return long
     */
    public static long compareDate(Calendar date1, Calendar date2, String pattern)
    {
        long ret = 0;
        if (date1 == null && date2 == null)
        {
            ret = 0;
        }
        else if (date1 == null)
        {
            ret = Integer.MIN_VALUE;
        }
        else if (date2 == null)
        {
            ret = Integer.MAX_VALUE;
        }
        else
        {
            ret = compareDate(date1.getTime(), date2.getTime(), pattern);
        }
        return ret;
    }

    /**
     * Cette methode permet de comparer 2 dates en utilisant uniquement les
     * champs donnes. Elle renvoie : - 0 si les 2 dates sont nulles ou egales -
     * MinValue si date1 == null et date2 != null - MaxValue si date2 == null et
     * date1 != null - < 0 si date1 est avant date2 - > 0 si date1 est apres
     * date2
     *
     * @param date1
     *            - date1
     * @param date2
     *            - date2
     * @param pattern
     *            : pattern qui permet de transformer la date en un nombre pour
     *            la comparaison. Le pattern permet de specifier les champs a
     *            prendre en compte
     * @return long
     */
    public static long compareDate(Date date1, Date date2, String pattern)
    {
        long ret = 0;
        if (date1 == null && date2 == null)
        {
            ret = 0;
        }
        else if (date1 == null)
        {
            ret = Integer.MIN_VALUE;
        }
        else if (date2 == null)
        {
            ret = Integer.MAX_VALUE;
        }
        else
        {
            SimpleDateFormat format = new SimpleDateFormat(pattern);
            long v1 = Long.parseLong(format.format(date1));
            long v2 = Long.parseLong(format.format(date2));
            ret = v1 - v2;
        }
        return ret;
    }

    /**
     * Cette methode permet de comparer les dates en utilisant les champs annee,
     * mois et jour
     *
     * {@inheritDoc}
     *
     * @see com.mogobiz.utils.DateUtilitaire#compareDate(java.util.Date, java.util.Date, String);
     */
    public static long compareDate(Calendar date1, Calendar date2)
    {
        return compareDate(date1, date2, PATTERN_PAR_DEFAUT);
    }

    /**
     * Cette methode permet de comparer les dates en utilisant les champs annee,
     * mois et jour
     *
     * @see com.mogobiz.utils.DateUtilitaire#compareDate(java.util.Calendar, java.util.Calendar, String);
     *
     * @param date
     *            - calendrier
     * @return long
     */
    public static long compareDateANow(Calendar date)
    {
        return compareDate(date, Calendar.getInstance(), PATTERN_PAR_DEFAUT);
    }

    /**
     * Cette methode permet de comparer les dates en utilisant les champs annee,
     * mois et jour
     *
     * @see com.mogobiz.utils.DateUtilitaire#compareDate(java.util.Calendar, java.util.Calendar, String);
     * @param date
     *            - calendrier
     * @param pattern
     *            - modele
     * @return long
     */
    public static long compareDateANow(Calendar date, String pattern)
    {
        return compareDate(date, Calendar.getInstance(), pattern);
    }

    /**
     * Cette methode permet de comparer les dates en utilisant les champs annee,
     * mois et jour
     *
     * {@inheritDoc}
     *
     * @see com.mogobiz.utils.DateUtilitaire#compareDate(java.util.Date, java.util.Date, String)
     */
    public static long compareDate(Date date1, Date date2)
    {
        return compareDate(date1, date2, PATTERN_PAR_DEFAUT);
    }

}
