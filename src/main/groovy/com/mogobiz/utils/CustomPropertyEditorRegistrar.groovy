package com.mogobiz.utils

import org.springframework.beans.PropertyEditorRegistry
public class CustomPropertyEditorRegistrar {
	 def dateEditor

	    void registerCustomEditors(PropertyEditorRegistry registry) {
	        registry.registerCustomEditor(Date.class, dateEditor)
	    }
}
