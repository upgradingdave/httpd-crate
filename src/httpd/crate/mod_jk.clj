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
  [& {:keys [path worker]
      :or {path "/*"
           worker "mod_jk_www"}}]
  [(str "JkMount " path  " " worker)]
  )

(defn vhost-jk-unmount
  [& {:keys [path worker]
      :or {path "/*"
           worker "mod_jk_www"}}]
  [(str "JkUnMount " path  " " worker)]
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
  "Takes a mod-jk configuration and returns Content for configure-mod-jk-worker"
  [config]
  (let [worker (get config :worker)
        host (get config :host)
        app-port (get config :app-port)
        socket-timeout (get config :socket-timeout)
        socket-connect-timeout (get config :socket-connect-timeout)]
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
   (str "worker.list=" worker)
   ;Review: dda says: "worker.maintain=60" which is missing here
   (str "worker." worker ".port=" app-port)
   (str "worker." worker ".host=" host)
   (str "worker." worker ".type=ajp13")
   (str "worker." worker ".socket_connect_timeout=" socket-connect-timeout)
   (str "worker." worker ".socket_timeout=" socket-timeout)
   ;Review: keepalive shoud prob be true see: dda documentation about timeouts
   (str "worker." worker ".socket_keepalive=false")
   (str "worker." worker ".connection_pool_timeout=100")
   ""]))

(defn mod-jk-configuration
  "Takes a mod-jk config and generates a Vector of Strings"
  [config]
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
   (str "  JkStripSession " (get config :JkStripSession))
   (str "  JkWatchdogInterval " (get config :JkWatchdogInterval))
   "  "
   "</IfModule>"])


(defn configure-mod-jk-worker
  "Takes a mod-jk configuration and creates a remote-file"
  [config]
  (actions/remote-file
    "/etc/libapache2-mod-jk/workers.properties"
    :owner "root"
    :group "root"
    :mode "644"
    :force true
    :content 
    (string/join
      \newline
      (workers-configuration config))))

(defn install-mod-jk
  "Takes a mod-jk configuration and installs mod-jk"
  [config]
  (actions/package "libapache2-mod-jk")
  (actions/remote-file
    "/etc/apache2/mods-available/jk.conf"
    :owner "root"
    :group "root"
    :mode "644"
    :force true
    :content 
    (string/join
      \newline
      (mod-jk-configuration config)
      ))  
  (cmds/a2enmod "jk")
  )