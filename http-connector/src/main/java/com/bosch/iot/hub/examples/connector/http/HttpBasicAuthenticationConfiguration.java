/*
 * Bosch SI Example Code License Version 1.0, January 2016
 *
 * Copyright 2016 Bosch Software Innovations GmbH ("Bosch SI"). All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * BOSCH SI PROVIDES THE PROGRAM "AS IS" WITHOUT WARRANTY OF ANY KIND, EITHER EXPRESSED OR IMPLIED, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. THE ENTIRE RISK AS TO THE
 * QUALITY AND PERFORMANCE OF THE PROGRAM IS WITH YOU. SHOULD THE PROGRAM PROVE DEFECTIVE, YOU ASSUME THE COST OF ALL
 * NECESSARY SERVICING, REPAIR OR CORRECTION. THIS SHALL NOT APPLY TO MATERIAL DEFECTS AND DEFECTS OF TITLE WHICH BOSCH
 * SI HAS FRAUDULENTLY CONCEALED. APART FROM THE CASES STIPULATED ABOVE, BOSCH SI SHALL BE LIABLE WITHOUT LIMITATION FOR
 * INTENT OR GROSS NEGLIGENCE, FOR INJURIES TO LIFE, BODY OR HEALTH AND ACCORDING TO THE PROVISIONS OF THE GERMAN
 * PRODUCT LIABILITY ACT (PRODUKTHAFTUNGSGESETZ). THE SCOPE OF A GUARANTEE GRANTED BY BOSCH SI SHALL REMAIN UNAFFECTED
 * BY LIMITATIONS OF LIABILITY. IN ALL OTHER CASES, LIABILITY OF BOSCH SI IS EXCLUDED. THESE LIMITATIONS OF LIABILITY
 * ALSO APPLY IN REGARD TO THE FAULT OF VICARIOUS AGENTS OF BOSCH SI AND THE PERSONAL LIABILITY OF BOSCH SI'S EMPLOYEES,
 * REPRESENTATIVES AND ORGANS.
 */
package com.bosch.iot.hub.examples.connector.http;

import java.util.Properties;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.encoding.ShaPasswordEncoder;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.authentication.configurers.provisioning.InMemoryUserDetailsManagerConfigurer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

/**
 * The example HTTP connector service employs basic authentication for HTTP-connected devices. Credentials for devices
 * which shall be authorized to communicate with the HTTP connector are loaded form the {@code credentials.properties}
 * file, passwords should be provided as SHA-256 hashes - please make sure that you have properly configured the list of
 * authorized devices before starting the HTTP connector application.
 */
@Configuration
@EnableWebSecurity
public class HttpBasicAuthenticationConfiguration extends WebSecurityConfigurerAdapter
{

   private static final Logger LOGGER = LoggerFactory.getLogger(HttpBasicAuthenticationConfiguration.class);

   @Override
   protected void configure(AuthenticationManagerBuilder builder) throws Exception
   {
      final InMemoryUserDetailsManagerConfigurer<AuthenticationManagerBuilder> configurer =
         builder.inMemoryAuthentication().passwordEncoder(passwordEncoder());
      // load and configure authorized devices list, passwords are sha-256 hashes
      Properties credentials = new Properties();
      try
      {
         credentials.load(Thread.currentThread().getContextClassLoader().getResourceAsStream("credentials.properties"));
      }
      catch (Exception e)
      {
         LOGGER.warn("Error while loading authorized devices credentials");
         throw new RuntimeException(e);
      }
      LOGGER.info("Loaded authorized devices credentials : {}", credentials);
      ShaPasswordEncoder shaPasswordEncoder = passwordEncoder();

      credentials.forEach((username, rawPassword) -> {
         String sha256passwordHash = shaPasswordEncoder.encodePassword(rawPassword.toString(),null);
         configurer.withUser((String) username).password((String) sha256passwordHash).roles("USER");
      });
   }

   @Override
   protected void configure(HttpSecurity http) throws Exception
   {
      // disable csrf protection if you would like to use the same device credentials form different browsers
      http.authorizeRequests().anyRequest().fullyAuthenticated().and().httpBasic().and().csrf().disable();
   }

   /**
    * Enable configuration of user credentials using SHA-256 password hashes.
    */
   @Bean
   public ShaPasswordEncoder passwordEncoder()
   {
      ShaPasswordEncoder encoder = new ShaPasswordEncoder(256); // TODO use BCrypt?
      return encoder;
   }

}
