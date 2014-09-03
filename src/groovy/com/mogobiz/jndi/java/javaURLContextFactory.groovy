package com.mogobiz.jndi.java

import javax.naming.Context;
import javax.naming.Name;
import javax.naming.NamingException;
import javax.naming.spi.InitialContextFactory;
import javax.naming.spi.ObjectFactory;

class javaURLContextFactory implements InitialContextFactory, ObjectFactory {
	
	/**
	*
	*/
   private static Context ctx;

    /**
     * @see javax.naming.spi.ObjectFactory#getObjectInstance(java.lang.Object, javax.naming.Name, javax.naming.Context, java.util.Hashtable)
     */
	@Override
	public Object getObjectInstance(Object obj, Name name, Context nameCtx,
			  Hashtable<?, ?> environment) throws Exception {
	  return ctx;
	}
	  
   /**
	* {@inheritDoc}
	*
	* @see javax.naming.spi.InitialContextFactory#getInitialContext(java.util.Hashtable)
	*/
   public Context getInitialContext(Hashtable < ?, ? > environment) throws NamingException
   {
	   return ctx;
   }
   
   /**
	* @param context
	*            - context
	*/
   protected static void setContext(Context context)
   {
	   ctx = context;
   }

}
