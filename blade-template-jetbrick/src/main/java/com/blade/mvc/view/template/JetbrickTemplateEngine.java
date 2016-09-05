/**
 * Copyright (c) 2016, biezhi 王爵 (biezhi.me@gmail.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * 	http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.blade.mvc.view.template;

import java.io.Writer;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;

import com.blade.Blade;
import com.blade.context.WebApplicationContext;

import com.blade.kit.StringKit;
import com.blade.mvc.http.Request;
import com.blade.mvc.http.wrapper.Session;
import com.blade.mvc.view.ModelAndView;
import jetbrick.template.*;
import jetbrick.template.TemplateException;
import jetbrick.template.resolver.GlobalResolver;
import jetbrick.template.web.JetWebEngine;

/**
 * JetbrickTemplateEngine
 * 
 * @author	<a href="mailto:biezhi.me@gmail.com" target="_blank">biezhi</a>
 * @since	1.0
 */
public class JetbrickTemplateEngine implements TemplateEngine {

	private JetEngine jetEngine;
	private Properties config;
	private String suffix = ".html";

	public JetbrickTemplateEngine() {
		config = new Properties();
		config.put(JetConfig.TEMPLATE_SUFFIX, suffix);
		if(StringKit.isNotBlank(Blade.$().config().getBasePackage())){
			config.put(JetConfig.AUTOSCAN_PACKAGES, Blade.$().config().getBasePackage());
		}
		String $classpathLoader = "jetbrick.template.loader.ClasspathResourceLoader";
		config.put(JetConfig.TEMPLATE_LOADERS, "$classpathLoader");
		config.put("$classpathLoader", $classpathLoader);
		config.put("$classpathLoader.root", "/templates/");
		config.put("$classpathLoader.reloadable", true);

		jetEngine = JetEngine.create(config);
	}

	public JetbrickTemplateEngine(Properties config) {
		this.config = config;
		jetEngine = JetEngine.create(config);
	}
	
	public JetbrickTemplateEngine(ServletContext servletContext) {
		jetEngine = JetWebEngine.create(servletContext);
	}

	public JetbrickTemplateEngine(String conf) {
		jetEngine = JetEngine.create(conf);
	}

	public JetbrickTemplateEngine(JetEngine jetEngine) {
		if (null == jetEngine) {
			throw new IllegalArgumentException("jetEngine must not be null");
		}
		this.jetEngine = jetEngine;
	}

	@Override
	public void render(ModelAndView modelAndView, Writer writer) throws TemplateException {

		Map<String, Object> modelMap = modelAndView.getModel();
		
		Request request = WebApplicationContext.request();
		Session session = request.session();

		Set<String> attrs = request.attributes();
		if (null != attrs && attrs.size() > 0) {
			for (String attr : attrs) {
				modelMap.put(attr, request.attribute(attr));
			}
		}

		Set<String> session_attrs = session.attributes();
		if (null != session_attrs && session_attrs.size() > 0) {
			for (String attr : session_attrs) {
				modelMap.put(attr, session.attribute(attr));
			}
		}

		JetContext context = new JetContext(modelMap.size());
		context.putAll(modelMap);

		String templateName = modelAndView.getView().endsWith(suffix) ? modelAndView.getView() : modelAndView.getView() + suffix;

		JetTemplate template = jetEngine.getTemplate(templateName);
		template.render(context, writer);
	}

	public JetEngine getJetEngine() {
		return jetEngine;
	}

	public JetGlobalContext getGlobalContext(){
		return jetEngine.getGlobalContext();
	}

	public GlobalResolver getGlobalResolver(){
		return jetEngine.getGlobalResolver();
	}

	public Properties getConfig() {
		return config;
	}

	public String getSuffix() {
		return suffix;
	}

	public void setSuffix(String suffix) {
		this.suffix = suffix;
	}
}