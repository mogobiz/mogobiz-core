package com.mogobiz.jndi

import grails.util.GrailsUtil

import javax.naming.Context
import javax.naming.InitialContext;
import javax.naming.NamingException

import com.mogobiz.jndi.java.javaURLContextFactory;

class IperJndiContext extends InitialContext {

	private static final String PREFIXE_CXT = "java:/comp/env/";
	
	/**
	*
	*/
   public IperJndiContext()
   {
	   javaURLContextFactory.setContext(this);
	   System.setProperty(Context.INITIAL_CONTEXT_FACTORY, javaURLContextFactory.class.getName());
	   String p = IperJndiContext.class.getPackage().getName();
	   System.setProperty(Context.URL_PKG_PREFIXES, p);	  
   }  
      
	@Override
	public void bind(String name, Object obj) throws NamingException {
		if (GrailsUtil.environment == "development" && name.startsWith(PREFIXE_CXT))
		{
			name = name.substring(PREFIXE_CXT.length());
		}
		String [] tab = name.split("/");
		if (tab.length == 1)
		{
			super.bind(name, obj);
		}
		else
		{
			Context context = this;
			for (int i = 0; i < tab.length; i++)
			{
				if (i == tab.length - 1)
				{
					context.bind(tab[i], obj);
				}
				else
				{
					if (!isBind(context, tab[i]))
					{
						context = context.createSubcontext(tab[i]);
					}
					else
					{
						context = (Context)context.lookup(tab[i]);
					}
				}
			}
		}

	}	
	
	@Override
	public Object lookup(String name) throws NamingException {
		if (GrailsUtil.environment == "development" && name.startsWith(PREFIXE_CXT))
		{
			name = name.substring(PREFIXE_CXT.length());
		}
		//return super.lookup(name);
		String [] tab = name.split("/");
		if (tab.length == 1)
		{
			return super.lookup(tab[0]);
		}
		else
		{
			Context context = this;
			for (int i = 0; i < tab.length; i++)
			{
				if (i == tab.length - 1)
				{
					return context.lookup(tab[i]);
				}
				else
				{
					if (!isBind(context, tab[i]))
					{
						context = context.createSubcontext(tab[i]);
					}
					else
					{
						context = (Context)context.lookup(tab[i]);
					}
				}
			}
			return null;
		}
	}

	private static boolean isBind(Context c, String name)
	{
		try
		{
			return c.lookup(name) != null;
		}
		catch (Exception ex)
		{
			return false;
		}
	}
}
