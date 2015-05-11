; Copyright (c) Michael Jerger. All rights reserved.
; The use and distribution terms for this software are covered by the
; Eclipse Public License 1.0 (http://opensource.org/licenses/eclipse-1.0.php)
; which can be found in the file epl-v10.html at the root of this distribution.
; By using this software in any fashion, you are agreeing to be bound by
; the terms of this license.
; You must not remove this notice, or any other, from this software.

(ns httpd.crate.mod-jk
  (require 
    [clojure.string :as string]
    [pallet.actions :as actions]
    [httpd.crate.cmds :as cmds]
    ))

(defn vhost-jk-mount
  [& {:keys [mount-path worker]
      :or {mount-path "/*"
           worker "mod_jk_www"}}]
  [(str "JkMount " mount-path  " " worker)]
  )

(defn vhost-jk-unmount
  [& {:keys [mount-path worker]
      :or {mount-path "/*"
           worker "mod_jk_www"}}]
  [(str "JkUnMount " mount-path  " " worker)]
  )

(defn vhost-jk-status-location
  []
  ["<Location /jk-status>"
   "  # Inside Location we can omit the URL in JkMount"
   "  JkMount jk-status"
   "  Order deny,allow"
   "  Deny from all"
   "  Allow from 127.0.0.1"
   "</Location>"
   "<Location /jk-manager>"
   "  # Inside Location we can omit the URL in JkMount"
   "  JkMount jk-manager"
   "  Order deny,allow"
   "  Deny from all"
   "  Allow from 127.0.0.1"
   "</Location>"])

(defn workers-configuration
  []
  ["# workers.tomcat_home should point to the location where you"                                                                                    
   "# installed tomcat. This is where you have your conf, webapps and lib"                                                                           
   "# directories."                                                                                                                                
   "#workers.tomcat_home=/usr/share/tomcat6"                                                                                                          
   ""
   "# workers.java_home should point to your Java installation. Normally"                                                                            
   "# you should have a bin and lib directories beneath it."                                                                                         
   "#workers.java_home=/usr/lib/jvm/default-java"                                                                                                     
   ""                                                                                                                                                
   "# You should configure your environment slash... ps=\\ on NT and / on UNIX"                                                                       
   "# and maybe something different elsewhere."                                                                                                      
   "#ps=/"                                                                                                                                            
   ""
   "# The loadbalancer (type lb) workers perform wighted round-robin"
   "# load balancing with sticky sessions."
   "#worker.loadbalancer.type=lb"
   "#worker.loadbalancer.balance_workers=mod_jk_www"
   ""
   "worker.list=mod_jk_www"
   "worker.mod_jk_www.port=8009"
   "worker.mod_jk_www.host=127.0.0.1"
   "worker.mod_jk_www.type=ajp13"
   "worker.mod_jk_www.socket_timeout=900"
   "worker.mod_jk_www.socket_keepalive=false"
   "worker.mod_jk_www.connection_pool_timeout=100"
   ""])

(defn mod-jk-configuration
  []
  ["# Licensed to the Apache Software Foundation (ASF) under one or more"
   "# contributor license agreements.  See the NOTICE file distributed with"
   "# this work for additional information regarding copyright ownership."
   "# The ASF licenses this file to You under the Apache License, Version 2.0"
   "# (the \"License\"); you may not use this file except in compliance with"
   "# the License.  You may obtain a copy of the License at"
   "#"
   "#     http://www.apache.org/licenses/LICENSE-2.0"
   ""
   "<IfModule jk_module>"
   ""
   "  JkWorkersFile /etc/libapache2-mod-jk/workers.properties"
   "  "
   "  JkLogFile /var/log/apache2/mod_jk.log"
   "  JkLogLevel info"
   "  JkShmFile /var/log/apache2/jk-runtime-status"
   "  "
   "  JkOptions +RejectUnsafeURI"
   "  JkStripSession On"
   "  JkWatchdogInterval 60"
   "  "
   "</IfModule>"])


(defn install-mod-jk
  []
  (actions/package "libapache2-mod-jk")
  (cmds/a2enmod "jk")
  )